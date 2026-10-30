cmake_minimum_required(VERSION 3.4.1)
project(kuikly_entry)

set(CMAKE_CXX_STANDARD 17)

# 注意：不要 include hvigor 生成的 ${PACKAGE_FIND_FILE}。
# 工程路径含空格（"Default Project"）时，它生成的 summary.cmake 里路径未加引号，
# CMake 会直接报 "Unknown arguments specified"。本项目手工 link libkuikly.so，用不到它。

set(SOURCE_SET
        napi_init.cpp
)

add_library(kuikly_entry SHARED ${SOURCE_SET})

# ── Kuikly 渲染层（ohpm 包 @kuikly-open/render）──
set(RENDER_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../oh_modules/@kuikly-open/render)
target_include_directories(kuikly_entry PRIVATE "${RENDER_DIR}/include")

# ohpm 安装的 HAR 自带预编译 libkuikly.so；从源码构建时在 build/ 下
if(EXISTS "${RENDER_DIR}/libs/${OHOS_ARCH}/libkuikly.so")
    set(KUIKLY_SO "${RENDER_DIR}/libs/${OHOS_ARCH}/libkuikly.so")
else()
    set(KUIKLY_SO "${RENDER_DIR}/build/default/intermediates/libs/default/${OHOS_ARCH}/libkuikly.so")
endif()

add_library(kuikly_render SHARED IMPORTED)
set_target_properties(kuikly_render PROPERTIES IMPORTED_LOCATION "${KUIKLY_SO}")
target_link_libraries(kuikly_entry PUBLIC kuikly_render)

# ── 系统库 ──
target_link_libraries(kuikly_entry PUBLIC
    libace_napi.z.so
    libace_ndk.z.so
    hilog_ndk.z.so
    libnative_drawing.so
    libpixelmap.so
    libimage_source.so
)

# ── 业务库：Kotlin/Native 产出的 libshared.so ──
target_link_libraries(kuikly_entry PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/arm64-v8a/libshared.so
)
