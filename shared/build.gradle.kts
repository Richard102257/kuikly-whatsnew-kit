plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }

    listOf(iosX64(), iosArm4(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "shared" }
    }

    cocoapods {
        summary = "Kuikly WhatsNewKit Demo Shared"
        homepage = "https://github.com/Richard102257/kuikly-whatsnew-kit"
        version = "1.0.0"
        ios.deploymentTarget = "13.0"
        framework { baseName = "shared" }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":whatsnew"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":whatsnewAndroid"))
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
    }
}

android {
    namespace = "com.kuikly.whatsnew.shared"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
