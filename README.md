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

### JitPack（推荐）

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
                // KMP 跨端模块（必须）
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.0.0")
            }
        }
        val androidMain by getting {
            dependencies {
                // Android 平台侧实现
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnewAndroid:1.0.0")
            }
        }
    }
}
```

### iOS（CocoaPods）

```ruby
# Podfile
pod 'WhatsNew', '~> 1.0.0'          # KMP 跨端 Framework
pod 'whatsnewIOS', '~> 1.0.0'       # iOS 平台侧原生实现
```

### HarmonyOS

```kotlin
kotlin {
    sourceSets {
        val ohosArm64Main by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.0.0")
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnewOhos:1.0.0_ohos")
            }
        }
    }
}
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
├── whatsnew/              # KMP 跨端模块（Android/iOS/Ohos/Web）
│   ├── build.gradle.kts   # 主构建脚本（Android + iOS + Ohos）
│   ├── build.ohos.gradle.kts  # 鸿蒙独立构建
│   └── src/
│       ├── commonMain/    # 跨端接口 + 数据模型
│       ├── androidMain/   # Android 平台实现
│       ├── iosMain/       # iOS 平台实现
│       └── ohosArm64Main/ # 鸿蒙平台实现
│
├── whatsnewAndroid/       # Android 平台侧独立模块（AAR 产物）
├── whatsnewIOS/           # iOS 平台侧独立模块 + podspec
├── whatsnewOhos/          # 鸿蒙平台侧独立模块（SO 产物）
│
├── androidApp/            # Android Demo 工程
├── iosApp/                # iOS Demo 工程
├── ohosApp/               # 鸿蒙 Demo 工程
├── h5App/                 # Web Demo
├── miniApp/               # 小程序 Demo
├── shared/                # Demo 共享模块
│
├── docs/                  # 文档
├── preview/               # 在线预览
└── .github/               # CI/CD + PR 模板
```

### Module Dependencies

```
whatsnew (KMP 跨端)
├── whatsnewAndroid (Android 平台侧) ← 用户按需引入
├── whatsnewIOS (iOS 平台侧) ← 用户通过 CocoaPods 引入
├── whatsnewOhos (鸿蒙平台侧) ← 用户按需引入
│
androidApp → whatsnew + whatsnewAndroid
iosApp → whatsnew + whatsnewIOS
ohosApp → whatsnew + whatsnewOhos
```

---

## 🚀 Quick Start

### 1. Installation

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.Richard102257:whatsnew:1.0.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.github.Richard102257:whatsnewAndroid:1.0.0")
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
import com.kuikly.core.views.DivView

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
