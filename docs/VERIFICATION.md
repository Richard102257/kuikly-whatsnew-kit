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
| 单元测试 | `./gradlew :whatsnew:testDebugUnitTest` | ✅ 10 tests / 0 failures |

单元测试覆盖：版本号解析（含 `2`、`2.3`、`abc`、空串）、版本比较、`InMemoryWhatsNewVersionStore`、
DSL Builder 产物（title / features / primaryAction / secondaryAction / layout）、集合顺序。

## 2. Android 分发（JitPack）

- 坐标：`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.1`
- 实测 HTTP 200：`whatsnew-1.1.1.pom`、`whatsnew-1.1.1.module`、`whatsnew-android-1.1.1.aar`

**第三方接入实测**：新建独立 KMP 工程，只声明上面的 JitPack 依赖（`androidMain`），代码里使用
`whatsNewCollection {}`、`WhatsNewView().setup(...)`、`WhatsNewEnvironment`、
`createPersistentWhatsNewVersionStore()`、`ViewContainer<*, *>.addChild(...)` ——
`:compileDebugKotlinAndroid` BUILD SUCCESSFUL。

## 3. Demo 工程

- `:androidApp:assembleDebug` → ✅ `androidApp-debug.apk`（6.4 MB）
- `:shared:compileKotlinIosArm64`（Demo 共享代码依赖 `project(":whatsnew")`）→ ✅ BUILD SUCCESSFUL

## 4. iOS 分发

### 4.1 XCFramework

`./gradlew :whatsnew:assembleWhatsNewReleaseXCFramework`（macOS + Xcode 16.2）→ ✅ 9m41s

```
WhatsNew.xcframework
├── ios-arm64/WhatsNew.framework                     (arm64, 静态)
└── ios-arm64_x86_64-simulator/WhatsNew.framework    (x86_64 + arm64, 静态)
```

`LC_BUILD_VERSION`: `platform 2 / minos 12.0 / sdk 18.2`；`Info.plist` 为 `XFWK 1.0`。

打包：`WhatsNew.xcframework.zip`（5.2 MB，含 `LICENSE`），
sha256 `dfaf9f0762bf18be936d693d07d0e9a240ea0f6bd0cffabdea61ae0a4d031a15`，
挂在 GitHub Release `v1.1.1`，下载 URL 实测可拉全量（`http=200 size=5419733`）。

### 4.2 CocoaPods

| 检查项 | 结果 |
|--------|------|
| `pod spec lint KuiklyWhatsNew.podspec --quick --allow-warnings` | ✅ passed validation |
| `pod spec lint KuiklyWhatsNew.podspec --allow-warnings`（完整编译 + 链接） | ✅ passed validation |
| `pod trunk push`（实际发布） | ✅ `KuiklyWhatsNew 1.1.1`，2026-10-29 16:31 UTC |
| trunk 所有者 | `Richard <lhui1022@gmail.com>` |
| CDN 上可读到 podspec | ✅ `https://cdn.cocoapods.org/Specs/a/6/0/KuiklyWhatsNew/1.1.1/KuiklyWhatsNew.podspec.json` |

pod 名说明：`WhatsNew` 在 trunk 上已被 `me@patrickbalestra.com` 占用，故发布名为
`KuiklyWhatsNew`；framework 名与代码 `import` 不受影响。

## 5. 环境备注

- 本机 Xcode 16.2 出厂未带 iOS 平台，需先 `xcodebuild -downloadPlatform iOS`（约 8.7 GB）。
  未安装时 `pod spec lint` 会报
  `iOS 18.2 is not installed. To use with Xcode, first download and install the platform`。
- JitPack 在 Linux 上构建，只能产出 Android + 公共元数据；iOS klib / XCFramework 必须在 macOS 上构建。
- `OpenKuiklyIOSRender` 依赖在本地 lint 时通过 `git url.insteadOf` 指向一份
  `core-render-ios` 子集仓库，以规避 `github.com` 拉取超时；正式 lint / push 使用 trunk 上的 2.30.0。
