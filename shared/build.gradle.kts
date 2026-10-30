plugins {
    kotlin("multiplatform")
    id("com.android.library")
    // iOS Demo：KSP 生成 KuiklyCoreEntry，CocoaPods 插件产出 shared.framework 的 podspec
    id("com.google.devtools.ksp")
    kotlin("native.cocoapods")
}

val isMacOs: Boolean = System.getProperty("os.name").startsWith("Mac")
val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.4.0-2.0.21"

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }

    // iOS 目标只能在 macOS 上构建
    if (isMacOs) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":whatsnew"))
                implementation("com.tencent.kuikly-open:core-annotations:$kuiklyVersion")
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

    if (isMacOs) {
        cocoapods {
            // pod 名 / framework 名都叫 shared，iOS 宿主用
            // `initWithPageName:pageData:frameworkName:@"shared"` 找到 KuiklyCoreEntry。
            summary = "Kuikly WhatsNewKit iOS demo (shared Kotlin code)"
            homepage = "https://github.com/Richard102257/kuikly-whatsnew-kit"
            version = "1.0.0"
            ios.deploymentTarget = "14.1"
            license = "MIT"
            framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }
}

dependencies {
    // @Page 注册由 Kuikly KSP 生成，iOS 侧需要给三个 native target 挂上处理器
    if (isMacOs) {
        compileOnly("com.tencent.kuikly-open:core-ksp:$kuiklyVersion") {
            add("kspIosArm64", this)
            add("kspIosX64", this)
            add("kspIosSimulatorArm64", this)
        }
    }
}

ksp {
    // 只注册 Demo 页面，避免把别的 @Page 也打进来
    arg("pageName", "WhatsNewDemoPage")
}

android {
    namespace = "io.github.Richard102257.whatsnew.shared"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
