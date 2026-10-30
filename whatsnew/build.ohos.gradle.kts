// 鸿蒙构建脚本 —— 仅包含 ohosArm64 目标，避免与 Android/iOS/JS 重复发布
//
// 前置条件：
//   1. Kuikly 定制的 Kotlin 发行版（2.0.21-KBA-010）—— 标准 Kotlin 没有 ohosArm64 目标
//   2. Kuikly 的 ohos 版 DSL 层：`com.tencent.kuikly-open:core:<version>-ohos`
//      （普通版本不含 ohosArm64 变体，Maven Central 上两者并存）
//
// 渲染层不在这个模块里：`@kuikly-open/render`（ohpm 包）由宿主的鸿蒙工程引入，
// 与 Android 引 `core-render-android`、iOS 引 `core-render-ios` 的分工一致。
//
// 用法（不需要替换 build.gradle.kts）：
//   ./gradlew -c settings.ohos.gradle.kts :whatsnew:assemble
plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION_OHOS = "1.3.0_ohos"
}

// Kuikly 的 ohos 版 DSL 层（带 -ohos 后缀）
val kuiklyOhosVersion: String =
    (project.findProperty("kuiklyOhosVersion") as String?) ?: "2.4.0-2.0.21-ohos"

version = MavenConfig.VERSION_OHOS
group = MavenConfig.GROUP

kotlin {
    ohosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core:$kuiklyOhosVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}
