plugins {
    kotlin("android")
    id("com.android.library")
    id("maven-publish")
}

object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION = "1.0.0"
    const val ARTIFACT_ID = "whatsnew-android"
}

version = MavenConfig.VERSION
group = MavenConfig.GROUP

android {
    namespace = "io.github.Richard102257.whatsnew.android"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // 依赖 Kuikly Android Render
    api("com.tencent.kuikly-open:core-render-android:2.1.1-2.0.21")
    // 依赖 Kuikly Core
    api("com.tencent.kuikly-open:core:2.1.1-2.0.21")
}

// ─── Maven 发布配置 ───
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = MavenConfig.GROUP
            artifactId = MavenConfig.ARTIFACT_ID
            version = MavenConfig.VERSION

            pom {
                name.set("Kuikly WhatsNewKit - Android")
                description.set("Android platform implementation for Kuikly WhatsNewKit")
                url.set("https://github.com/Richard102257/kuikly-whatsnew-kit")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("Richard102257")
                        name.set("Richard")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Richard102257/kuikly-whatsnew-kit.git")
                    url.set("https://github.com/Richard102257/kuikly-whatsnew-kit")
                }
            }
        }
    }
    repositories {
        val username = project.findProperty("mavenUsername") as String? ?: ""
        val password = project.findProperty("mavenPassword") as String? ?: ""
        if (username.isNotEmpty() && password.isNotEmpty()) {
            maven {
                credentials { setUsername(username); setPassword(password) }
                url = uri("https://s01.oss.sonatype.org/service/local/deploy/maven2/")
            }
        } else {
            mavenLocal()
        }
    }
}

// 仅在有签名配置时启用（JitPack 构建无需签名）
if (project.hasProperty("signing.keyId")) {
    apply(plugin = "signing")
    configure<SigningExtension> { sign(publishing.publications) }
}
