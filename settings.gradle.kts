rootProject.name = "kuikly-whatsnew-kit"

// ─── 扩展库模块（独立发布） ───
include(":whatsnew")              // KMP 跨端模块（Android AAR + iOS Framework + Ohos SO）
include(":whatsnewAndroid")       // Android 平台侧原生实现
include(":whatsnewIOS")           // iOS 平台侧原生实现（CocoaPods）
include(":whatsnewOhos")          // 鸿蒙平台侧原生实现

// ─── Demo 工程（不发布） ───
include(":androidApp")            // Android Demo
include(":shared")                // Demo 共享模块
