# Kuikly WhatsNewKit

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Kuikly](https://img.shields.io/badge/Kuikly-2.1.1-green.svg)](https://kuikly.tds.qq.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20HarmonyOS%20%7C%20Web%20%7C%20Mini%20Programs-orange.svg)](https://kuikly.tds.qq.com)

> 🎊 基于 Kuikly 跨端框架的 WhatsNewKit 实现，让你的新功能展示跨所有平台！

A fully cross-platform implementation of [WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) built on [Kuikly](https://kuikly.tds.qq.com) — Tencent's high-performance, full-platform development framework. Showcase your awesome new app features across **Android, iOS, HarmonyOS, Web, and Mini Programs** with a single codebase.

Inspired by [SvenTiigi/WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) — the popular Swift Package for presenting app changelogs.

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
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.1")
            }
        }
    }
}
```

> JitPack 在 Linux 上构建，只能产出 **Android AAR + 公共元数据**。
> Kotlin/Native 的 iOS klib 必须在 macOS 上编译，所以 iOS 走下面两种方式之一。

### iOS

**方式 A：源码模块（推荐，与 Kuikly 官方示例一致）**

```kotlin
// settings.gradle.kts
include(":whatsnew")
project(":whatsnew").projectDir = file("../kuikly-whatsnew-kit/whatsnew")
```

在 macOS 上构建时 iOS target 自动注册，klib 随 App 一起编译。

**方式 B：CocoaPods 预编译 XCFramework**

```ruby
# Podfile
pod 'WhatsNew', '~> 1.1.1'
```

podspec 指向 GitHub Release 上的 `WhatsNew.xcframework.zip`（`ios-arm64` +
`ios-arm64_x86_64-simulator`），由 macOS 上的 `./gradlew :whatsnew:buildXCFramework`
构建后上传。适合二进制分发 / 非 Kotlin 工程集成。

**方式 C：Maven Central（可选，需在 macOS 上发布）**

在 macOS 上用 Sonatype 凭据执行 `./gradlew :whatsnew:publish`，会把 **iOS klib**
一起发布，之后 iOS 也能像 Android 一样直接用 Maven 坐标。

### HarmonyOS

鸿蒙 target 需要 Kuikly 的 `ohosArm64` 目标，使用 `whatsnew/build.ohos.gradle.kts`
在 macOS / 鸿蒙工具链上构建，或同样走源码模块方式引入。

> 📖 详细集成教程请查看 [docs/INTEGRATION_zh.md](docs/INTEGRATION_zh.md)
> 🚀 5 分钟快速上手请查看 [docs/QUICKSTART.md](docs/QUICKSTART.md)

---

## ✨ Features

- ✅ **One codebase, all platforms** — Android, iOS, HarmonyOS, Web, Mini Programs
- ✅ **Kuikly DSL + Compose 页面** — `WhatsNewView` 用于 Kuikly DSL；Compose 页面用 `WhatsNewComposeState` 控制展示
- ✅ **Manual & Automatic presentation** — Full control or let the environment handle it
- ✅ **Version tracking** — Persistent, in-memory, and iCloud-like storage options
- ✅ **Patch version fallback** — Automatically falls back to nearest matching version
- ✅ **Fully customizable layout** — Adjust spacing, padding, colors, and more
- ✅ **Primary & Secondary actions** — Dismiss, open URLs, or execute custom actions
- ✅ **Native performance** — Uses Kuikly's native rendering, not Skia
- ✅ **Lightweight** — Minimal footprint, no heavy dependencies
- ✅ **Dynamic capability** — Supports hot updates via Kuikly Dynamic

---

## 📦 Project Structure

```
kuikly-whatsnew-kit/
├── whatsnew/                 # 唯一的库模块（KMP，纯 Kuikly DSL）
│   ├── build.gradle.kts      # Android + iOS 目标（iOS 仅 macOS）
│   ├── build.ohos.gradle.kts # 鸿蒙独立构建脚本
│   └── src/
│       ├── commonMain/       # 数据模型 + DSL View + 版本匹配逻辑
│       ├── androidMain/      # 版本号 + SharedPreferences
│       ├── iosMain/          # NSBundle + NSUserDefaults
│       └── ohosArm64Main/    # 鸿蒙 Preferences
│
├── WhatsNew.podspec          # iOS 分发（预编译 XCFramework）
├── androidApp/ + shared/     # Android Demo
├── iosApp/ ohosApp/ h5App/ miniApp/  # 其他平台 Demo 工程
├── docs/                     # 文档（CI 工作流模板在 docs/ci/）
└── preview/                  # 在线预览
```

平台差异只有两件事：**读取 App 版本号** 和 **持久化已展示版本**，
通过 `expect/actual` 实现，每个平台约 25 行。

---

## 🚀 Quick Start

### 1. Installation

```kotlin
// build.gradle.kts
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
// 方式 A：直接创建
val page = WhatsNewPage(environment)

// 方式 B：设置一次全局环境，之后按名字路由打开
defaultWhatsNewEnvironment = environment
router.openPage("WhatsNewPage")
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
| `createPersistentWhatsNewVersionStore()` | Factory for platform-specific persistent store |

---

## 📱 Platform Support

| Platform | Status |
|----------|--------|
| Android | ✅ Full |
| iOS | ✅ Full |
| HarmonyOS | ✅ Full |
| Web (H5) | ✅ Beta |
| Mini Programs | ✅ Beta |

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [docs/QUICKSTART.md](docs/QUICKSTART.md) | 5 分钟快速上手 |
| [docs/INTEGRATION_zh.md](docs/INTEGRATION_zh.md) | 完整集成教程 |
| [docs/PUBLISHING.md](docs/PUBLISHING.md) | 发布到 Maven Central / JitPack / CocoaPods |
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
