# 集成指南（中文）

本库是 **纯 Kuikly DSL 组件**（`componentType: KuiklyView`），只有一个模块 `:whatsnew`。
Android / iOS / HarmonyOS / Web / 微信小程序共用同一份代码，不需要任何平台侧原生实现。

平台差异只有两件事，通过 `expect/actual` 提供：

| 能力 | Android | iOS | HarmonyOS | JS（小程序 / H5） |
|------|---------|-----|-----------|------------------|
| 读取 App 版本号 | `PackageManager` | `NSBundle` | 占位（需 `whatsNewCurrentVersionOverride`） | `wx.getAccountInfoSync()` / `<meta name="app-version">` |
| 持久化已展示版本 | `SharedPreferences` | `NSUserDefaults` | 内存（或改用 `createWhatsNewVersionStore()`） | `wx.setStorageSync` / `localStorage` |

## 平台接入索引

| 平台 | 依赖方式 | 额外步骤 | 详细文档 |
|------|----------|----------|----------|
| **Android** | JitPack（Maven 坐标） | `initKuiklyWhatsNew(context)` | 本页 |
| **iOS** | ① CocoaPods ② 源码模块 | 无 | 本页 |
| **鸿蒙** | 源码模块 | ① `whatsNewCurrentVersionOverride` ② 页面内用 `createWhatsNewVersionStore()` | 本页「六、平台初始化」 |
| **H5** | JitPack（js klib） | `js(IR) { binaries.executable() }` + Kuikly 插件 + `h5App-js` 壳 | [MINIAPP_zh.md](MINIAPP_zh.md) |
| **微信小程序** | JitPack（js klib） | 同上 + `miniApp-js` 壳 | [MINIAPP_zh.md](MINIAPP_zh.md) |

---

## 一、安装

### 1. JitPack（Android / Web / 小程序，推荐）

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
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.1")
            }
        }
    }
}
```

### 2. iOS

JitPack 在 Linux 上构建，只能产出 Android AAR + JS klib + 公共元数据；iOS 的
Kotlin/Native klib 必须在 macOS 上编译。因此 iOS 有三种引入方式：

**A. 源码模块（推荐）**

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")
```

在 macOS 上构建时 iOS target 自动注册，klib 随 App 一起编译。

**B. CocoaPods（预编译 XCFramework）**

```ruby
# Podfile
pod 'KuiklyWhatsNew', '~> 1.2.1'
```

podspec 指向 GitHub Release 上的 `WhatsNew.xcframework.zip`（`ios-arm64` +
`ios-arm64_x86_64-simulator`），由 macOS 上的 `./gradlew :whatsnew:buildXCFramework`
构建后上传，podspec 通过 `vendored_frameworks` 引用。

**C. Maven Central（可选）**

在 macOS 上用 Sonatype 凭据执行 `./gradlew :whatsnew:publish`，会把 iOS klib
一并发布，之后 iOS 也能直接用 Maven 坐标。

### 3. 本地源码（调试 / 二次开发）

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")

// build.gradle.kts
implementation(project(":whatsnew"))
```

### 4. 鸿蒙

鸿蒙没有可用的 Maven 产物，只能以**源码模块**方式引入（原因见下）：

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")
```

前置条件（缺一不可）：

| 条件 | 说明 |
|------|------|
| Kuikly 定制 Kotlin | 需要 `2.0.21-KBA-010`，标准 Kotlin 没有 `ohosArm64` 目标 |
| OHOS 渲染层 | `core-render-ohos` **没有发布到 Maven**（Central / 腾讯镜像都 404），由 KuiklyUI 仓库源码构建，宿主鸿蒙工程自带 |
| 版本号 | 鸿蒙侧的 `bundleManager` 是 ArkTS API，本库暂无 cinterop 绑定，**必须**手动设置 `whatsNewCurrentVersionOverride` |
| 持久化 | 默认实现是内存，需要跨会话保留时用 `createWhatsNewVersionStore()` |

`whatsnew/build.ohos.gradle.kts` 是独立的鸿蒙构建脚本（只声明 DSL 层 `core`）：

```bash
cp whatsnew/build.ohos.gradle.kts whatsnew/build.gradle.kts
./gradlew :whatsnew:assemble
git checkout whatsnew/build.gradle.kts
```

