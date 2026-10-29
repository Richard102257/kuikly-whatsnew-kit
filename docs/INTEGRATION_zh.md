# 集成指南（中文）

本库是 **纯 Kuikly DSL 组件**（`componentType: KuiklyView`），只有一个模块 `:whatsnew`。
Android / iOS / HarmonyOS / Web / 小程序共用同一份代码，不需要任何平台侧原生实现。

平台差异只有两件事，通过 `expect/actual` 提供，每个平台约 25 行：

| 能力 | Android | iOS | HarmonyOS |
|------|---------|-----|-----------|
| 读取 App 版本号 | `PackageManager` | `NSBundle` | `BundleManager` |
| 持久化已展示版本 | `SharedPreferences` | `NSUserDefaults` | `Preferences` |

---

## 一、安装

### 1. JitPack（Android / 公共元数据，推荐）

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts（你的 KMP / Kuikly 共享模块）
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.1")
            }
        }
    }
}
```

### 2. iOS（CocoaPods）

```ruby
# Podfile
pod 'WhatsNew', '~> 1.1.1'
```

CocoaPods 分发的是**预编译 XCFramework**（`WhatsNew.xcframework.zip`，挂在 GitHub Release 上）。
Kotlin/Native 的 iOS 产物无法在 Linux 上构建，所以它由 macOS 构建后上传，
podspec 通过 `vendored_frameworks` 引用。

### 3. 本地源码（调试 / 二次开发）

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")

// build.gradle.kts
implementation(project(":whatsnew"))
```

---

## 二、定义内容

### 单个版本

```kotlin
import io.github.Richard102257.whatsnew.*

val whatsNew = whatsNew {
    version("2.0.0")                          // 对应 App 的 versionName
    title("What's New in v2.0")               // 可选，默认 "What's New"

    feature(
        icon = WhatsNewIcon.SystemIcon("star.fill"),
        title = "AI-Powered Development",
        subtitle = "Generate boilerplate with a single prompt."
    )
    feature(
        icon = WhatsNewIcon.SystemIcon("bolt.fill"),
        title = "Lightning Fast",
        subtitle = "Native rendering keeps 60fps on every platform."
    )

    // 主按钮（必有）
    primaryAction {
        title = "Continue"
        backgroundColor = 0xFF6200EE
        foregroundColor = 0xFFFFFFFF
        cornerRadius = 12f
        onDismiss = { /* 额外回调，可选 */ }
    }

    // 次按钮（可选）
    secondaryAction {
        title = "Learn More"
        foregroundColor = 0xFF6200EE
        openURL("https://github.com/Richard102257/kuikly-whatsnew-kit")
        // 或：custom { /* 自定义逻辑 */ }
    }
}
```

### 多个版本

```kotlin
val collection = whatsNewCollection {
    whatsNew("1.0.0") { title("首次发布"); /* ... */ }
    whatsNew("2.0.0") { title("AI 助手"); /* ... */ }
    whatsNew("2.1.0") { title("性能优化"); /* ... */ }
}
```

### 数据模型

| 类型 | 字段 |
|------|------|
| `WhatsNew` | `version: WhatsNewVersion`、`title: WhatsNewTitle`、`features: List<WhatsNewFeature>`、`primaryAction`、`secondaryAction?`、`layout` |
| `WhatsNewTitle` | `text: String`、`foregroundColor: Long`（ARGB） |
| `WhatsNewFeature` | `icon: WhatsNewIcon`、`title: String`、`subtitle: String?` |
| `WhatsNewIcon` | `SystemIcon(name)`、`UrlIcon(url)` |
| `WhatsNewPrimaryAction` | `title`、`backgroundColor`、`foregroundColor`、`cornerRadius`、`onDismiss` |
| `WhatsNewSecondaryAction` | `title`、`foregroundColor`、`cornerRadius`、`action: SecondaryActionType` |
| `SecondaryActionType` | `OpenURL(url)`、`Custom(handler)` |

> 颜色一律用 **ARGB Long**，例如 `0xFF6200EE`（不透明紫）。
> `SystemIcon` 目前用 emoji 占位渲染（`star.fill` → ⭐），无需图片资源。

---

## 三、在页面中使用

### 方式 A：Kuikly DSL 页面（推荐）

```kotlin
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.pager.Pager
import io.github.Richard102257.whatsnew.*

class HomePage : Pager() {

    private val environment = WhatsNewEnvironment(
        versionStore = createPersistentWhatsNewVersionStore(),
        whatsNewCollection = collection
    )

    override fun body(): ViewBuilder = {
        val pending = this@HomePage.environment.whatsNew()

        if (pending != null) {
            addChild(WhatsNewView()) {
                setup(pending) {
                    // 主按钮点击后：记录已展示
                    this@HomePage.environment.markAsPresented(pending.version)
                }
            }
        } else {
            addChild(HomeContent()) { }
        }
    }
}
```

### 方式 B：独立页面（路由打开）

```kotlin
// App 启动时设置一次默认环境
defaultWhatsNewEnvironment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = collection
)

// 任意位置打开
acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage("WhatsNewPage")
```

宿主创建 Pager 时需要把 `WhatsNewPage` 注册进去：

```kotlin
override fun createPage(pageName: String): Pager? {
    return when (pageName) {
        "WhatsNewPage" -> WhatsNewPage()
        else -> super.createPage(pageName)
    }
}
```

