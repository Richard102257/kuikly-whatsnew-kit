pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "kuikly-whatsnew-kit"

// ─── 库模块（唯一，纯 Kuikly DSL 跨端组件）───
include(":whatsnew")

// ─── Demo 工程（不发布）───
include(":androidApp")
include(":shared")
include(":miniApp")
