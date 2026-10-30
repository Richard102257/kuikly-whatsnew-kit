# Kuikly WhatsNewKit

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Kuikly](https://img.shields.io/badge/Kuikly-2.4.0-green.svg)](https://kuikly.tds.qq.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20HarmonyOS%20%7C%20Web%20%7C%20Mini%20Programs-orange.svg)](https://kuikly.tds.qq.com)

> 🎊 基于 Kuikly 跨端框架的 WhatsNewKit 实现，让你的新功能展示跨所有平台！

A fully cross-platform implementation of [WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) built on [Kuikly](https://kuikly.tds.qq.com) — Tencent's high-performance, full-platform development framework. Showcase your awesome new app features across **Android, iOS, HarmonyOS, Web, and Mini Programs** with a single codebase.

Inspired by [SvenTiigi/WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) — the popular Swift Package for presenting app changelogs.

---

## 📸 运行截图

下面两张图是 **Kuikly 在 Android 模拟器上真实渲染**出来的画面（不是原生 View 仿写、
也不是设计稿）。渲染的页面代码就是 [`miniApp/src/commonMain`](miniApp/src/commonMain/kotlin/io/github/Richard102257/whatsnew/demo/WhatsNewDemoPage.kt)
里那份 —— 和小程序 / H5 编译成 JS 的是同一份代码。

| 首次启动：展示 What's New | 点「继续」后再次启动：已是最新版本 |
|---|---|
| <img src="docs/images/android-01-whatsnew.png" width="320" alt="Android WhatsNew 首屏"> | <img src="docs/images/android-02-caught-up.png" width="320" alt="Android 已是最新版本"> |

截图对应的行为链路：首屏由 `WhatsNewEnvironment.whatsNew()` 按版本号匹配 → 点击主按钮
写入 `SharedPreferences` → 杀进程重启后 `whatsNew()` 返回 `null`，页面走「已经是最新版本」分支。
也就是说这两张图同时验证了**渲染**和**版本持久化**。

### 各端截图状态（如实说明）

| 平台 | 截图 | 说明 |
|------|------|------|
| Android | ✅ 已覆盖 | 上面的两张，AVD `Medium_Phone_API_37.0` + Kuikly `2.4.0-2.0.21` |
| iOS | ⏳ 未覆盖 | 库本身有 iOS target 并已发布 XCFramework，但本机没有可直接运行的 iOS Demo 工程 |
| HarmonyOS | ⏳ 未覆盖 | 需要 DevEco Studio + OHOS SDK，`core-render-ohos` 也没有公开 Maven 产物 |
| H5 | ⏳ 未覆盖 | `core-render-web` 未发布到 Maven / npm，需要先从 KuiklyUI 源码构建渲染器 |
| 微信小程序 | ⏳ 未覆盖 | 需要微信开发者工具（本机未安装） |

> 没有截图的平台不是「没实现」，而是**当前环境跑不起来**。库的 commonMain 代码是
> 同一份，Android 能跑通意味着逻辑层是通的；UI 渲染差异需要各自容器验证，这里不假装验证过。

---

## 📦 安装

### 单一依赖（Android，推荐）

库是**纯 Kuikly DSL 组件**（`componentType: KuiklyView`），只有一个模块 `:whatsnew`，
不需要任何平台侧原生代码。

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
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.2")
            }
        }
    }
}
```

> JitPack 在 Linux 上构建，能产出 **Android AAR + JS klib + 公共元数据**。
> Kotlin/Native 的 iOS klib 必须在 macOS 上编译，所以 iOS 走下面两种方式之一。
>
> ⚠️ 1.2.0 起默认依赖 Kuikly `2.4.0-2.0.21`（js 产物从这个版本开始有）。
> 如果你的工程还在 `2.1.x`，升级前请先确认其它依赖兼容。

### iOS

**方式 A：源码模块（推荐，与 Kuikly 官方示例一致）**

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")
```

