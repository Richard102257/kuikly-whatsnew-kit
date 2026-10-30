# iOS Demo

最小化的 Kuikly 宿主：一个 `UIViewController` + `KuiklyRenderViewControllerBaseDelegator`，
页面内容由 Kotlin/Native `shared.framework` 渲染（`@Page("WhatsNewDemoPage")` 定义在
`shared/src/iosMain`）。

## 跑起来

```bash
# 1. 生成 podspec + 编模拟器 framework
#    Intel Mac 用 x86_64，Apple Silicon 用 arm64
./gradlew :shared:podspec :shared:syncFramework \
  -Pkotlin.native.cocoapods.platform=iphonesimulator \
  -Pkotlin.native.cocoapods.archs=x86_64 \
  -Pkotlin.native.cocoapods.configuration=Debug

# 2. 生成 Xcode 工程 + 装 pod
ruby iosApp/generate_project.rb
cd iosApp
KUIKLYUI_PATH=/path/to/KuiklyUI pod install

# 3. 打开 iosApp.xcworkspace 运行（或 xcodebuild）
open iosApp.xcworkspace
```

`KUIKLYUI_PATH` 指向 KuiklyUI 源码仓库根目录，`OpenKuiklyIOSRender` 是从源码编译的 pod。
`pod install` 后 `Build shared` 脚本阶段会自动跑 `:shared:syncFramework`；已经手动编过的话
可以 `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` 跳过。

## 截图

`iosAppUITests` 里有一个 UI 测试，会真点一下页面底部的「继续」，把 `1.2.0` 写进
`NSUserDefaults`；之后再启动 App 就是「已经是最新版本」状态。完整命令见
[`docs/VERIFICATION.md`](../docs/VERIFICATION.md#351-复现步骤)。
