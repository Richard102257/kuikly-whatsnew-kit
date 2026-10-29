# Quick Start - 5 Minutes

## 🚀 最快集成（Maven 依赖）

### 1. 添加依赖（1 分钟）

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.Richard102257.whatsnew:core:1.0.0")
                implementation("io.github.Richard102257.whatsnew:compose:1.0.0")  // 或 dsl
            }
        }
    }
}
```

### 2. 定义内容（1 分钟）

```kotlin
val whatsNew = whatsNew {
    version("2.0.0")
    title("What's New")
    feature(
        imageSource = WhatsNew.ImageSource.SystemIcon("star.fill"),
        title = "New Feature",
        subtitle = "Check this out!"
    )
    primaryAction {
        title = "Continue"
        backgroundColor = 0xFF6200EE
        foregroundColor = 0xFFFFFFFF
    }
}
```

### 3. 展示（1 分钟）

**Compose DSL:**
```kotlin
WhatsNewDialog(whatsNew = whatsNew, onDismiss = { })
```

**Kuikly DSL:**
```kotlin
getRenderView().addSubView(WhatsNewDslView(whatsNew = whatsNew))
```

### 4. 完成！🎉

---

## 📦 安装方式速查

| 方式 | 依赖格式 | 难度 |
|------|----------|------|
| Maven Central | `io.github.Richard102257.whatsnew:core:1.0.0` | ⭐ |
| JitPack | `com.github.org:core:1.0.0` | ⭐ |
| 源码集成 | 复制源码到项目 | ⭐⭐ |
| Git Submodule | `git submodule add` | ⭐⭐ |

---

## 📋 API 速查

### 创建 WhatsNew

```kotlin
whatsNew {
    version("2.0.0")                    // 版本号
    title("What's New")                 // 标题

    feature(                            // 功能项
        imageSource = SystemIcon("star.fill"),
        title = "Title",
        subtitle = "Description"
    )

    primaryAction {                     // 主按钮
        title = "Continue"
        backgroundColor = 0xFF6200EE
        foregroundColor = 0xFFFFFFFF
        cornerRadius = 12f
    }

    secondaryAction {                   // 次按钮（可选）
        title = "Learn More"
        foregroundColor = 0xFF6200EE
        openURL("https://example.com")
        // 或 custom { /* 自定义 */ }
    }

    layout {                            // 布局（可选）
        copy(
            featureListSpacing = 24f,
            titleBottomSpacing = 40f
        )
    }
}
```

### 版本管理

```kotlin
// 创建集合
val collection = whatsNewCollection {
    whatsNew("1.0.0") { /* ... */ }
    whatsNew("2.0.0") { /* ... */ }
}

// 创建环境
val env = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = collection
)

// 获取当前需要展示的 WhatsNew
val toShow: WhatsNew? = env.whatsNew()

// 标记已展示
env.markAsPresented(version)
```

### 展示方式

```kotlin
// Compose: 弹窗
WhatsNewDialog(whatsNew = wn, onDismiss = { })

// Compose: 全屏
WhatsNewFullScreen(whatsNew = wn, onDismiss = { })

// Compose: 自动
WhatsNewComposeProvider(environment = env) { Content() }

// DSL: 视图
getRenderView().addSubView(WhatsNewDslView(whatsNew = wn))
```

### 图标名称对照

| SF Symbol | 本系统图标名 |
|-----------|-------------|
| star.fill | star.fill |
| heart.fill | heart.fill |
| bolt.fill | bolt.fill |
| globe | globe |
| sparkles | wand.and.stars |
| paintbrush.fill | paintbrush.fill |
| bell.fill | bell.fill |
| gear | gear |
| lock.fill | lock.fill |
| camera.fill | camera.fill |
| map.fill | map.fill |
| message.fill | message.fill |

---

## 🔧 平台特定初始化

### Android

```kotlin
// Application.onCreate()
initKuiklyWhatsNew(context)

// 获取持久化存储
val store = createPersistentWhatsNewVersionStore()
```

### iOS

```kotlin
// 无需额外初始化
// 使用 createPersistentWhatsNewVersionStore() 即可
```

---

## 📚 更多文档

- 完整集成教程: [INTEGRATION_zh.md](INTEGRATION_zh.md)
- 发布指南: [PUBLISHING.md](PUBLISHING.md)
- 示例代码: [example/](../example/)
