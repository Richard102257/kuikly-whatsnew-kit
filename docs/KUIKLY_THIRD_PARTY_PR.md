# KuiklyUI-third-party 注册材料

> **当前状态：fork 内 PR 已建；上游 PR 待点一下链接。**
>
> - Fork：https://github.com/Richard102257/KuiklyUI-third-party
> - 分支：`add-whatsnew`（commit `c9a72c9`，只改 `KuiklyUI-Libraries.json`，+15 行）
> - **Fork 内 PR**：https://github.com/Richard102257/KuiklyUI-third-party/pull/1
>   （`add-whatsnew → main`，+15 −0，1 个文件，MERGEABLE）
> - **上游 PR**（真正合并进 KuiklyUI-third-party 的那个）：
>   https://github.com/Tencent-TDS/KuiklyUI-third-party/compare/main...Richard102257:add-whatsnew?expand=1
>
> 上游 PR 为什么没直接用 CLI 建：当前用的 fine-grained PAT 只对本人名下的仓库有写权限，
> 对 `Tencent-TDS/KuiklyUI-third-party` 只能读。`gh pr create` 和 REST `POST /pulls`
> 都返回 `Resource not accessible by personal access token (403)`。
> 换成 classic token（`public_repo`）就能一条命令提交，或者直接点上面的链接。
>
> 注意：fork 里的 PR 只是自己仓库内的分支合并，**不会通知 Tencent-TDS**，
> 上游维护者看不到。要让库出现在官方目录里，必须再开一次指向上游的 PR。

> 补丁见 [`docs/third-party/0001-Add-WhatsNew-to-KuiklyUI-Libraries.patch`](third-party/0001-Add-WhatsNew-to-KuiklyUI-Libraries.patch)
> （与分支上的 commit 一致，JSON 校验通过，共 114 个条目）

## 0. 提交步骤

```bash
# fork 已存在（2026-10-31 创建），直接复用
git clone https://github.com/Richard102257/KuiklyUI-third-party.git
cd KuiklyUI-third-party
git checkout add-whatsnew

# 如需重来：从 main 新建分支并应用补丁
# git checkout -b add-whatsnew origin/main
# git am /path/to/0001-Add-WhatsNew-to-KuiklyUI-Libraries.patch
```

然后点这个链接创建**上游** PR（标题与描述见第 2、3 节）：

https://github.com/Tencent-TDS/KuiklyUI-third-party/compare/main...Richard102257:add-whatsnew?expand=1

（fork 内的 PR #1 已由 CLI 建好，只用于自己 review，不等于提交给上游。）

## 1. 添加到 `KuiklyUI-Libraries.json`

在文件末尾添加：

```json
{
  "githubUrl": "https://github.com/Richard102257/kuikly-whatsnew-kit",
  "examples": [
    "https://github.com/Richard102257/kuikly-whatsnew-kit"
  ],
  "componentName": "WhatsNew",
  "componentDescription": "Kuikly 跨端「新功能引导」组件（SvenTiigi/WhatsNewKit 移植），支持自动/手动展示、版本追踪与 patch 回退、自定义布局、主次按钮，提供 Kuikly DSL 与 Compose 页面两种用法",
  "componentType": "KuiklyView",
  "developer": "Richard102257",
  "Android": true,
  "iOS": true,
  "Ohos": true,
  "Js": true,
  "sDynamic": false
}
```

> 组件是纯 Kuikly DSL（`componentType: KuiklyView`），不含平台侧原生代码。
> `Js: true` 的依据是 JitPack 上有 `whatsnew-js-1.3.0.klib`（H5 / 小程序都用这份产物）；
> `Ohos: true` 的依据是 `ohosArm64` klib 和完整 HAP 都能编出来（`ohosApp/`），
> 但**没有实机运行过**（Intel Mac 无官方模拟器、无真机、无签名账号），PR 描述里写明了。
> `sDynamic: false`：jsMain 依赖 `localStorage` / `wx.setStorageSync`，没按动态模式验证过。

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
implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.3.0")
```

iOS 也可通过 CocoaPods 引入预编译 XCFramework：

```ruby
pod 'KuiklyWhatsNew', '~> 1.3.0'
```

> iOS 的 Kotlin/Native klib 只能在 macOS 上编译，所以 iOS 推荐把 `whatsnew`
> 作为源码模块（submodule）引入；CocoaPods 提供的是预编译二进制分发方式。

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

// 在 Kuikly DSL 页面中：宿主界面 + 弹层（遮罩 + 居中卡片）
override fun body(): ViewBuilder = {
    val pending = environment.whatsNew()
    addChild(HomeContent()) { }
    if (pending != null) {
        addChild(WhatsNewOverlay()) {
            present(pending) { environment.markAsPresented(pending.version) }
        }
    }
}
```

### 平台支持
- Android：JitPack AAR（`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.3.0`）
- iOS：源码模块（推荐）/ CocoaPods 预编译 XCFramework（`pod 'KuiklyWhatsNew'`）
- HarmonyOS：同一份 Kuikly DSL 代码，`./gradlew -c settings.ohos.gradle.kts :whatsnew:assemble`
  （KBA Kotlin + `core:...-ohos` + ohpm `@kuikly-open/render`；HAP 构建见 `ohosApp/`）
```

## 4. 提交前检查

- [x] JitPack 上 `1.3.0` 构建成功（`whatsnew-1.3.0.pom` / `whatsnew-android-1.3.0.aar` 可下载）
- [x] GitHub Release `v1.3.0` 上有 `WhatsNew.xcframework.zip`
- [x] `pod spec lint KuiklyWhatsNew.podspec` 通过，且已发布到 trunk（`pod 'KuiklyWhatsNew', '~> 1.3.0'`）
- [x] README 中的版本号与 tag 一致
- [x] fork 已建、分支 `add-whatsnew` 已推（`compare` 显示 `ahead 1`，只改 1 个文件）
- [ ] PR 已提交（**待网页确认**：fine-grained PAT 无权限对 Tencent-TDS 建 PR）
