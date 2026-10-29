# Kuikly WhatsNewKit

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Kuikly](https://img.shields.io/badge/Kuikly-2.0-green.svg)](https://kuikly.tds.qq.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20HarmonyOS%20%7C%20Web%20%7C%20Mini%20Programs-orange.svg)](https://kuikly.tds.qq.com)

> 🎊 基于 Kuikly 跨端框架的 WhatsNewKit 实现，让你的新功能展示跨所有平台！

A fully cross-platform implementation of [WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) built on [Kuikly](https://kuikly.tds.qq.com) — Tencent's high-performance, full-platform development framework. Showcase your awesome new app features across **Android, iOS, HarmonyOS, Web, and Mini Programs** with a single codebase.

Inspired by [SvenTiigi/WhatsNewKit](https://github.com/SvenTiigi/WhatsNewKit) — the popular Swift Package for presenting app changelogs.

---

## 📦 安装

### 单一依赖（推荐）

库是**纯 Kuikly DSL 组件**（`componentType: KuiklyView`），只依赖一个模块即可在
Android / iOS / HarmonyOS / Web / 小程序上工作，无需任何平台侧代码。

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

> Android 端由 JitPack 直接产出 AAR + klib；iOS 端使用 CocoaPods 分发的预编译
> XCFramework（见下），或直接用 Maven 上的 iOS klib。

### iOS（CocoaPods，预编译 XCFramework）

```ruby
# Podfile
pod 'WhatsNew', '~> 1.1.1'
```

podspec 指向 GitHub Release 上的 `WhatsNew.xcframework.zip`，由 GitHub Actions
在 macOS runner 上构建（Kotlin/Native 的 iOS 产物无法在 Linux 上构建）。

### HarmonyOS

```kotlin
// 与 Android 相同的 Maven 依赖；鸿蒙侧仅需 expect/actual 提供的版本号与存储实现
implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.1")
```

> 📖 详细集成教程请查看 [docs/INTEGRATION_zh.md](docs/INTEGRATION_zh.md)
> 🚀 5 分钟快速上手请查看 [docs/QUICKSTART.md](docs/QUICKSTART.md)

---

## ✨ Features

- ✅ **One codebase, all platforms** — Android, iOS, HarmonyOS, Web, Mini Programs
- ✅ **Dual DSL support** — Compose DSL (Jetpack Compose-compatible) and Kuikly Self-developed DSL
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
import io.github.Richard102257.*

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

```kotlin
import io.github.Richard102257.*
import com.tencent.kuikly.core.views.DivView

class MyPager : Pager() {
    override fun createRenderView() {
        super.createRenderView()

        val whatsNewView = WhatsNewView()
        whatsNewView.setup(whatsNew, onDismiss = { /* handle dismiss */ })

        getRenderView().addSubView(whatsNewView)
    }
}
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
