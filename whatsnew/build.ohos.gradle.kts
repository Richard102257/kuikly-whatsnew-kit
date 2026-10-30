// 鸿蒙构建脚本 —— 仅包含 ohos target，避免与 Android/iOS 重复发布
//
// 前置条件（缺一不可，本机未验证）：
//   1. 使用 Kuikly 定制的 Kotlin 发行版（2.0.21-KBA-010），标准 Kotlin 没有 ohosArm64 目标
//   2. 宿主的鸿蒙工程自带 OHOS 渲染层（core-render-ohos 由 KuiklyUI 仓库的
//      C++ / ArkTS 源码构建，**没有发布到 Maven Central 或 Tencent 镜像**），
//      所以这里只依赖 DSL 层 `core`，render 层由宿主提供 —— 与 Android / iOS 的分工一致
//
// 用法：cp whatsnew/build.ohos.gradle.kts whatsnew/build.gradle.kts && ./gradlew :whatsnew:assemble
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION_OHOS = "1.2.0_ohos"
}

// 与 whatsnew/build.gradle.kts 保持一致
val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.4.0-2.0.21"

version = MavenConfig.VERSION_OHOS
group = MavenConfig.GROUP

kotlin {
    // 仅鸿蒙 Target
    ohosArm64 {
        binaries.sharedLib {
        }
    }

    sourceSets {
        val commonMain by getting
        val ohosArm64Main by getting {
            dependencies {
                // 只依赖 Kuikly DSL 层；OHOS 渲染层由宿主的鸿蒙工程提供
                api("com.tencent.kuikly-open:core:$kuiklyVersion")
            }
        }
    }
}

android {
    namespace = "io.github.Richard102257.whatsnew"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}

publishing {
    repositories {
        mavenLocal()
    }
}
