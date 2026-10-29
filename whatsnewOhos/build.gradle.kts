plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

object MavenConfig {
    const val GROUP = "io.github.Richard102257"
    const val VERSION = "1.0.0_ohos"
    const val ARTIFACT_ID = "whatsnew-ohos"
}

version = MavenConfig.VERSION
group = MavenConfig.GROUP

android {
    namespace = "io.github.Richard102257.ohos"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    // 依赖 Kuikly Ohos Render
    api("com.tencent.kuikly-open:core-render-ohos:2.0.0")
}

// ─── Maven 发布配置（JitPack / Maven Central） ───
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = MavenConfig.GROUP
            artifactId = MavenConfig.ARTIFACT_ID
            version = MavenConfig.VERSION

            pom {
                name.set("Kuikly WhatsNewKit - HarmonyOS")
                description.set("HarmonyOS platform implementation for Kuikly WhatsNewKit")
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

signing {
    sign(publishing.publications)
}
