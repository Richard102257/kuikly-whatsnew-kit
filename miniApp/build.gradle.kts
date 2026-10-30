import com.tencent.kuikly.gradle.config.KuiklyConfig

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    // Kuikly 打包插件：生成小程序 / H5 需要的页面入口与 bundle
    id("com.tencent.kuikly-open.kuikly")
}

// 与 :whatsnew 保持一致
val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.4.0-2.0.21"

kotlin {
    // Kuikly 插件在 apply 阶段依赖 android 的 compileOnly 配置，所以需要 androidTarget
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    js(IR) {
        browser {
            webpackTask {
                outputFileName = "nativevue2.js"
            }
            commonWebpackConfig {
                output?.library = null
                devtool = "source-map"
            }
        }
        // 小程序 / H5 需要一个可直接加载的 bundle
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":whatsnew"))
                implementation("com.tencent.kuikly-open:core-annotations:$kuiklyVersion")
            }
        }
    }
}

dependencies {
    // @Page 注册由 Kuikly KSP 生成
    compileOnly("com.tencent.kuikly-open:core-ksp:$kuiklyVersion") {
        add("kspJs", this)
        add("kspAndroid", this)
    }
}

ksp {
    // 支持 `-PpageName=WhatsNewDemoPage` 只打包单个页面
    arg("pageName", (project.findProperty("pageName") as? String) ?: "")
}

configure<KuiklyConfig> {
    js {
        // 与 webpackTask#outputFileName 保持一致
        outputName("nativevue2")
    }
}

android {
    namespace = "io.github.Richard102257.whatsnew.demo"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
