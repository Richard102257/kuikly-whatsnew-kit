plugins {
    kotlin("multiplatform") version "2.0.21" apply false
    kotlin("plugin.compose") version "2.0.21" apply false
    id("com.android.library") version "8.2.0" apply false
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