> ⚠️ 鸿蒙路径**未实机验证**（本机没有 DevEco / OHOS SDK）。
> 代码层面 `ohosArm64Main` 的 `expect/actual` 已就位，纯 DSL 的 UI 由 Kuikly 渲染，
> 但上面两个「额外步骤」必须做，否则组件不会显示（版本号恒为 `1.0.0`）
> 或每次冷启动都重复显示（存储不落盘）。

### 5. Web（H5）/ 微信小程序

```kotlin
// build.gradle.kts（你的 Kuikly 业务模块）
implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.1")
```

还需要 `js(IR) { binaries.executable() }` + Kuikly Gradle 插件 + KSP，
以及 Kuikly 官方的 `h5App-js` / `miniApp-js` 壳工程来加载 bundle。

👉 完整步骤见 [MINIAPP_zh.md](MINIAPP_zh.md)。

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

### 方式 C：Compose 页面

本库提供 `WhatsNewComposeState` 做状态管理（`pendingWhatsNew` / `autoPresent()` /
`dismiss()`），渲染仍复用 Kuikly DSL 的 `WhatsNewView`：

```kotlin
import io.github.Richard102257.whatsnew.*

val state = remember { WhatsNewComposeState(environment) }

// 任意触发点
Button(onClick = { state.autoPresent() }) { Text("查看更新") }

// 展示（用你工程里承载 Kuikly 视图的方式挂载 WhatsNewView）
state.pendingWhatsNew?.let { pending ->
    whatsNewViewNode(pending) { state.dismiss() }
}
```

> 完整的 `@Composable` 包装需要额外依赖 `com.tencent.kuikly-open:compose`，
> 当前版本未引入，避免给只用 Kuikly DSL 的工程增加依赖。

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

需要两件事（原因见「一、安装 → 4. 鸿蒙」）：

**1. 指定版本号**（鸿蒙侧无法自动读取）：

```kotlin
whatsNewCurrentVersionOverride = "1.2.0"
```

**2. 让「已展示版本」真正落盘**：默认的 `createPersistentWhatsNewVersionStore()`
在鸿蒙上是内存实现，冷启动会丢失。在页面里改用 `createWhatsNewVersionStore()`
（基于 Kuikly 内置的 `SharedPreferencesModule`，鸿蒙 render 层已实现）：

```kotlin
class HomePage : Pager() {

    private val environment by lazy {
        WhatsNewEnvironment(
            versionStore = createWhatsNewVersionStore(),   // this: Pager
            whatsNewCollection = collection
        )
    }

    override fun body(): ViewBuilder = {
        val pending = this@HomePage.environment.whatsNew()
        // ...
    }
}
```

### 所有平台通用：页面内存储（含鸿蒙）

`createWhatsNewVersionStore()` 是 `Pager` 的扩展函数，底层是 Kuikly 的
`SharedPreferencesModule`，Android / iOS / 鸿蒙 / H5 / 小程序的 render 层各自实现了它，
所以**一个实现覆盖全部平台**：

```kotlin
override fun created() {
    super.created()
    // 也可以在 created 里提前创建，注意此时已有 Pager
    val store = createWhatsNewVersionStore()
}
```

| 用哪个 | 时机 | 覆盖平台 |
|--------|------|----------|
| `createPersistentWhatsNewVersionStore()` | App 启动阶段（还没有 Pager） | Android / iOS / H5 / 小程序（鸿蒙为内存） |
| `createWhatsNewVersionStore()` | 页面内（有 Pager） | **全部平台，含鸿蒙** |

### 微信小程序 / H5

无需初始化，库会在调用时自动探测：

- 版本号：`wx.getAccountInfoSync().miniProgram.version` → `<meta name="app-version">`
  → `window.__WHATSNEW_VERSION__` → `1.0.0`
- 存储：`wx.setStorageSync` → `localStorage` → 内存兜底

小程序开发版读不到版本号，建议显式覆盖：

```kotlin
whatsNewCurrentVersionOverride = "1.2.0"
```

H5 也可以用 meta 标签，让版本号跟着构建产物走：

```html
<meta name="app-version" content="1.2.0">
```

宿主侧还需要 `js(IR) { binaries.executable() }` + Kuikly Gradle 插件 + KSP，
详见 [MINIAPP_zh.md](MINIAPP_zh.md)。

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
`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.1`。

**Q：图标能用自己的图片吗？**
可以，使用 `WhatsNewIcon.UrlIcon("https://...")`。当前版本以占位符渲染，
后续会接入 Kuikly 的 `ImageView`。

**Q：支持深色模式吗？**
颜色是显式传入的 ARGB 值，按需给两套配色即可。
