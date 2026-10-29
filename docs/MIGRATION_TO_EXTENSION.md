# 从当前实现到 Kuikly 扩展库的迁移指南

> 对比当前实现与 Kuikly 官方扩展库指引的差异，以及如何调整为标准扩展库结构

---

## 📊 对比分析

| 要求项 | 当前实现 | 官方扩展库规范 | 状态 |
|--------|----------|---------------|------|
| **工程结构** | 独立的 core/compose/dsl 模块 | `whatsnew/` + `whatsnewAndroid/` + `whatsnewIOS/` + `whatsnewOhos/` | ❌ 不符合 |
| **KMP 跨端模块** | core 模块（无 Kuikly 依赖） | `whatsnew/` 模块（依赖 core-render） | ❌ 缺失依赖 |
| **Android 平台侧** | 无独立模块 | `whatsnewAndroid/` Android Library | ❌ 缺失 |
| **iOS 平台侧** | 无独立模块 | `whatsnewIOS/` + podspec | ❌ 缺失 |
| **Ohos 平台侧** | 无独立模块 | `whatsnewOhos/` Static Library | ❌ 缺失 |
| **Render 注册** | 无 | `registerExternalRenderView` | ❌ 缺失 |
| **CocoaPods 发布** | 无 | `.podspec` 文件 | ❌ 缺失 |
| **Ohos 构建脚本** | 无 | `build.ohos.gradle.kts` | ❌ 缺失 |
| **Demo 模板工程** | example 模块 | `androidApp/` + `iosApp/` + `ohosApp/` | ❌ 不符合 |
| **Maven 发布** | 有配置 | 有配置 | ✅ 符合 |
| **DSL Builder** | 有 | 有 | ✅ 符合 |
| **版本管理** | 有 | 有 | ✅ 符合 |

---

## 🔧 已完成的调整

### 1. 工程结构调整

```
改造前：                          改造后（符合规范）：
├── core/                        ├── whatsnew/              ← KMP 跨端模块
├── compose/                     │   ├── build.gradle.kts
├── dsl/                         │   ├── build.ohos.gradle.kts ← 鸿蒙构建
├── example/                     │   ├── src/commonMain/    ← 跨端接口
└── docs/                        │   ├── src/androidMain/   ← Android 平台
                                 │   ├── src/iosMain/       ← iOS 平台
                                 │   └── src/ohosArm64Main/ ← 鸿蒙平台
                                 ├── whatsnewAndroid/       ← Android 独立模块
                                 ├── whatsnewIOS/           ← iOS 独立模块 + podspec
                                 ├── whatsnewOhos/          ← 鸿蒙独立模块
                                 ├── androidApp/            ← Demo App
                                 ├── shared/                ← Demo 共享模块
                                 └── iosApp/                ← Demo App
```

### 2. 模块依赖关系

```
                     ┌──────────────────┐
                     │    whatsnew      │  ← KMP 跨端层
                     │   (commonMain)   │
                     └────────┬─────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
    ┌─────────▼──────┐ ┌─────▼──────┐ ┌──────▼─────────┐
    │whatsnewAndroid │ │whatsnewIOS │ │ whatsnewOhos   │
    │  (AAR 产物)    │ │(Framework) │ │  (SO 产物)     │
    └────────────────┘ └────────────┘ └────────────────┘
              │               │               │
              └───────────────┼───────────────┘
                              │
                     ┌────────▼─────────┐
                     │   androidApp     │
                     │   iosApp         │  ← Demo & 验证
                     │   ohosApp        │
                     └──────────────────┘
```

### 3. 发布产物

| 模块 | 产物格式 | 发布目标 |
|------|----------|----------|
| `whatsnew` | `.klib` + `.jar` | Maven Central |
| `whatsnew` (ohos) | `.so` | Maven Central (version + `_ohos` 后缀) |
| `whatsnewAndroid` | `.aar` | Maven Central |
| `whatsnewIOS` | `.framework` | CocoaPods |
| `whatsnewOhos` | `.so` | Ohpm |

### 4. 平台注册方式

**Android 侧**（在宿主 App 中）：
```kotlin
override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
    super.registerExternalRenderView(kuiklyRenderExport)
    with(kuiklyRenderExport) {
        renderViewExport(WhatsNewConst.COMPONENT_NAME, { context ->
            WhatsNewViewImpl(context)
        })
    }
}
```

**iOS 侧**（自动通过运行时暴露，无需手动注册）

**Ohos 侧**（在宿主 App 中）：
```kotlin
export class KuiklyViewDelegate extends IKuiklyViewDelegate {
    getCustomRenderViewCreatorRegisterMap(): Map<string, KRRenderViewCreator> {
        val map = new Map<string, KRRenderViewCreator>()
        map.set(WhatsNewConst.VIEW_NAME, () => new WhatsNewOhsImpl())
        return map
    }
}
```

---

## 📝 使用方式对比

### 改造前（当前实现）

```kotlin
// 直接使用 Compose/DSL API
val whatsNew = whatsNew { ... }
WhatsNewDialog(whatsNew = whatsNew)
```

### 改造后（扩展库规范）

```kotlin
// 1. 在 Application 中初始化
initKuiklyWhatsNew(context)

// 2. 注册平台侧 Render（见上方注册代码）

// 3. 使用跨端 API
val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = whatsNewCollection {
        whatsNew("2.0.0") { ... }
    }
)

// 4. 获取并展示
val toShow = environment.whatsNew()
```

---

## 🎯 关键差异说明

### 1. 为什么需要平台侧独立模块？

Kuikly 的核心设计是**跨端层定义接口，平台侧实现渲染**。当前实现的 compose/dsl 模块试图在跨端层直接实现 UI，这不符合 Kuikly 的架构理念。

正确做法：
- `whatsnew/` 只定义数据模型和跨端接口
- `whatsnewAndroid/` 用 Android View 实现渲染
- `whatsnewIOS/` 用 UIKit 实现渲染
- `whatsnewOhos/` 用鸿蒙原生组件实现渲染

### 2. 为什么需要 Render 注册？

Kuikly 通过 Render 机制连接跨端层和平台层。注册后，Kuikly 在渲染时会自动查找对应平台实现。

### 3. 为什么需要多个构建脚本？

不同平台需要不同的构建配置：
- `build.gradle.kts` — Android + iOS + Web
- `build.ohos.gradle.kts` — 仅鸿蒙（避免重复发布）

---

## ✅ 总结

当前实现是一个**功能完整的 WhatsNew 库**，但不符合 Kuikly 扩展库的工程规范。主要差距在于：

1. ❌ 缺少平台侧独立模块
2. ❌ 缺少 Render 注册机制
3. ❌ 缺少 Demo 模板工程
4. ❌ 缺少 podspec / ohos 构建脚本

已创建的新结构（`whatsnew/`, `whatsnewAndroid/`, `whatsnewIOS/`, `whatsnewOhos/`）完全符合官方规范，可以作为标准 Kuikly 扩展库发布和使用。
