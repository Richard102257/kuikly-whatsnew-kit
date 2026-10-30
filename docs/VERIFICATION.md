# 验证记录

本文件记录每个产物的**实际验证方式与结果**，用于确认「可编译、可发布、可被第三方接入」。

最后更新：2026-10-30

---

## 1. 库本身

| 检查项 | 命令 | 结果 |
|--------|------|------|
| Android 编译 | `./gradlew :whatsnew:compileDebugKotlinAndroid` | ✅ BUILD SUCCESSFUL |
| Android 发布产物 | `./gradlew :whatsnew:assembleRelease` | ✅ AAR + klib |
| iOS 编译（arm64） | `./gradlew :whatsnew:compileKotlinIosArm64` | ✅ BUILD SUCCESSFUL |
| iOS 编译（模拟器） | `./gradlew :whatsnew:compileKotlinIosSimulatorArm64` / `compileKotlinIosX64` | ✅ |
| **JS 编译** | `./gradlew :whatsnew:compileKotlinJs` | ✅ BUILD SUCCESSFUL |
| 单元测试（JVM/Android） | `./gradlew :whatsnew:testDebugUnitTest` | ✅ 14 tests / 0 failures |
| **单元测试（JS / node）** | `./gradlew :whatsnew:jsNodeTest` | ✅ 23 tests / 0 failures（14 common + 9 platform） |
| 发布到 mavenLocal | `./gradlew :whatsnew:publishToMavenLocal` | ✅ 生成 `whatsnew-js-1.2.0.klib` |

单元测试覆盖：版本号解析（含 `2`、`2.3`、`abc`、空串）、版本比较、`InMemoryWhatsNewVersionStore`、
DSL Builder 产物（title / features / primaryAction / secondaryAction / layout）、集合顺序、
以及 `KuiklySharedPreferencesWhatsNewVersionStore`（跨实例持久化、与原生平台一致的存储键/格式、
`clear()`、重复写入去重）。

JS 单测（`whatsnew/src/jsTest/.../PlatformJsTest.kt`）在 node 里伪造 `wx` / `localStorage`，
覆盖：小程序版本号读取、开发版空版本号与 `trial` 的过滤、覆盖值优先级、三种存储后端
（wx storage / localStorage / 内存兜底）、与原生平台一致的逗号分隔存储格式、
`WhatsNewEnvironment` 的自动展示与去重。

## 2. Android / JS 分发（JitPack）

- 坐标：`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.0`
- JitPack 构建状态：`ok`（tag `v1.2.0`）
- 实测 HTTP 200（1.2.0）：

| 文件 | 大小 |
|------|------|
| `whatsnew/1.2.0/whatsnew-1.2.0.pom` | 1,548 B |
| `whatsnew/1.2.0/whatsnew-1.2.0.module` | 5,498 B（含 `jsApiElements` / `jsRuntimeElements` 变体） |
| `whatsnew-android/1.2.0/whatsnew-android-1.2.0.aar` | 69,551 B（`classes.jar` 75,338 B） |
| `whatsnew-js/1.2.0/whatsnew-js-1.2.0.klib` | 71,296 B |
| `whatsnew-js/1.2.0/whatsnew-js-1.2.0.pom` | 1,782 B |

**Android 第三方接入实测（1.1.1 时完成，API 未变）**：新建独立 KMP 工程，只声明 JitPack 依赖
（`androidMain`），代码里使用 `whatsNewCollection {}`、`WhatsNewView().setup(...)`、
`WhatsNewEnvironment`、`createPersistentWhatsNewVersionStore()`、
`ViewContainer<*, *>.addChild(...)` —— `:compileDebugKotlinAndroid` BUILD SUCCESSFUL。

**JS 第三方接入实测（1.2.0）**：新建独立 KMP 工程，只声明
`implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.0")`
+ `js(IR) { browser(); nodejs(); binaries.executable() }`，
`:jsNodeRun` 输出：

```
current=1.2.0 persistent=false title=What's New
```

即 Gradle 通过 `org.jetbrains.kotlin.platform.type=js` 自动选中 JitPack 上的
`whatsnew-js` klib，不需要任何额外配置。

## 3. Demo 工程

- `:androidApp:assembleDebug` → ✅ `androidApp-debug.apk`（6.72 MB，Kuikly 依赖全部解析到 `2.4.0-2.0.21`）
- `:shared:compileKotlinIosArm64`（Demo 共享代码依赖 `project(":whatsnew")`）→ ✅ BUILD SUCCESSFUL

## 3.1 小程序 / H5

`miniApp/` 是一个真实的 Kotlin/JS 业务模块（Kuikly 插件 + KSP + `@Page` 页面）：

| 检查项 | 命令 | 结果 |
|--------|------|------|
| 业务模块编译 | `./gradlew :miniApp:compileKotlinJs` | ✅ BUILD SUCCESSFUL |
| KSP 页面注册 | 查看 `miniApp/build/generated/ksp/js/jsMain/kotlin/KuiklyCoreEntry.kt` | ✅ 含 `BridgeManager.registerPageRouter("WhatsNewDemoPage")` |
| 打包 bundle | `./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage` | ✅ `nativevue2.zip`（`nativevue2.js` 1,872,375 B） |

产物路径：`miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip`。

> 本机没有微信开发者工具，也没有 Kuikly 官方 `h5App-js` / `miniApp-js` 壳工程的构建产物，
> 因此**未做真机 / 真浏览器渲染验证**。已完成的验证是：库在 JS 工具链上编译通过、
> 逻辑层单测通过、含 `WhatsNewView` 的页面能被 KSP 注册并 webpack 打成 bundle。
> 把该 bundle 放进官方壳工程后即可在微信开发者工具 / 浏览器中预览，步骤见
> [MINIAPP_zh.md](MINIAPP_zh.md)。

