# KuiklyUI-third-party 注册材料

## 1. 添加到 `KuiklyUI-Libraries.json`

在文件末尾添加：

```json
{
  "githubUrl": "https://github.com/Richard102257/kuikly-whatsnew-kit",
  "examples": [],
  "componentName": "WhatsNew",
  "componentDescription": "A cross-platform \"What's New\" component for KuiklyUI, inspired by SvenTiigi/WhatsNewKit. Pure Kuikly DSL: automatic/manual presentation, version tracking, patch-version fallback, customizable layout, primary and secondary actions.",
  "componentType": "KuiklyView",
  "developer": "Richard102257",
  "Android": true,
  "iOS": true,
  "Ohos": true,
  "Js": false,
  "Macos": false,
  "Linux": false,
  "Windows": false,
  "Tvos": false
}
```

> 组件是纯 Kuikly DSL（`componentType: KuiklyView`），不含平台侧原生代码，
> 因此 Android / iOS / Ohos 均为 true，且同一份产物通用。
> `Js` 暂为 false（未声明 js target）。

## 2. PR 标题

```
Add WhatsNew to KuiklyUI-Libraries.json
```

## 3. PR 描述

```markdown
## WhatsNew — cross-platform "What's New" component for KuiklyUI

### 功能
- 自动 / 手动展示
- 版本追踪 + 持久化（不会重复弹出）
- patch 版本回退（2.1.3 找不到时回退到 2.1.0）
- 最新未展示版本自动匹配
- 可自定义布局（内边距、间距、按钮高度）
- 主按钮 / 次按钮（支持打开 URL、自定义回调）
- 纯 Kuikly DSL 实现，无平台侧原生代码

### 引入方式

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.0")
```

iOS 也可通过 CocoaPods 引入预编译 XCFramework：

```ruby
pod 'WhatsNew', '~> 1.1.0'
```

### 使用示例

```kotlin
val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = whatsNewCollection {
        whatsNew("2.0.0") {
            title("What's New in v2.0")
            feature(
                icon = WhatsNewIcon.SystemIcon("star.fill"),
                title = "AI-Powered Development",
                subtitle = "Generate boilerplate with a single prompt."
            )
            primaryAction { title = "Continue" }
        }
    }
)

// 在 Kuikly DSL 页面中
override fun body(): ViewBuilder = {
    addChild(WhatsNewView()) {
        setup(environment.whatsNew())
    }
}
```

### 平台支持
- Android：JitPack AAR
- iOS：CocoaPods 预编译 XCFramework（GitHub Actions 在 macOS 构建）
- HarmonyOS：KMP klib
```

## 4. 提交前检查

- [ ] JitPack 上 `v1.1.0` 构建成功
- [ ] GitHub Release 上有 `WhatsNew.xcframework.zip`
- [ ] `pod spec lint WhatsNew.podspec` 通过
- [ ] README 中的版本号与 tag 一致
