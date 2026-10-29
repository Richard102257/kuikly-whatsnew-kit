// 鸿蒙构建脚本 —— 仅包含 ohos target，避免与 Android/iOS 重复发布
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION_OHOS = "1.0.0_ohos"
}

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
                // 依赖 Kuikly Ohos Render
                api("com.tencent.kuikly-open:core-render-ohos:2.0.0")
            }
        }
    }
}

android {
    namespace = "io.github.Richard102257"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}

publishing {
    repositories {
        mavenLocal()
    }
}