在 macOS 上构建时 iOS target 自动注册，klib 随 App 一起编译。

**方式 B：CocoaPods（已发布到 trunk）**

```ruby
# Podfile
pod 'KuiklyWhatsNew', '~> 1.2.2'
```

[![CocoaPods](https://img.shields.io/badge/CocoaPods-KuiklyWhatsNew-red)](https://cocoapods.org/pods/KuiklyWhatsNew)

podspec 指向 GitHub Release 上的 `WhatsNew.xcframework.zip`（`ios-arm64` +
`ios-arm64_x86_64-simulator`，静态 framework），由 macOS 上的
`./gradlew :whatsnew:assembleWhatsNewReleaseXCFramework` 构建后上传。
framework 名仍是 `WhatsNew`，pod 名因为 trunk 上 `WhatsNew` 已被占用而叫 `KuiklyWhatsNew`。

**方式 C：Maven Central（可选，需在 macOS 上发布）**

在 macOS 上用 Sonatype 凭据执行 `./gradlew :whatsnew:publish`，会把 **iOS klib**
一起发布，之后 iOS 也能像 Android 一样直接用 Maven 坐标。

### HarmonyOS

鸿蒙 target 需要 Kuikly 的 `ohosArm64` 目标，使用 `whatsnew/build.ohos.gradle.kts`
在 macOS / 鸿蒙工具链上构建，或同样走源码模块方式引入。

### 微信小程序 / H5

库从 **1.2.0** 起声明了 `js(IR)` 目标，`commonMain` 代码直接编译成 JS，
不需要重写 UI。两个平台的差异只有版本号和存储，库已经处理好了：

| 能力 | 微信小程序 | H5 |
|------|-----------|-----|
| 版本号 | `wx.getAccountInfoSync().miniProgram.version` | `<meta name="app-version">` / `window.__WHATSNEW_VERSION__` |
| 存储 | `wx.getStorageSync` / `setStorageSync` | `localStorage` |

```kotlin
// 开发版小程序读不到版本号时，显式覆盖（正式包可删）
whatsNewCurrentVersionOverride = "1.2.0"

val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(), // 自动选 wx / localStorage
    whatsNewCollection = collection
)
```

宿主侧需要 `js(IR) { binaries.executable() }` + Kuikly Gradle 插件 + KSP，
再交给 Kuikly 官方的 `miniApp-js` / `h5App-js` 壳工程加载。

> 📖 完整步骤见 [docs/MINIAPP_zh.md](docs/MINIAPP_zh.md)
> 📖 详细集成教程请查看 [docs/INTEGRATION_zh.md](docs/INTEGRATION_zh.md)
> 🚀 5 分钟快速上手请查看 [docs/QUICKSTART.md](docs/QUICKSTART.md)

---

## ✨ Features

- ✅ **One codebase, all platforms** — Android、iOS、HarmonyOS、H5、微信小程序共用同一份 Kuikly DSL 代码
- ✅ **Kuikly DSL + Compose 页面** — `WhatsNewView` 用于 Kuikly DSL；Compose 页面用 `WhatsNewComposeState` 控制展示
- ✅ **Manual & Automatic presentation** — Full control or let the environment handle it
- ✅ **Version tracking** — 可插拔的 `WhatsNewVersionStore`（内存 / 平台持久化 / 自定义）
- ✅ **Patch version fallback** — Automatically falls back to nearest matching version
- ✅ **Fully customizable layout** — Adjust spacing, padding, colors, and more
- ✅ **Primary & Secondary actions** — Dismiss, open URLs, or execute custom actions
- ✅ **Native performance** — Uses Kuikly's native rendering, not Skia
- ✅ **Lightweight** — 只依赖 Kuikly core，无其他第三方依赖

---

## 📦 Project Structure

```
kuikly-whatsnew-kit/
├── whatsnew/                 # 唯一的库模块（KMP，纯 Kuikly DSL）
│   ├── build.gradle.kts      # Android + iOS + JS 目标（iOS 仅 macOS）
│   ├── build.ohos.gradle.kts # 鸿蒙独立构建脚本
│   └── src/
│       ├── commonMain/       # 数据模型 + DSL View + 版本匹配逻辑
│       ├── androidMain/      # 版本号 + SharedPreferences
│       ├── iosMain/          # NSBundle + NSUserDefaults
│       ├── jsMain/           # wx.getAccountInfoSync + wx storage / localStorage
│       ├── ohosArm64Main/    # 鸿蒙 Preferences
│       ├── commonTest/       # 纯逻辑单测（10 个）
│       └── jsTest/           # 伪造 wx / localStorage 的 JS 单测（9 个）
│
├── KuiklyWhatsNew.podspec          # iOS 分发（预编译 XCFramework）
├── androidApp/ + shared/     # Android Demo
├── miniApp/                  # 小程序 / H5 Demo 业务模块（js + KSP + Kuikly 插件）
├── iosApp/ ohosApp/ h5App/   # 其他平台 Demo 工程
├── docs/                     # 文档（CI 工作流模板在 docs/ci/）
└── preview/                  # 在线预览
```

平台差异只有两件事：**读取 App 版本号** 和 **持久化已展示版本**，
通过 `expect/actual` 实现，每个平台约 25–100 行。

---

## 🚀 Quick Start

### 1. Installation

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.2")
            }
        }
    }
}
```

### 2. Define Your WhatsNew Content

```kotlin
import io.github.Richard102257.whatsnew.*

