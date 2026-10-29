// iOS 平台侧模块
//
// iOS 平台侧代码通过 whatsnewIOS.podspec 发布到 CocoaPods。
// 不需要 Gradle 构建脚本，因为：
// 1. iOS 平台侧代码是纯 Kotlin/Native 代码，由 KMP 编译
// 2. CocoaPods 直接引用源码（whatsnewIOS/Classes/**/*）
// 3. 编译由宿主工程的 Xcode 完成
//
// 发布命令：
//   pod trunk push whatsnewIOS.podspec
// 或：
//   pod repo push your-private-repo whatsnewIOS.podspec
