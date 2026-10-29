plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

val isMacOs: Boolean = System.getProperty("os.name").startsWith("Mac")

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }

    // iOS 目标只能在 macOS 上构建
    if (isMacOs) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
            it.binaries.framework { baseName = "shared" }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":whatsnew"))
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
    namespace = "io.github.Richard102257.whatsnew.shared"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