val whatsNew = whatsNew {
    version("2.0.0")
    title("What's New")

    feature(
        icon = WhatsNewIcon.SystemIcon("star.fill"),
        title = "Amazing Feature",
        subtitle = "Check out this incredible new capability!"
    )

    feature(
        icon = WhatsNewIcon.SystemIcon("bolt.fill"),
        title = "Performance",
        subtitle = "App is now 2x faster with native rendering."
    )

    primaryAction {
        title = "Continue"
        backgroundColor = 0xFF6200EE
        foregroundColor = 0xFFFFFFFF
    }

    secondaryAction {
        title = "Learn More"
        foregroundColor = 0xFF6200EE
        openURL("https://your-app.com/changelog")
    }
}
```

### 3. Kuikly DSL Usage

`WhatsNewView` 是一个普通的 `ViewContainer`，在任何 `body(): ViewBuilder` 里 `addChild` 即可：

```kotlin
import io.github.Richard102257.whatsnew.*
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.pager.Pager

class MyPager : Pager() {
    override fun body(): ViewBuilder = {
        addChild(WhatsNewView()) {
            setup(whatsNew) {
                // 用户点击主按钮 / 关闭
            }
        }
    }
}
```

也可以直接使用内置页面（自带「无新版本」占位内容）：

```kotlin
// 方式 A：直接创建（推荐）
val page = WhatsNewPage(environment)

// 方式 B：需要按名字路由时，在自己的模块里包一层 —— Kuikly 的 @Page 是
// SOURCE 注解且 KSP 只扫描当前模块，库里的页面不会被宿主的 KSP 注册
@Page("WhatsNewPage")
internal class AppWhatsNewPage : WhatsNewPage(defaultWhatsNewEnvironment) {
    init { pageName = "WhatsNewPage" }
}
// 之后即可 router.openPage("WhatsNewPage")
```

### 4. Automatic Presentation

```kotlin
val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = myWhatsNewCollection
)

