// Kuikly Gradle 插件（仅小程序 / H5 Demo 打包需要；库模块本身不依赖它）
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.tencent.kuikly-open:core-gradle-plugin:2.4.0-2.0.21")
    }
}

plugins {
    kotlin("multiplatform") version "2.0.21" apply false
    kotlin("android") version "2.0.21" apply false
    kotlin("plugin.compose") version "2.0.21" apply false
    id("com.android.library") version "8.2.0" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    id("maven-publish")
    id("signing")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

subprojects {
    group = "io.github.Richard102257"
    version = "1.0.0"
}
