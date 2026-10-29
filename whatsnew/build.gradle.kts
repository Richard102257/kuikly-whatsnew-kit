plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

// ─── 版本配置 ───
object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION = "1.0.0"
    // 鸿蒙版本加后缀避免重复
    const val VERSION_OHOS = "1.0.0_ohos"
}

version = MavenConfig.VERSION
group = MavenConfig.GROUP

kotlin {
    // Android Target
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    // iOS Targets
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

    // HarmonyOS Target（通过 build.ohos.gradle.kts 单独构建发布）
    // 此处仅声明编译目标，不发布
    ohosArm64 {
        binaries.sharedLib {
        }
    }

    // CocoaPods（KMP 跨端模块的 iOS Framework 产物发布）
    // 发布命令: ./gradlew :whatsnew:podPublish
    // 用户通过 pod 'WhatsNew' 引入跨端模块编译出的 Framework
    cocoapods {
        summary = "Kuikly WhatsNewKit - Cross-platform changelog component"
        homepage = "https://github.com/Richard102257/kuikly-whatsnew-kit"
        version = MavenConfig.VERSION
        ios.deploymentTarget = "13.0"
        license = "MIT"
        framework {
            baseName = "WhatsNew"
            isStatic = true
            // 依赖 Kuikly iOS Render
            export("com.tencent.kuikly-open:core:2.0.0")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // 核心数据模型无外部依赖
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                // 依赖 Kuikly Android Render
                api("com.tencent.kuikly-open:core-render-android:2.0.0")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
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
    defaultConfig {
        minSdk = 21
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
                url = uri(
                    if (version.toString().contains("ohos"))
                        "https://s01.oss.sonatype.org/service/local/deploy/maven2/"
                    else
                        "https://s01.oss.sonatype.org/service/local/deploy/maven2/"
                )
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
            scm {
                connection.set("scm:git:git://github.com/Richard102257/kuikly-whatsnew-kit.git")
                url.set("https://github.com/Richard102257/kuikly-whatsnew-kit")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
