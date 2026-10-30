// 鸿蒙构建专用 settings
//
// 用法：
//   ./gradlew -c settings.ohos.gradle.kts :whatsnewOhos:linkSharedDebugSharedOhosArm64
//
// 与默认 settings.gradle.kts 的区别：
//   1. Kotlin 插件从腾讯 Maven 镜像取 KBA 发行版（2.0.21-KBA-010）
//   2. `:whatsnew` 用 build.ohos.gradle.kts（只有 ohosArm64 目标）
//   3. 只包含鸿蒙相关模块，不加载 Android / iOS / JS 目标
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/repository/maven-tencent/") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/repository/maven-tencent/") }
    }
}

rootProject.name = "kuikly-whatsnew-kit"
rootProject.buildFileName = "build.ohos.gradle.kts"

// 库：只编 ohosArm64（Kuikly DSL 层）
include(":whatsnew")
project(":whatsnew").buildFileName = "build.ohos.gradle.kts"

// Demo：编译成 libshared.so，被 ohosApp 的 NAPI 层链接
include(":whatsnewOhos")
