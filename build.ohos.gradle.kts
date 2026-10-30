// 鸿蒙构建的根构建脚本
//
// 只在 `-c settings.ohos.gradle.kts` 时生效（settings 里把 rootProject.buildFileName 指到这里）。
// 默认的 build.gradle.kts 是 Android / iOS / JS 的构建，含 com.android.* 插件，
// 鸿蒙构建不需要也不应该引入它。
//
// Kotlin 使用 Kuikly 定制的 KBA 发行版：标准 Kotlin 没有 `ohosArm64` 目标。
// 制品（gradle 插件 / native 预编译包）都在腾讯 Maven 镜像上。
plugins {
    kotlin("multiplatform") version "2.0.21-KBA-010" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
