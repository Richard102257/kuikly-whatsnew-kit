import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

// ─── 版本配置 ───
object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION = "1.2.1"
    // 鸿蒙版本加后缀避免 Maven 元数据冲突
    const val VERSION_OHOS = "1.2.1_ohos"
}

// Kuikly 版本（格式：<Kuikly 版本>-<Kotlin 版本>）
// 2.4.0 起 Kuikly 才提供 js 产物（core-js / core-render-web-*），
// 这是 Web / 微信小程序支持的硬性前提。
val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.4.0-2.0.21"

version = MavenConfig.VERSION
group = MavenConfig.GROUP

// iOS 目标与 CocoaPods 插件只能在 macOS 上构建
val isMacOs: Boolean = System.getProperty("os.name").startsWith("Mac")

kotlin {
    // ─── Android ───
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    // ─── JS / Web / 微信小程序 ───
    // 纯 DSL 组件不产出可执行 JS：库只输出 klib，由宿主壳工程（H5 / 小程序）
    // 打包成 bundle 后运行。
    js(IR) {
        browser {
            // 库的 JS 单测跑在 node 上，浏览器测试需要 Chrome，交给宿主工程
            testTask { enabled = false }
        }
        nodejs()
        // 浏览器 / 小程序都走 webpack 输出，库本身不需要 executable
    }

    // ─── iOS（仅 macOS 可构建）───
    if (isMacOs) {
        // XCFramework 聚合产物（CocoaPods 分发用）
        val whatsNewXcf = XCFrameworkConfig(project, "WhatsNew")
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "WhatsNew"
                isStatic = true
                whatsNewXcf.add(this)
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kuikly 核心库（跨端 ViewContainer / DivView / TextView / Pager 等）
                api("com.tencent.kuikly-open:core:$kuiklyVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core-render-android:$kuiklyVersion")
            }
        }

        val jsMain by getting {
            // Web / 小程序侧只需要 Kuikly 的 JS 标准实现。
            // core-render-web（h5 / miniapp）由宿主壳工程引入，库不绑定具体运行时。
        }

        if (isMacOs) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
        }
    }
}

android {
    namespace = "io.github.Richard102257.whatsnew"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// ─── 说明 ───
// 1. 鸿蒙产物由 build.ohos.gradle.kts 单独构建（需要 Kuikly 的 ohosArm64 Kotlin 目标），
//    该脚本含 plugins {} 块，不能通过 apply(from=) 复用，使用时替换本文件即可。
// 2. iOS 产物由 GitHub Actions 的 macOS runner 构建成 XCFramework，通过 CocoaPods 分发
//    （podspec 见根目录 KuiklyWhatsNew.podspec，指向 GitHub Release 上的预编译 zip）。

// ─── iOS XCFramework 构建任务（仅 macOS）───
// 产物用于 CocoaPods 分发：./gradlew :whatsnew:assembleWhatsNewXCFramework
// 输出目录：whatsnew/build/XCFrameworks/release/WhatsNew.xcframework
if (isMacOs) {
    tasks.register("buildXCFramework") {
        group = "build"
        description = "Assemble the WhatsNew XCFramework for CocoaPods distribution"
        dependsOn("assembleWhatsNewXCFramework")
    }
}

// ─── Maven 发布配置 ───
publishing {
    repositories {
        val username = project.findProperty("mavenUsername") as String? ?: ""
        val password = project.findProperty("mavenPassword") as String? ?: ""

        if (username.isNotEmpty() && password.isNotEmpty()) {
            maven {
                credentials {
                    setUsername(username)
                    setPassword(password)
                }
                url = uri("https://s01.oss.sonatype.org/service/local/deploy/maven2/")
            }
        } else {
            mavenLocal()
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Kuikly WhatsNewKit")
            description.set("Cross-platform WhatsNew changelog component for Kuikly")
            url.set("https://github.com/Richard102257/kuikly-whatsnew-kit")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Richard102257")
                    name.set("Richard")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/Richard102257/kuikly-whatsnew-kit.git")
                url.set("https://github.com/Richard102257/kuikly-whatsnew-kit")
            }
        }
    }
}
