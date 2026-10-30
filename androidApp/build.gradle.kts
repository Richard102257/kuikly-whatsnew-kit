plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "io.github.Richard102257.whatsnew.demo"
    compileSdk = 34
    defaultConfig {
        applicationId = "io.github.Richard102257.whatsnew.demo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"  // 设置为 2.0.0 触发自动展示
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":shared"))
    // shared 里用的是 implementation(project(":whatsnew"))，不会传递到 app 的编译类路径
    implementation(project(":whatsnew"))

    // Kuikly 依赖（与 :whatsnew 使用同一个版本，见 gradle.properties 的 kuiklyVersion）
    val kuiklyVersion: String = (project.findProperty("kuiklyVersion") as String?) ?: "2.4.0-2.0.21"
    implementation("com.tencent.kuikly-open:core:$kuiklyVersion")
    implementation("com.tencent.kuikly-open:core-render-android:$kuiklyVersion")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
