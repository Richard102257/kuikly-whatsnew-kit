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
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":whatsnewAndroid"))

    // Kuikly 依赖
    implementation("com.tencent.kuikly-open:core:2.1.1-2.0.21")
    implementation("com.tencent.kuikly-open:core-render-android:2.1.1-2.0.21")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
