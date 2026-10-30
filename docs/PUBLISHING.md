# 发布指南

本库只有一个模块 `:whatsnew`（纯 Kuikly DSL 组件），各平台产物与发布方式如下。

## 产物矩阵

| 平台 | 产物 | 构建环境 | 分发渠道 |
|------|------|----------|----------|
| Android | AAR + klib | Linux / macOS | JitPack |
| 公共元数据 | klib | Linux / macOS | JitPack |
| iOS | XCFramework | **仅 macOS** | CocoaPods（预编译）+ GitHub Release |
| HarmonyOS | klib / .so | Kuikly ohos 工具链 | Maven / 手动 |

> ⚠️ Kotlin/Native 的 iOS 产物无法在 Linux（JitPack）上构建，必须使用 macOS runner。
> 这就是 iOS 走「GitHub Actions 构建 XCFramework → CocoaPods 分发」的原因。

---

## 一、Android / 公共元数据（JitPack）

仓库已配置 `jitpack.yml`：

```yaml
jdk:
  - openjdk17
install:
  - ./gradlew :whatsnew:publishToMavenLocal -x test --no-daemon
```

发布步骤：

```bash
git tag v1.2.0
git push origin v1.2.0
```

然后访问 `https://jitpack.io/#Richard102257/kuikly-whatsnew-kit/v1.2.0` 触发构建。

用户侧引用：

```kotlin
implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.0")
```

本地验证构建：

```bash
./gradlew :whatsnew:assembleRelease :whatsnew:publishToMavenLocal

# 单元测试（纯逻辑：版本解析/比较、版本存储、DSL Builder）
./gradlew :whatsnew:testDebugUnitTest
```

---

## 二、iOS（CocoaPods + 预编译 XCFramework）

### 1. 构建 XCFramework

在 macOS 上：

```bash
./gradlew :whatsnew:assembleWhatsNewXCFramework
# 产物：whatsnew/build/XCFrameworks/release/WhatsNew.xcframework
# 或简写：./gradlew :whatsnew:buildXCFramework
```

打包并上传到 GitHub Release：

```bash
cd whatsnew/build/XCFrameworks/release
zip -qry WhatsNew.xcframework.zip WhatsNew.xcframework

gh release create v1.2.0 WhatsNew.xcframework.zip \
  --title "v1.2.0" --notes "WhatsNew 1.2.0"
```

或由 CI 自动完成（`.github/workflows/release-ios.yml`，模板见 `docs/ci/release-ios.yml`）：
推送 `v*` tag 时，macOS runner 构建并打包 `WhatsNew.xcframework.zip` 上传到 GitHub Release。

### 2. podspec

`KuiklyWhatsNew.podspec` 使用预编译产物：

```ruby
spec.source = {
  :http => "https://github.com/Richard102257/kuikly-whatsnew-kit/releases/download/v#{spec.version}/WhatsNew.xcframework.zip"
}
spec.vendored_frameworks = 'WhatsNew.xcframework'
```

> 注意：**不能**把 `.kt` 源码放进 `source_files`。CocoaPods / Xcode 不编译 Kotlin，
> 必须分发 Kotlin/Native 编译出的 framework。

> pod 名为什么是 `KuiklyWhatsNew` 而不是 `WhatsNew`：trunk 上 `WhatsNew` 已被其他作者
> 占用（`pod trunk push WhatsNew.podspec` 会返回 *not allowed to push new versions for
> this pod*）。pod 名与 framework 名（`WhatsNew.framework`）互不影响，代码里的
> `import WhatsNew` / `import io.github.Richard102257.whatsnew.*` 不变。

### 3. 发布到 CocoaPods Trunk

前提：

- GitHub Release 上已存在对应版本的 `WhatsNew.xcframework.zip`（zip 内含 `LICENSE`，
  podspec 的 `license[:file]` 才能在下载目录里找到）
- 本机 Xcode **已安装 iOS 平台**（`xcrun simctl list runtimes` 至少有一个 iOS runtime）。
  只装了 Xcode 但没下平台时，`xcodebuild` 会报
  `iOS xx.x is not installed. To use with Xcode, first download and install the platform`，
  `pod spec lint` / `pod trunk push` 都会失败（它们要真正编译并链接一个示例 App）。
  安装方式：`xcodebuild -downloadPlatform iOS`（约 8.7 GB）。

```bash
export PATH="$HOME/.rbenv/versions/3.4.11/bin:$PATH"
export LANG=en_US.UTF-8

# 首次需要注册（已验证邮箱会收到确认链接）
pod trunk register lhui1022@gmail.com 'Richard'

# 快速语法检查（不编译，几秒）
pod spec lint KuiklyWhatsNew.podspec --quick --allow-warnings

# 完整校验（下载 XCFramework + 编译示例 App），建议先跑通再 push
pod spec lint KuiklyWhatsNew.podspec --allow-warnings

# 正式发布（同样会做一次完整校验）
pod trunk push KuiklyWhatsNew.podspec --allow-warnings
```

`pod trunk push` 没有 `--quick` / `--skip-build` 之类的开关，本地校验不通过就无法发布。

---

## 三、HarmonyOS

鸿蒙目标需要 Kuikly 提供的 `ohosArm64` Kotlin 目标，使用独立构建脚本：

```bash
cp whatsnew/build.ohos.gradle.kts whatsnew/build.gradle.kts
./gradlew :whatsnew:assemble
git checkout whatsnew/build.gradle.kts
```

产物为 `.so` / klib，可发布到 Maven 或随宿主工程一起构建。

---

## 四、版本号统一位置

发布前需要同步修改：

| 文件 | 字段 |
|------|------|
| `gradle.properties` | `VERSION=` |
| `whatsnew/build.gradle.kts` | `MavenConfig.VERSION` / `VERSION_OHOS` |
| `KuiklyWhatsNew.podspec` | `spec.version` |
| `docs/KUIKLY_THIRD_PARTY_PR.md` | 示例版本号 |

---

## 五、检查清单

- [ ] `./gradlew :whatsnew:assembleRelease` 通过
- [ ] JitPack 上 `vX.Y.Z` 构建成功（绿色）
- [ ] GitHub Release 上有 `WhatsNew.xcframework.zip`
- [ ] `pod spec lint KuiklyWhatsNew.podspec` 通过
- [ ] `pod trunk push` 成功，`pod search WhatsNew` 可查到
- [ ] 更新 `docs/KUIKLY_THIRD_PARTY_PR.md` 并向 KuiklyUI-third-party 提交 PR