## 4. iOS 分发

### 4.1 XCFramework（1.2.0，Kuikly 2.4.0）

`./gradlew :whatsnew:assembleWhatsNewXCFramework`（macOS + Xcode 16.2）→ ✅ 6m42s

```
WhatsNew.xcframework
├── ios-arm64/WhatsNew.framework                     (arm64, 静态)
└── ios-arm64_x86_64-simulator/WhatsNew.framework    (x86_64 + arm64, 静态)
```

打包：`WhatsNew.xcframework.zip`（5,436,328 B），
sha256 `d110422a52c5cc462d73d7946da6a258d895a6f7fd43152d2c709078b4be9695`，
挂在 GitHub Release `v1.2.0`，下载 URL 实测可拉全量且 sha256 一致：

```
https://github.com/Richard102257/kuikly-whatsnew-kit/releases/download/v1.2.0/WhatsNew.xcframework.zip
```

### 4.2 CocoaPods

| 检查项 | 结果 |
|--------|------|
| `pod spec lint KuiklyWhatsNew.podspec --quick --allow-warnings` | ✅ passed validation |
| `pod trunk push KuiklyWhatsNew.podspec --allow-warnings`（本地完整编译 + 链接） | ✅ 通过（xcodebuild 4 targets） |
| 实际发布结果 | ✅ `KuiklyWhatsNew 1.2.0`，2026-10-30 04:02:33 UTC（trunk API 确认） |
| trunk 所有者 | `Richard <lhui1022@gmail.com>` |
| CDN 上可读到 podspec | ✅ `https://cdn.cocoapods.org/Specs/a/6/0/KuiklyWhatsNew/1.2.0/KuiklyWhatsNew.podspec.json` |
| 真实工程 `pod install`（本地 podspec 指向 1.2.0） | ✅ `Installing KuiklyWhatsNew (1.2.0)` + `OpenKuiklyIOSRender (2.30.0)` |
| 真实工程 `pod install '~> 1.2.0'`（**走 trunk / CDN，无本地路径**） | ✅ `Podfile.lock` 里 `KuiklyWhatsNew (1.2.0)`，`Pods/KuiklyWhatsNew/WhatsNew.xcframework` 两个切片就位 |

> `pod trunk push` 命令本身会以 exit 1 结束，报
> `Calling the GitHub commit API timed out` —— 那是 trunk 把 spec 镜像到 GitHub 仓库的
> 后续步骤受 `github.com:443` 超时影响，**版本已经发布成功**（trunk API 的 `versions`
> 里已有 `1.2.0`，CDN 也能取到 spec）。
>
> CDN 的 `all_pods_versions_*.txt` 索引由后台任务定期重建，刚发布后
> `pod install '~> 1.2.0'` 会短暂解析不到（1.2.0 于 04:02 UTC 发布，
> 索引在 06:24 UTC 才包含 `KuiklyWhatsNew/1.1.1/1.2.0`）。这期间可以用本地 podspec 验证：
>
> ```ruby
> pod 'KuiklyWhatsNew', :podspec => './KuiklyWhatsNew.podspec'
> ```
>
> 索引更新后，干净目录 + 仅 Podfile（`pod 'KuiklyWhatsNew', '~> 1.2.0'`）
> 执行 `pod install --repo-update` 已实测成功。

pod 名说明：`WhatsNew` 在 trunk 上已被 `me@patrickbalestra.com` 占用，故发布名为
`KuiklyWhatsNew`；framework 名与代码 `import` 不受影响。

### 4.3 已知小瑕疵（1.2.0）

1.2.0 的 `WhatsNew.xcframework.zip` 只打了 `WhatsNew.xcframework`，**没带 `LICENSE`**，
所以 lint / `pod install` 会打印 `[!] Unable to read the license file 'LICENSE'`
（podspec 里 `license[:file] => 'LICENSE'`）。这是警告，trunk 已正常接受，pod 也能装；
从 1.2.1 起按 [PUBLISHING.md](PUBLISHING.md) 的命令把 `LICENSE` 一起打进 zip。

## 5. 环境备注

- 本机 Xcode 16.2 出厂未带 iOS 平台，需先 `xcodebuild -downloadPlatform iOS`（约 8.7 GB）。
  未安装时 `pod spec lint` 会报
  `iOS 18.2 is not installed. To use with Xcode, first download and install the platform`。
- JitPack 在 Linux 上构建，能产出 Android AAR + JS klib + 公共元数据；
  iOS klib / XCFramework 必须在 macOS 上构建。
- JitPack 构建 js target 需要下载 Node.js / yarn（Kotlin/JS 插件自管理），
  `kotlin-js-store/yarn.lock` 已提交，避免 `Lock file was changed` 失败。
- `OpenKuiklyIOSRender` 依赖在本地 lint 时通过 `git url.insteadOf` 指向一份
  `core-render-ios` 子集仓库，以规避 `github.com` 拉取超时；正式 lint / push 使用 trunk 上的 2.30.0。
- 鸿蒙未验证：`core-render-ohos` **没有发布到 Maven Central，Tencent 镜像上也没有**
  （`maven-metadata.xml` 404），它由 KuiklyUI 仓库的 C++ / ArkTS 源码构建；
  `ohosArm64` 目标还需要 Kuikly 定制的 Kotlin `2.0.21-KBA-010`。
  因此 `whatsnew/build.ohos.gradle.kts` 只声明 DSL 层 `core`，render 层由宿主的鸿蒙工程提供。