### 方式 C：Compose DSL 页面

```kotlin
import io.github.Richard102257.whatsnew.*

@Composable
fun MyScreen() {
    val state = remember { WhatsNewComposeState(environment) }

    Column(Modifier.fillMaxSize()) {
        Button(onClick = { state.autoPresent() }) { Text("查看更新") }
    }

    state.pendingWhatsNew?.let { pending ->
        // 用任意容器承载 WhatsNewView（例如 Kuikly 的 Compose 互操作节点）
        WhatsNewView().apply {
            setup(pending) { state.dismiss() }
        }
    }
}
```

---

## 四、版本匹配规则

`WhatsNewEnvironment.whatsNew()` 按以下顺序返回**第一个未展示过**的条目：

1. **精确匹配**：条目版本 == 当前 App 版本
2. **patch 回退**：`2.1.3` 没有时回退到 `2.1.0`
3. **最新未展示**：所有 `<= 当前版本` 的条目里，取最新且未展示的

展示后调用 `markAsPresented(version)` 持久化，下次不再弹出。

```kotlin
val env = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),  // 持久化
    whatsNewCollection = collection,
    defaultLayout = WhatsNewLayout.default                 // 统一布局
)

env.whatsNew()                    // 待展示内容，或 null
env.markAsPresented(version)      // 记录已展示
env.hasPresented(version)         // 是否已展示
env.currentVersion                // 当前 App 版本（WhatsNewVersion）
```

自定义存储：实现 `WhatsNewVersionStore` 接口即可（例如存到服务端）。

```kotlin
class RemoteStore : WhatsNewVersionStore {
    override fun savePresentedVersion(version: String) { /* ... */ }
    override fun getPresentedVersions(): Set<String> = emptySet()
    override fun hasPresented(version: String): Boolean = false
    override fun clear() { /* ... */ }
}
```

---

## 五、自定义布局

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

全局默认布局：

```kotlin
WhatsNewLayout.default = WhatsNewLayout(
    contentPadding = PaddingValues(top = 56f, start = 20f, bottom = 20f, end = 20f)
)
```

---

## 六、平台初始化

### Android

`getCurrentVersion()` 与 `createPersistentWhatsNewVersionStore()` 依赖 `Context`，
需在 `Application` 中初始化一次：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKuiklyWhatsNew(this)
    }
}
```

### iOS

无需初始化，直接使用 `NSBundle` / `NSUserDefaults`。

### HarmonyOS

无需初始化。当前鸿蒙侧的持久化为内存实现，如需落盘请替换
`OhosWhatsNewVersionStore` 中的 `loadFromPreferences` / `saveToPreferences`。

---

## 七、打开 URL

次按钮的 `openURL(...)` 由宿主注入实现（不同平台跳转方式不同）：

```kotlin
// App 启动时设置一次
whatsNewUrlOpener = { url ->
    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage("WebPage", ...)
    // 或调用平台侧的浏览器 / 内部路由
}
```

不设置时，`openURL` 点击无效果（不会崩溃）。

---

## 八、从 SvenTiigi/WhatsNewKit 迁移

| Swift | 本库 |
|-------|------|
| `WhatsNew(version:title:features:primaryAction:)` | `whatsNew { version(...); title(...); feature(...) }` |
| `WhatsNew.Feature(image:title:subtitle:)` | `WhatsNewFeature(icon, title, subtitle)` |
| `WhatsNew.Title(text:foregroundColor:)` | `WhatsNewTitle(text, foregroundColor)` |
| `WhatsNew.PrimaryAction(title:backgroundColor:foregroundColor:action:)` | `primaryAction { title = ...; onDismiss = ... }` |
| `WhatsNew.SecondaryAction(title:action:)` | `secondaryAction { title = ...; openURL(...) }` |
| `WhatsNew.Version(major:minor:patch:)` | `WhatsNewVersion(major, minor, patch)` |
| `WhatsNewEnvironment(whatsNewCollection:versionStore:)` | `WhatsNewEnvironment(whatsNewCollection = ..., versionStore = ...)` |
| `environment.whatsNew()` | `environment.whatsNew()` |
| `WhatsNewVersionStore` 协议 | `WhatsNewVersionStore` 接口 |

---

## 九、常见问题

**Q：为什么只有一个模块？**
WhatsNew 的 UI 完全可以用 Kuikly DSL（`DivView` / `TextView`）表达，Kuikly 会把它渲染成各平台原生控件。
再写一套 Android `LinearLayout`、iOS `UIView`、鸿蒙组件属于重复实现，因此已删除。

**Q：iOS 为什么走 XCFramework 而不是源码 pod？**
CocoaPods / Xcode 不编译 Kotlin。Kotlin/Native 必须先编译成 `.framework`，
再打包成 XCFramework 分发。

**Q：JitPack 上的版本号是什么格式？**
`com.github.<user>.<repo>:<module>:<tag>`，例如
`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.1`。

**Q：图标能用自己的图片吗？**
可以，使用 `WhatsNewIcon.UrlIcon("https://...")`。当前版本以占位符渲染，
后续会接入 Kuikly 的 `ImageView`。

**Q：支持深色模式吗？**
颜色是显式传入的 ARGB 值，按需给两套配色即可。
