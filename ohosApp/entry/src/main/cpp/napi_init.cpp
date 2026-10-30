/*
 * NAPI 桥：把 Kotlin/Native 的 libshared.so 暴露给 ArkTS。
 *
 * libshared.so 与 libshared_api.h 由 :whatsnewOhos 编译产出：
 *   ./gradlew -c settings.ohos.gradle.kts :whatsnewOhos:linkSharedDebugSharedOhosArm64
 * 产出后拷到：
 *   entry/libs/arm64-v8a/libshared.so
 *   entry/src/main/cpp/thirdparty/biz_entry/libshared_api.h
 */

#include <hilog/log.h>
#include <string>
#include <cstring>

#include "Kuikly/Kuikly.h"
#include "napi/native_api.h"
#include "thirdparty/biz_entry/libshared_api.h"

static void MyLogAdapter(int logLevel, const char *tag, const char *message) {
    static int myDomain = 0x1234;
    OH_LOG_Print(LOG_APP, LOG_INFO, myDomain, tag, "%{public}s", message);
}

static bool adapterRegistered = false;

static napi_value InitKuikly(napi_env env, napi_callback_info info) {
    if (!adapterRegistered) {
        KRRegisterLogAdapter(MyLogAdapter);
        adapterRegistered = true;
    }

    auto api = libshared_symbols();
    int handler = api->kotlin.root.initKuikly();

    napi_value result;
    napi_create_int32(env, handler, &result);
    return result;
}

EXTERN_C_START
static napi_value Init(napi_env env, napi_value exports) {
    napi_property_descriptor desc[] = {
        {"initKuikly", nullptr, InitKuikly, nullptr, nullptr, nullptr, napi_default, nullptr},
    };
    napi_define_properties(env, exports, sizeof(desc) / sizeof(desc[0]), desc);
    return exports;
}
EXTERN_C_END

static napi_module entryModule = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = Init,
    .nm_modname = "kuikly_entry",
    .nm_priv = static_cast<void *>(0),
    .reserved = {0},
};

extern "C" __attribute__((constructor)) void RegisterKuiklyEntryModule(void) {
    napi_module_register(&entryModule);
}
