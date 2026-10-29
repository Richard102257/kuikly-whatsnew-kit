plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

// ─── 版本配置 ───
object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION = "1.1.1"
    // 鸿蒙版本加后缀避免 Maven 元数据冲突
    const val VERSION_OHOS = "1.1.1_ohos"
}

// Kuikly 版本（格式：<Kuikly 版本>-<Kotlin 版本>）
val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.1.1-2.0.21"

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

    // ─── iOS（仅 macOS 可构建）───
    if (isMacOs) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "WhatsNew"
                isStatic = true
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
//    （podspec 见根目录 WhatsNew.podspec，指向 GitHub Release 上的预编译 zip）。

// ─── iOS XCFramework 构建任务（仅 macOS）───
// 产物用于 CocoaPods 分发：./gradlew :whatsnew:buildXCFramework
if (isMacOs) {
    tasks.register("buildXCFramework") {
        group = "build"
        description = "Assemble the WhatsNew XCFramework for CocoaPods distribution"
        dependsOn(tasks.matching { it.name.contains("XCFramework") })
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
