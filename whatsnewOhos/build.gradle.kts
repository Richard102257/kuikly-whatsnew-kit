// 鸿蒙 Demo 的 Kotlin 侧 —— 编译成 libshared.so
//
// 产物：
//   build/bin/ohosArm64/sharedDebugShared/libshared.so        ← 放进 ohosApp/entry/libs/arm64-v8a/
//   build/bin/ohosArm64/sharedDebugShared/libshared_api.h     ← 放进 ohosApp/entry/src/main/cpp/thirdparty/biz_entry/
//
// NAPI 层（ohosApp/entry/src/main/cpp/napi_init.cpp）通过 libshared_symbols()->kotlin.root.initKuikly()
// 进入 Kotlin，initKuikly() 由 Kuikly 的 KSP 处理器生成（注册 @Page 路由）。
//
// 用法：
//   ./gradlew -c settings.ohos.gradle.kts :whatsnewOhos:linkSharedDebugSharedOhosArm64
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

val kuiklyOhosVersion: String =
    (project.findProperty("kuiklyOhosVersion") as String?) ?: "2.4.0-2.0.21-ohos"

kotlin {
    ohosArm64 {
        binaries.sharedLib("shared") {
            // 导出 initKuikly / com_tencent_kuikly_SetCallKotlin 给 NAPI 层
            freeCompilerArgs += "-Xadd-light-debug=enable"
            linkerOpts += "--build-id=sha1"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":whatsnew"))
                implementation("com.tencent.kuikly-open:core-annotations:$kuiklyOhosVersion")
            }
        }
    }
}

dependencies {
    // @Page 路由表 / initKuikly() 入口由 KSP 生成
    add("kspOhosArm64", "com.tencent.kuikly-open:core-ksp:$kuiklyOhosVersion")
}

ksp {
    // 只注册 Demo 页面
    arg("pageName", "WhatsNewDemoPage")
}
