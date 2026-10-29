# KuiklyUI-third-party 注册 PR 信息

## 需要添加到 KuiklyUI-Libraries.json 的内容

在文件末尾添加以下 JSON 条目：

```json
{
  "githubUrl": "https://github.com/Richard102257/kuikly-whatsnew-kit",
  "examples": [],
  "componentName": "WhatsNewKit",
  "componentDescription": "A cross-platform WhatsNew library for KuiklyUI, inspired by SvenTiigi/WhatsNewKit. Supports automatic/manual presentation, version tracking, customizable layout with primary/secondary actions.",
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

## PR 标题
Add WhatsNewKit to KuiklyUI-Libraries.json

## PR 描述
```markdown
## WhatsNewKit - Cross-platform What's New component for KuiklyUI

### Features
- ✅ Automatic version detection and presentation
- ✅ Manual presentation with customizable content
- ✅ Version tracking with persistent storage
- ✅ Patch version fallback support
- ✅ Customizable layout (spacing, button height, padding)
- ✅ Primary and secondary actions with URL opening
- ✅ Kuikly DSL and Compose DSL support

### Platforms
- Android (via JitPack)
- iOS (via CocoaPods)
- Ohos (via Maven Central)

### Installation
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

// build.gradle.kts (commonMain)
implementation("com.github.Richard102257:kuikly-whatsnew-kit:1.0.3")
```

iOS (CocoaPods):
```ruby
pod 'whatsnewIOS', '~> 1.0.0'
```

### Usage
```kotlin
// Create environment
val env = WhatsNewEnvironment()

// Define WhatsNew content
val whatsNew = whatsNew {
    version("2.0.0")
    title("What's New")
    feature(
        icon = WhatsNewIcon.SystemIcon("star.fill"),
        title = "Amazing Feature",
        subtitle = "This is a great new feature."
    )
    primaryAction {
        title = "Continue"
    }
}

// Auto-present
env.presentWhatsNewIfNecessary(whatsNew) { /* dismissed */ }
```
```

## 注意事项
- examples 数组暂时为空，等 Demo 工程完善后补充
- 需要等 JitPack 构建成功后再提交 PR
