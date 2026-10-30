# HarmonyOS Demo（Kuikly WhatsNewKit）

这是一个标准的 DevEco Studio 工程（Stage 模型 + ArkTS + NAPI），把 `:whatsnew` 的
Kotlin 代码编成 `libshared.so`，由 Kuikly 的鸿蒙渲染层（ohpm 包 `@kuikly-open/render`）驱动。

```
ohosApp/
├── AppScope/app.json5              # bundleName / versionName（Demo 用 1.2.0）
├── entry/
│   ├── oh-package.json5            # @kuikly-open/render 2.4.0 + libkuikly_entry.so
│   ├── src/main/cpp/               # NAPI 桥：napi_init.cpp 调 Kotlin 的 initKuikly()
│   ├── src/main/ets/               # EntryAbility + Kuikly 组件 + NativeManager
│   └── src/main/resources/         # 图标 / 文案 / 页面表
└── build-profile.json5
```

## 1. 编译 Kotlin 侧（libshared.so）

```bash
export JAVA_HOME="$HOME/.local/jdk17/.../Contents/Home"

# 只在鸿蒙构建里生效：KBA Kotlin + ohosArm64 目标
./gradlew -c settings.ohos.gradle.kts :whatsnewOhos:linkSharedDebugSharedOhosArm64
```

产物：

| 文件 | 去向 |
|---|---|
| `whatsnewOhos/build/bin/ohosArm64/sharedDebugShared/libshared.so` | `ohosApp/entry/libs/arm64-v8a/` |
| `whatsnewOhos/build/bin/ohosArm64/sharedDebugShared/libshared_api.h` | `ohosApp/entry/src/main/cpp/thirdparty/biz_entry/` |

拷贝：

```bash
cd ohosApp
mkdir -p entry/libs/arm64-v8a entry/src/main/cpp/thirdparty/biz_entry
cp ../whatsnewOhos/build/bin/ohosArm64/sharedDebugShared/libshared.so entry/libs/arm64-v8a/
cp ../whatsnewOhos/build/bin/ohosArm64/sharedDebugShared/libshared_api.h entry/src/main/cpp/thirdparty/biz_entry/
```

## 2. 装 ohpm 依赖

```bash
export PATH="/Applications/DevEco-Studio.app/Contents/tools/ohpm/bin:/Applications/DevEco-Studio.app/Contents/tools/node/bin:$PATH"
cd ohosApp/entry && ohpm install
```

`@kuikly-open/render` 的 HAR 自带 `libs/arm64-v8a/libkuikly.so` 和 `include/Kuikly/*.h`，
CMake 直接链它，不需要从 KuiklyUI 源码构建渲染层。

## 3. 编 HAP

```bash
export DEVECO_SDK_HOME="/Applications/DevEco-Studio.app/Contents/sdk"
export PATH="/Applications/DevEco-Studio.app/Contents/tools/hvigor/bin:$PATH"
cd ohosApp
hvigorw --mode module -p product=default -p buildMode=debug assembleHap
```

产物：`ohosApp/entry/build/default/outputs/default/entry-default-unsigned.hap`。

## 4. 装到设备 / 模拟器

需要签名。`build-profile.json5` 里 `signingConfigs` 是空的，所以只产出 unsigned HAP。
签名有两种方式：

- **DevEco Studio**：File → Project Structure → Signing Configs → 勾选 *Automatically
  generate signature*（需要登录华为开发者账号），然后在 Device Manager 里启动模拟器或连真机 Run。
- **命令行**：用 `hap-sign-tool.jar`（在
  `$DEVECO_SDK_HOME/default/openharmony/toolchains/lib/`）配自己的证书和 profile 签名。

安装：

```bash
hdc install entry-default-signed.hap
```

## 已知限制

- `:whatsnew` 的 `getCurrentVersion()` 在鸿蒙上仍是占位实现（`bundleManager` 是 ArkTS API，
  Kotlin/Native 侧没有绑定）。Demo 页面改为读 render 层透传的 `pageData.appVersion`
  （OHOS 渲染层用 `bundleManager.getBundleInfoForSelf()` 取的真实 `versionName`）。
- 存储用 `createWhatsNewVersionStore()`（Kuikly 内置 `SharedPreferencesModule`），
  跨冷启动保留；`createPersistentWhatsNewVersionStore()` 的鸿蒙 actual 仍是内存实现。
