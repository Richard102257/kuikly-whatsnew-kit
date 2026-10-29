# 架构决策：为什么是「单一纯 DSL 模块」

> 本文替换了早期的 `MIGRATION_TO_EXTENSION.md`（那份文档描述的是
> `whatsnew` + `whatsnewAndroid` + `whatsnewIOS` + `whatsnewOhos` 四模块方案，
> 该方案已废弃）。

## 背景

KuiklyUI-third-party 的组件分两类：

| `componentType` | 含义 | 是否需要平台侧代码 |
|-----------------|------|-------------------|
| `KuiklyView` | 用 Kuikly DSL 组合出的跨端组件 | 不需要 |
| `NativeView` | 各平台用原生 View 实现的组件 | 需要 Android / iOS / Ohos 三套 |

最初的实现选了 `NativeView` 路线：`whatsnewAndroid` 写 `LinearLayout`，
`whatsnewIOS` 写 `UIView`，`whatsnewOhos` 写鸿蒙组件，`whatsnew` 只放数据模型。

## 问题

1. **三份重复实现**：WhatsNew 的界面就是标题 + 功能列表 + 两个按钮，
   `DivView` / `TextView` 完全能表达，平台侧再写一遍纯属重复。
2. **API 不匹配**：三套原生实现依赖了并不存在的 Kuikly 扩展 API
   （`@KRRenderViewExport`、`com.kuikly.core.export.*`），实际无法编译。
3. **维护成本**：任何 UI 调整要改三遍，且需要三套真机验证。
4. **发布成本**：三个模块三套产物（AAR / framework / .so），发布链路复杂。

## 决策

**收敛为单一模块 `:whatsnew`，`componentType: KuiklyView`，纯 Kuikly DSL 实现。**

```
whatsnew/
└── src/
    ├── commonMain/     # 数据模型 + DSL View + 版本匹配（主体）
    ├── androidMain/    # 版本号 + SharedPreferences
    ├── iosMain/        # NSBundle + NSUserDefaults
    └── ohosArm64Main/  # 鸿蒙 Preferences
```

平台差异只剩两件事，用 `expect/actual` 表达：

```kotlin
expect fun getCurrentVersion(): WhatsNewVersion
expect fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore
```

## 影响

| 项目 | 变化 |
|------|------|
| 依赖 | 从 2~3 个坐标收敛为 1 个：`...:whatsnew:<version>` |
| Android | JitPack 直接产出 AAR + klib |
| iOS | 预编译 XCFramework（`KuiklyWhatsNew.podspec`），macOS 构建 |
| HarmonyOS | 同一份 klib，无需额外模块 |
| Web / 小程序 | 同一份 DSL 代码，无需额外模块 |

## 什么情况下才需要回到 `NativeView`

只有当你必须调用平台原生控件（例如系统相机、原生地图、系统分享面板）时，
才需要为对应平台实现 `IKuiklyRenderViewExport` 并单独发布平台模块。
WhatsNew 不属于这一类。