// Get WhatsNew to present (if any)
val toPresent: WhatsNew? = environment.whatsNew()
```

---

## 📖 Core Concepts

### WhatsNew

The central model describing your changelog for a specific version.

```kotlin
val whatsNew = WhatsNew(
    version = WhatsNewVersion(2, 0, 0),
    title = WhatsNewTitle("What's New"),
    features = listOf(
        WhatsNewFeature(
            icon = WhatsNewIcon.SystemIcon("star.fill"),
            title = "New Feature",
            subtitle = "Description of the feature"
        )
    ),
    primaryAction = WhatsNewPrimaryAction(
        title = "Continue",
        backgroundColor = 0xFF6200EE,
        foregroundColor = 0xFFFFFFFF
    ),
    secondaryAction = WhatsNewSecondaryAction(
        title = "Learn More",
        foregroundColor = 0xFF6200EE,
        action = SecondaryActionType.OpenURL("https://example.com")
    )
)
```

### Version Store

| Store | Description |
|-------|-------------|
| `InMemoryWhatsNewVersionStore` | Stores in memory, for testing |
| `createPersistentWhatsNewVersionStore()` | Factory for platform-specific persistent store (app start, no Pager needed) |
| `createWhatsNewVersionStore()` | `Pager` extension backed by Kuikly's `SharedPreferencesModule` — works on **every** platform including HarmonyOS |
| `KuiklySharedPreferencesWhatsNewVersionStore(prefs)` | Same backend, constructed from an existing `SharedPreferencesModule` |

> HarmonyOS 的 `createPersistentWhatsNewVersionStore()` 是内存实现，需要跨冷启动保留时用
> `createWhatsNewVersionStore()`（页面内）。

---

## 📱 Platform Support

| Platform | 依赖方式 | Status |
|----------|----------|--------|
| Android | JitPack `com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.2` | ✅ 已验证（AAR + Demo APK + 第三方工程） |
| iOS | ① `pod 'KuiklyWhatsNew', '~> 1.2.2'` ② 源码模块 | ✅ 已验证（trunk 发布 + 真实 `pod install` + 第三方工程） |
| HarmonyOS | 源码模块（无 Maven 产物） | ⚠️ 代码就绪，需 2 步手动配置，未实机验证 |
| Web (H5) | JitPack（js klib） | ✅ 已验证（klib + 9 个 node 单测 + 第三方 `jsNodeRun`；H5 壳工程未在本机跑） |
| Mini Programs | JitPack（js klib） | ✅ 已验证（同上 + 真实 bundle 构建；本机无微信开发者工具，未实机预览） |

**HarmonyOS 的两个手动步骤**（详见 [INTEGRATION_zh.md](docs/INTEGRATION_zh.md)）：

```kotlin
whatsNewCurrentVersionOverride = "1.2.0"          // 1. 鸿蒙侧读不到 bundle 版本号
versionStore = createWhatsNewVersionStore()        // 2. 默认实现是内存，页面内改用 Kuikly 模块存储
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [docs/QUICKSTART.md](docs/QUICKSTART.md) | 5 分钟快速上手 |
| [docs/INTEGRATION_zh.md](docs/INTEGRATION_zh.md) | 完整集成教程 |
| [docs/MINIAPP_zh.md](docs/MINIAPP_zh.md) | 微信小程序 / H5 接入指南 |
| [docs/PUBLISHING.md](docs/PUBLISHING.md) | 发布到 Maven Central / JitPack / CocoaPods |
| [docs/VERIFICATION.md](docs/VERIFICATION.md) | 各产物的实际验证记录（编译 / 测试 / 分发 / 第三方接入） |
| [preview/preview.html](preview/preview.html) | 在线预览页面 |

---

## 📄 License

```
MIT License

Copyright (c) 2026 Kuikly WhatsNewKit Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- [Kuikly](https://kuikly.tds.qq.com) — Tencent's cross-platform development framework
- [WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) by SvenTiigi — The original Swift implementation
- [KuiklyUI on GitHub](https://github.com/Tencent-TDS/KuiklyUI) — The open-source Kuikly framework

---

<p align="center">
  Made with ❤️ for the Kuikly community
</p>
