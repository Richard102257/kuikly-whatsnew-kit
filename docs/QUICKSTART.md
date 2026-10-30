# 5 分钟快速上手

## 1. 添加依赖

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.1")
            }
        }
    }
}
```

iOS（CocoaPods）：

```ruby
pod 'KuiklyWhatsNew', '~> 1.2.1'
```

> 只有一个模块，Android / iOS / HarmonyOS / Web / 小程序通用。

---

## 2. 定义 WhatsNew 内容

```kotlin
import io.github.Richard102257.whatsnew.*

val whatsNew = whatsNew {
    version("2.0.0")                       // 对应 App 的 versionName
    title("What's New in v2.0")

    feature(
        icon = WhatsNewIcon.SystemIcon("star.fill"),
        title = "AI-Powered Development",
        subtitle = "Generate boilerplate with a single prompt."
    )
    feature(
        icon = WhatsNewIcon.SystemIcon("bolt.fill"),
        title = "Lightning Fast",
        subtitle = "Native rendering keeps 60fps everywhere."
    )

    primaryAction {
        title = "Continue"
        backgroundColor = 0xFF6200EE
        foregroundColor = 0xFFFFFFFF
    }

    secondaryAction {
        title = "Learn More"
        openURL("https://github.com/Richard102257/kuikly-whatsnew-kit")
    }
}
```

多个版本可以一起定义，环境会自动挑出「当前版本该展示的那一个」：

```kotlin
val collection = whatsNewCollection {
    whatsNew("1.0.0") { /* ... */ }
    whatsNew("2.0.0") { /* ... */ }
    whatsNew("2.1.0") { /* ... */ }
}
```

---

## 3. 在 Kuikly DSL 页面中使用

```kotlin
class MyPage : Pager() {
    private val environment = WhatsNewEnvironment(
        versionStore = createPersistentWhatsNewVersionStore(),
        whatsNewCollection = collection
    )

    override fun body(): ViewBuilder = {
        addChild(WhatsNewView()) {
            setup(environment.whatsNew()) {
                // 用户点了「Continue」之后的回调
            }
        }
    }
}
```

---

## 4. 自动展示（推荐）

```kotlin
val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = collection
)

// 有新版本时返回内容，否则返回 null
val pending = environment.whatsNew()
if (pending != null) {
    showWhatsNew(pending) {
        environment.markAsPresented(pending.version)
    }
}
```

`WhatsNewEnvironment.whatsNew()` 的匹配顺序：

1. **精确匹配** 当前版本
2. **patch 回退**：`2.1.3` 找不到时回退到 `2.1.0`
3. **最新未展示版本**：取 `<= 当前版本` 里最新的、还没展示过的

已展示过的版本会被持久化（Android: `SharedPreferences`，iOS: `NSUserDefaults`，
鸿蒙: `Preferences`），不会重复弹出。

---

## 5. 平台初始化

**Android**：在 `Application` 中初始化一次，供版本号读取和存储使用：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKuiklyWhatsNew(this)
    }
}
```

**iOS**：无需初始化，直接使用 `NSBundle` / `NSUserDefaults`。

**HarmonyOS**：需要两件事（`bundleManager` 是 ArkTS API，库内暂无绑定）：

```kotlin
// 1. 指定版本号（否则恒为 1.0.0，组件不会显示）
whatsNewCurrentVersionOverride = "1.2.0"

// 2. 页面里改用 Kuikly 模块存储（默认实现是内存，冷启动会丢）
class HomePage : Pager() {
    private val environment by lazy {
        WhatsNewEnvironment(
            versionStore = createWhatsNewVersionStore(),
            whatsNewCollection = collection
        )
    }
}
```

**Web / 微信小程序**：无需初始化，版本号与存储自动探测（详见 [MINIAPP_zh.md](MINIAPP_zh.md)）。

---

## 6. 自定义布局

```kotlin
whatsNew {
    version("2.0.0")
    layout {
        copy(
            contentPadding = PaddingValues(top = 48f, start = 24f, bottom = 24f, end = 24f),
            titleBottomSpacing = 32f,
            featureListSpacing = 20f,
            footerActionSpacing = 16f,
            primaryButtonHeight = 52f,
            secondaryButtonHeight = 48f
        )
    }
}
```

---

## 下一步

- 完整集成说明：[INTEGRATION_zh.md](INTEGRATION_zh.md)
- 小程序 / H5 接入：[MINIAPP_zh.md](MINIAPP_zh.md)
- 发布流程：[PUBLISHING.md](PUBLISHING.md)
- 验证记录：[VERIFICATION.md](VERIFICATION.md)
