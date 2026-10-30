# 验证记录

本文件记录每个产物的**实际验证方式与结果**，用于确认「可编译、可发布、可被第三方接入」。

最后更新：2026-10-30

---

## 1. 库本身

| 检查项 | 命令 | 结果 |
|--------|------|------|
| Android 编译 | `./gradlew :whatsnew:compileDebugKotlinAndroid` | ✅ BUILD SUCCESSFUL |
| Android 发布产物 | `./gradlew :whatsnew:assembleRelease` | ✅ AAR + klib |
| iOS 编译（arm64） | `./gradlew :whatsnew:compileKotlinIosArm64` | ✅ BUILD SUCCESSFUL |
| iOS 编译（模拟器） | `./gradlew :whatsnew:compileKotlinIosSimulatorArm64` / `compileKotlinIosX64` | ✅ |
| **JS 编译** | `./gradlew :whatsnew:compileKotlinJs` | ✅ BUILD SUCCESSFUL |
| 单元测试（JVM/Android） | `./gradlew :whatsnew:testDebugUnitTest` | ✅ 14 tests / 0 failures |
| **单元测试（JS / node）** | `./gradlew :whatsnew:jsNodeTest` | ✅ 23 tests / 0 failures（14 common + 9 platform） |
| 发布到 mavenLocal | `./gradlew :whatsnew:publishToMavenLocal` | ✅ 生成 `whatsnew-js-1.2.0.klib` |

单元测试覆盖：版本号解析（含 `2`、`2.3`、`abc`、空串）、版本比较、`InMemoryWhatsNewVersionStore`、
DSL Builder 产物（title / features / primaryAction / secondaryAction / layout）、集合顺序、
以及 `KuiklySharedPreferencesWhatsNewVersionStore`（跨实例持久化、与原生平台一致的存储键/格式、
`clear()`、重复写入去重）。

JS 单测（`whatsnew/src/jsTest/.../PlatformJsTest.kt`）在 node 里伪造 `wx` / `localStorage`，
覆盖：小程序版本号读取、开发版空版本号与 `trial` 的过滤、覆盖值优先级、三种存储后端
（wx storage / localStorage / 内存兜底）、与原生平台一致的逗号分隔存储格式、
`WhatsNewEnvironment` 的自动展示与去重。

## 2. Android / JS 分发（JitPack）

- 坐标：`com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.0`
- JitPack 构建状态：`ok`（tag `v1.2.0`）
- 实测 HTTP 200（1.2.0）：

| 文件 | 大小 |
|------|------|
| `whatsnew/1.2.0/whatsnew-1.2.0.pom` | 1,548 B |
| `whatsnew/1.2.0/whatsnew-1.2.0.module` | 5,498 B（含 `jsApiElements` / `jsRuntimeElements` 变体） |
| `whatsnew-android/1.2.0/whatsnew-android-1.2.0.aar` | 69,551 B（`classes.jar` 75,338 B） |
| `whatsnew-js/1.2.0/whatsnew-js-1.2.0.klib` | 71,296 B |
| `whatsnew-js/1.2.0/whatsnew-js-1.2.0.pom` | 1,782 B |

**Android 第三方接入实测（1.1.1 时完成，API 未变）**：新建独立 KMP 工程，只声明 JitPack 依赖
（`androidMain`），代码里使用 `whatsNewCollection {}`、`WhatsNewView().setup(...)`、
`WhatsNewEnvironment`、`createPersistentWhatsNewVersionStore()`、
`ViewContainer<*, *>.addChild(...)` —— `:compileDebugKotlinAndroid` BUILD SUCCESSFUL。

**JS 第三方接入实测（1.2.0）**：新建独立 KMP 工程，只声明
`implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.0")`
+ `js(IR) { browser(); nodejs(); binaries.executable() }`，
`:jsNodeRun` 输出：

```
current=1.2.0 persistent=false title=What's New
```

即 Gradle 通过 `org.jetbrains.kotlin.platform.type=js` 自动选中 JitPack 上的
`whatsnew-js` klib，不需要任何额外配置。

## 3. Demo 工程

- `:androidApp:assembleDebug` → ✅ `androidApp-debug.apk`（6.72 MB，Kuikly 依赖全部解析到 `2.4.0-2.0.21`）
- `:shared:compileKotlinIosArm64`（Demo 共享代码依赖 `project(":whatsnew")`）→ ✅ BUILD SUCCESSFUL

## 3.1 小程序 / H5

`miniApp/` 是一个真实的 Kotlin/JS 业务模块（Kuikly 插件 + KSP + `@Page` 页面）：

| 检查项 | 命令 | 结果 |
|--------|------|------|
| 业务模块编译 | `./gradlew :miniApp:compileKotlinJs` | ✅ BUILD SUCCESSFUL |
| KSP 页面注册 | 查看 `miniApp/build/generated/ksp/js/jsMain/kotlin/KuiklyCoreEntry.kt` | ✅ 含 `BridgeManager.registerPageRouter("WhatsNewDemoPage")` |
| 打包 bundle | `./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage` | ✅ `nativevue2.zip`（`nativevue2.js` 1,872,859 B） |

产物路径：`miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip`。

> 小程序容器里已经完成真实渲染验证：同一份 bundle 放进 Kuikly 官方 `miniApp` 壳，
> 用微信开发者工具的「测试号」打开，`WhatsNewDemoPage` 正常渲染、点「继续」后
> `wx.setStorageSync` 落盘、重新进入走「已经是最新版本」分支。
> 截图和完整步骤见 [3.6](#36-微信小程序真实渲染截图)。
>
> H5 用同一份 bundle 在 Chromium + `core-render-web` 里也跑通了，
> 截图见 [3.4](#34-h5-真实渲染截图)。

## 3.2 Android 真实渲染截图（1.2.2）

`:androidApp` 里新增 `KuiklyDemoActivity`，通过 Kuikly 官方的
`KuiklyRenderViewBaseDelegator.onAttach` 渲染 `:miniApp` 的 `WhatsNewDemoPage` ——
**和微信小程序 / H5 编译成 JS 的是同一份 commonMain 页面代码**，没有原生 View 仿写。

| 截图 | 说明 |
|------|------|
| <img src="images/android-01-whatsnew.png" width="260"> | 宿主界面（`Kuikly WhatsNew` 标题 + 通用/通知/关于）+ `WhatsNewOverlay` 弹层：40% 黑遮罩、居中圆角卡片，卡片里是标题、3 条 feature、次按钮「查看更新日志」、主按钮「继续」 |
| <img src="images/android-02-caught-up.png" width="260"> | 点「继续」→ 杀进程重启：`whatsNew()` 返回 `null`，弹层不再出现，宿主界面顶部显示「已经是最新版本 🎉 / 当前版本 1.2.0」 |

复现命令：

```bash
# 1. 构建 APK（:androidApp 依赖 :miniApp 的 Kuikly 页面）
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# 2. 清掉已展示记录，首屏
adb shell pm clear io.github.Richard102257.whatsnew.demo
adb shell am start -n io.github.Richard102257.whatsnew.demo/.MainActivity
adb exec-out screencap -p > docs/images/android-01-whatsnew.png

# 3. 点卡片上的「继续」（1080x2400 上按钮中心实测约 571,1653）→ 重启看持久化效果
adb shell input tap 571 1653
adb shell am force-stop io.github.Richard102257.whatsnew.demo
adb shell am start -n io.github.Richard102257.whatsnew.demo/.MainActivity
adb exec-out screencap -p > docs/images/android-02-caught-up.png
```

环境：AVD `Medium_Phone_API_37.0`（Android 15 / API 37，x86_64）。

> 这两张图在 2026-10-30 重新跑过一遍：冷启动模拟器 → 连上 `AndroidWifi`（状态栏是系统
> 真实状态栏，不是 demo mode）→ `pm clear` → 截图 → `input tap` 点弹层卡片上的「继续」→
> `am force-stop` 后重启 → 截图。`shared_prefs/kuikly_whatsnew_store.xml` 里能看到
> `presented_1.2.0=true` / `presented_all=1.2.0`（单条，没有重复），证明持久化真的落盘了。
>
> GPU 选择上，`-gpu swiftshader_indirect` 在这台 Intel Mac 上会把 System UI 拖到 ANR
> （截图里会弹出 "System UI isn't responding"），`-gpu host` 反而正常；如果你的机器
> `host` 崩（SIGSEGV），换 `angle_indirect`。

### 3.3 这一轮修掉的 3 个真实渲染 bug

1.2.1 的 Android 渲染实际上是**不可用**的（截图是这轮第一次真正跑出来的）：

| # | 现象 | 根因 | 修复 |
|---|------|------|------|
| 1 | `KuiklyRenderViewExportException: can not find viewExport, name: WhatsNewView` | `WhatsNewView.viewName()` 返回自定义名字，但纯 DSL 组件没有注册平台 View | 改为 `ViewConst.TYPE_VIEW`（`com.tencent.kuikly.core.base`） |
| 2 | `RuntimeException: pager id is empty`，且 `attr {}` 静默失效 | `val v = DivView(); v.attr{}; addChild(v)` —— 脱离父容器创建时 `pagerId` 为空 | 全部改成 `addChild(DivView()) { ... }` 惯用写法 |
| 3 | 标题不居中、副标题被截断成「编译成 JS 即…」 | Kuikly 的 `TextView` 默认单行 + 省略号；且 `alignItems(FLEX_START)` 下拿不到确定宽度 | 标题外层加 `alignItems(CENTER)` 容器；文本容器改 `STRETCH` + `lines(10)` + `textOverFlowWordWrapping()` |

> `lines(0)` 在 Android 上等价于 `TextView.setMaxLines(0)`，整段文字会直接不显示，
> 所以这里用的是 `lines(10)`。

## 3.4 H5 真实渲染截图

| 截图 | 说明 |
|------|------|
| <img src="images/h5-01-whatsnew.png" width="240"> | 与 Android 同一份 `WhatsNewDemoPage`，在 Chromium 里由 Kuikly 的 `core-render-web` 渲染；宿主界面 + `WhatsNewOverlay` 弹层 |
| <img src="images/h5-02-caught-up.png" width="240"> | 点「继续」→ 刷新：`localStorage` 里已有 `kuikly_whatsnew_presented_all=1.2.0`，弹层消失，宿主界面顶部显示「已经是最新版本」 |

复现步骤（全部在本机跑通，没有依赖任何私有产物）：

```bash
# 1. 从 KuiklyUI 源码构建 H5 渲染器（core-render-web 没有发布到 Maven / npm）
#    源码里没有 settings.gradle.kts，需要自己补一个，只 include core-render-web
cd KuiklyUI
KUIKLY_VERSION=2.4.0 KUIKLY_KOTLIN_VERSION=2.0.21 \
  ./gradlew :core-render-web:h5:jsBrowserDevelopmentWebpack
# → core-render-web/h5/build/kotlin-webpack/js/developmentExecutable/KuiklyCore-render-web-h5.js

# 2. 打业务 bundle（和微信小程序用的是同一条命令）
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage
# → miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip 里的 nativevue2.js

# 3. 放进官方 h5App-js 壳
h5App-js/src/libs/KuiklyCore-render-web-h5.js
h5App-js/src/bundles/WhatsNewDemoPage.bundle.js
node scripts/generate-manifest.js && npm run build:dev
python3 -m http.server 8099 --directory build/distributions
# 打开 http://127.0.0.1:8099/?page_name=WhatsNewDemoPage
# 截图：CDP 设 390x844 @2x，真实点一下卡片上的「继续」，再刷新截第二张
node h5shot.js
```

### 3.4.1 为了跑起来打的两处本地补丁

这两处都在 **本地壳工程** 里，不在 `:whatsnew` 库代码里，也不影响发布产物：

1. **Kotlin/JS 2.0.21 的 `@JsExport` 导出 bug**
   渲染器加载时 `$jsExportAll$` 会引用三个从未声明的符号
   （`isJsArray$outlinedJsCode$` / `jsObjectOwnKeys$outlinedJsCode$` /
   `maybeStartAutoResizeForwarder$outlinedJsCode$`），直接抛 `ReferenceError`，
   导致所有 `@JsExport` 导出都挂不到 `window` 上。这三个符号运行期不会被调用，
   在 `KuiklyCore-render-web-h5.js` 前面补三个空实现即可。

2. **`window.com` 会被页面 bundle 覆盖**
   渲染器和业务 bundle 都是 UMD，都会执行 `for (var i in a) root[i] = a[i]`。
   业务 bundle 后加载，会把渲染器写入的 `window.com` 换成自己那份（只有 `core`）。
   所以在渲染器文件末尾把 `com.tencent.kuikly.core.render.web` 存到
   `window.__kuiklyRenderWebModule`，让壳去读这个引用。

3. **H5 的 `stretch + 子视图 margin` 只扣左边距**
   弹层最初用「遮罩 `alignItems(STRETCH)` + 卡片左右 `margin`」做留白，Android 正常，
   H5 上卡片却从右边溢出屏幕、标题被裁掉。改成把留白做成遮罩自己的 `padding` 后三端一致。
   这也是 `WhatsNewOverlay` 里用 padding 而不是 margin 的原因。

另外 `window.kotlin` 在 UMD 包里不会暴露到全局，壳里的 `SizeI` 用的是渲染器导出的
构造器，所以不需要 Kotlin stdlib 全局对象。

## 3.5 iOS 真实渲染截图

| 截图 | 说明 |
|------|------|
| <img src="images/ios-01-whatsnew.png" width="240"> | iPhone 16 Pro 模拟器（iOS 18.3），Kotlin/Native `shared.framework` + 官方 `OpenKuiklyIOSRender` 渲染；宿主界面 + `WhatsNewOverlay` 弹层 |
| <img src="images/ios-02-caught-up.png" width="240"> | UI 测试真点了一下卡片上的「继续」→ 重启 App：`NSUserDefaults` 里已有 `presented_all=1.2.0`，弹层不再出现，宿主界面顶部显示「已经是最新版本」 |

> ⚠️ **`ios-02` 还是弹层改造前的整页布局**（2026-10-30 17:07 那张）。弹层这一轮重截时
> 这台机器的屏幕被锁了（`CGSSessionScreenIsLocked = 1`）：`xcodebuild test` 卡在
> test session 启动前，`simctl boot` / `simctl install` 也一起挂住，所以第二张没能重拍。
> `ios-01` 是新版（弹层）。解除锁屏后按 [3.5.1](#351-复现步骤) 的第 4、5 步跑一遍即可重拍，
> 命令和 `dy` 坐标都已经写好了。

页面源码是 [`shared/src/iosMain/.../WhatsNewIosDemoPage.kt`](../shared/src/iosMain/kotlin/io/github/Richard102257/whatsnew/shared/WhatsNewIosDemoPage.kt)，
组件本身还是 `:whatsnew` 的 commonMain 代码。和 Android / H5 页面相比只差两点：

- 不 override 版本号，直接读 `CFBundleShortVersionString`（Info.plist 写死 `1.2.0`）
- 宿主界面外层包了一个 Kuikly 的 `SafeArea`，否则标题会被灵动岛压住（`pagerData.safeAreaInsets`
  在 iOS 上是真实值，Android / H5 上是 0）；**弹层放在 `SafeArea` 外面**，否则遮罩只盖住安全区、
  盖不到状态栏

### 3.5.1 复现步骤

```bash
# 1. 生成 podspec + 编出模拟器用的 framework
#    Intel Mac 是 x86_64 切片，Apple Silicon 改成 arm64
./gradlew :shared:podspec :shared:syncFramework \
  -Pkotlin.native.cocoapods.platform=iphonesimulator \
  -Pkotlin.native.cocoapods.archs=x86_64 \
  -Pkotlin.native.cocoapods.configuration=Debug

# 2. 生成 Xcode 工程 + 装 pod（渲染器走 KuiklyUI 源码 pod）
ruby iosApp/generate_project.rb
cd iosApp
KUIKLYUI_PATH=/path/to/KuiklyUI pod install

# 3. 编译 & 安装
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  -derivedDataPath build/DerivedData build

# 4. UI 测试真点一下卡片上的「继续」（写 NSUserDefaults）
#    坐标 dy=0.745 是按 1206x2622 截图里紫色按钮中心（y≈1953px）算的
xcodebuild test -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  -derivedDataPath build/DerivedData \
  -only-testing:iosAppUITests/WhatsNewUITests/testTapPrimaryAction

# 5. 重启 App 截「已是最新版本」
xcrun simctl terminate booted io.github.Richard102257.whatsnew.demo
xcrun simctl launch booted io.github.Richard102257.whatsnew.demo
sleep 6
xcrun simctl io booted screenshot docs/images/ios-02-caught-up.png
```

`pod install` 之后 Xcode 里那个 `Build shared` 脚本阶段会自动跑 Gradle 的 `syncFramework`；
如果已经手动编过，可以用 `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` 跳过。

### 3.5.2 踩到的三个坑

1. **Intel Mac 必须编 `iosX64` 切片**
   只编 `iosSimulatorArm64` 的话链接器只会打一行
   `ld: warning: ignoring file '.../shared.framework/shared[2](shared.framework.o)':
   found architecture 'arm64', required architecture 'x86_64'`，
   App 能装能启动，页面却是**全白**（`KuiklyCoreEntry` 根本不在二进制里）。
   这个 warning 很容易被淹没在 xcodebuild 的日志里。

2. **入口类名是 `SharedKuiklyCoreEntry`**
   KSP 生成的 Kotlin 类叫 `KuiklyCoreEntry`，但 Kotlin/Native 导出成 ObjC 时会加上
   framework 前缀；渲染器 `entryClassWithFrameworkName:` 拼的是「首字母大写的
   framework 名 + KuiklyCoreEntry」，所以 `frameworkName:@"shared"` 正好对上
   `SharedKuiklyCoreEntry`。framework 名改了这里也要跟着改。

3. **必须自己处理安全区**
   `KuiklyRenderViewControllerBaseDelegator` 会把 renderView 铺满整个 window，
   页面从 y=0 开始画，标题会压在灵动岛下面。用 Kuikly 自带的 `SafeArea` 包一层即可，
   不需要在宿主里改 frame。

另外，Kuikly 的视图树默认不导出 accessibilityIdentifier，所以
`iosAppUITests/WhatsNewUITests.swift` 用归一化坐标（`dy = 0.885`）点主按钮 —— 点的是
真实屏幕位置，不是伪造状态。

## 3.6 微信小程序真实渲染截图

| 截图 | 说明 |
|------|------|
| <img src="images/miniapp-01-whatsnew.png" width="240"> | 微信开发者工具，iPhone 12/13 Pro 模拟器；与 H5 完全相同的 `nativevue2.js`，由 `core-render-web:miniapp` 渲染；宿主界面 + `WhatsNewOverlay` 弹层 |
| <img src="images/miniapp-02-caught-up.png" width="240"> | 点「继续」→ 重新进入页面：`wx.setStorageSync` 里已有 `kuikly_whatsnew_presented_all=1.2.0`，弹层消失，宿主界面顶部显示「已经是最新版本」 |

### 3.6.1 复现步骤

```bash
# 1. 编小程序渲染器入口（注意不是 :core-render-web:miniapp）
#    :core-render-web:miniapp 只导出 kotlin / com，官方壳要的 renderView / initApp / fontLoaded
#    在 KuiklyUI 的 :miniApp 模块里（miniApp/src/jsMain/kotlin/Main.kt）
cd KuiklyUI
# settings.gradle.kts 里 include(":core-render-web:miniapp") 和 include(":miniApp")
./gradlew kotlinUpgradeYarnLock :miniApp:jsBrowserDevelopmentWebpack
# → miniApp/build/kotlin-webpack/js/developmentExecutable/miniprogramApp.js

# 2. 打业务 bundle（和 H5 同一条命令）
cd /path/to/kuikly-whatsnew-kit
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage
# → miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip

# 3. 组装官方 miniApp 壳
#    壳文件来自 KuiklyUI 的 miniApp/dist（app.js / base.wxml / comp.* / custom-wrapper.* /
#    utils.wxs / assets），然后：
cp .../miniprogramApp.js  wxshell/lib/miniprogramApp.js
unzip -p .../nativevue2.zip nativevue2.js > wxshell/business/nativevue2.js
# app.json: pages 只留 pages/index/index；window.navigationStyle 用 default
# pages/index/index.js: render.renderView({ pageName: "WhatsNewDemoPage" })

# 4. 用开发者工具打开（AppID 用「测试号」）
#    手动：导入项目 → 选择 wxshell 目录 → 点「测试号」→ 创建 → Trust & Run
#    本机用 CLI 直接 open 会报 "Login user is not the developer" / "There is no such AppID"，
#    因为 CLI 会拿 project.config.json 里的 AppID 去服务端校验；测试号只能走 IDE 的导入流程。

# 5. 开自动化并截图
"/Applications/wechatwebdevtools.app/Contents/MacOS/cli" auto \
  --project "$PWD/wxshell" --auto-port 9420
# miniprogram-automator: clearStorageSync → reLaunch → mp.screenshot()
#   → page.$$('view') 找到卡片上的「继续」并 tap → 确认 storage → reLaunch → 再截
node wxshot.js
```

### 3.6.2 踩到的坑

1. **`render.initApp is not a function`**
   直接把 `core-render-web:miniapp` 的 webpack 产物当 `lib/miniprogramApp.js` 用会白屏。
   那个产物导出的是 `{ kotlin, com }`，`renderView` / `initApp` / `fontLoaded` 是
   `:miniApp` 模块 `Main.kt` 里的 `@JsExport` 包装。必须编 `:miniApp:jsBrowserDevelopmentWebpack`，
   产物文件名正好是 `miniprogramApp.js`。

2. **`navigationStyle: custom` 会把标题顶到状态栏里**
   官方 demo 壳默认是 `custom`，页面自己不管安全区时标题会压在刘海下面。
   这次把 `app.json` 的 `window.navigationStyle` 改成 `default`，让微信原生导航栏占位；
   内容区仍由 Kuikly 渲染。如果坚持 `custom`，需要在页面外层包 Kuikly 的 `SafeArea`。

3. **「已是最新版本」要重新进入页面**
   Demo 页的 `body()` 只在页面构建时读一次 `whatsNew()`，点「继续」只写存储、不会自动重排。
   截图流程是 tap → `mp.reLaunch('/pages/index/index')` → 再截，验证的是存储真的落盘了，
   不是把 UI 手动改掉。

4. **自动化端口**
   `cli auto` 会打印 `autoPort`；`miniprogram-automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' })`
   连上后 `mp.screenshot()` 拿到的是开发者工具模拟器画面（640×1386），不是 macOS 截屏。
   开发者工具主窗口是 `NSWindowSharingNone`，`screencapture` 截不到它，必须走 automator。

## 4. iOS 分发

### 4.1 XCFramework（1.2.0，Kuikly 2.4.0）

`./gradlew :whatsnew:assembleWhatsNewXCFramework`（macOS + Xcode 16.2）→ ✅ 6m42s

```
WhatsNew.xcframework
├── ios-arm64/WhatsNew.framework                     (arm64, 静态)
└── ios-arm64_x86_64-simulator/WhatsNew.framework    (x86_64 + arm64, 静态)
```

打包：`WhatsNew.xcframework.zip`（5,436,328 B），
sha256 `d110422a52c5cc462d73d7946da6a258d895a6f7fd43152d2c709078b4be9695`，
挂在 GitHub Release `v1.2.0`，下载 URL 实测可拉全量且 sha256 一致：

```
https://github.com/Richard102257/kuikly-whatsnew-kit/releases/download/v1.2.0/WhatsNew.xcframework.zip
```

### 4.2 CocoaPods

| 检查项 | 结果 |
|--------|------|
| `pod spec lint KuiklyWhatsNew.podspec --quick --allow-warnings` | ✅ passed validation |
| `pod trunk push KuiklyWhatsNew.podspec --allow-warnings`（本地完整编译 + 链接） | ✅ 通过（xcodebuild 4 targets） |
| 实际发布结果 | ✅ `KuiklyWhatsNew 1.2.0`，2026-10-30 04:02:33 UTC（trunk API 确认） |
| trunk 所有者 | `Richard <lhui1022@gmail.com>` |
| CDN 上可读到 podspec | ✅ `https://cdn.cocoapods.org/Specs/a/6/0/KuiklyWhatsNew/1.2.0/KuiklyWhatsNew.podspec.json` |
| 真实工程 `pod install`（本地 podspec 指向 1.2.0） | ✅ `Installing KuiklyWhatsNew (1.2.0)` + `OpenKuiklyIOSRender (2.30.0)` |
| 真实工程 `pod install '~> 1.2.0'`（**走 trunk / CDN，无本地路径**） | ✅ `Podfile.lock` 里 `KuiklyWhatsNew (1.2.0)`，`Pods/KuiklyWhatsNew/WhatsNew.xcframework` 两个切片就位 |

> `pod trunk push` 命令本身会以 exit 1 结束，报
> `Calling the GitHub commit API timed out` —— 那是 trunk 把 spec 镜像到 GitHub 仓库的
> 后续步骤受 `github.com:443` 超时影响，**版本已经发布成功**（trunk API 的 `versions`
> 里已有 `1.2.0`，CDN 也能取到 spec）。
>
> CDN 的 `all_pods_versions_*.txt` 索引由后台任务定期重建，刚发布后
> `pod install '~> 1.2.0'` 会短暂解析不到（1.2.0 于 04:02 UTC 发布，
> 索引在 06:24 UTC 才包含 `KuiklyWhatsNew/1.1.1/1.2.0`）。这期间可以用本地 podspec 验证：
>
> ```ruby
> pod 'KuiklyWhatsNew', :podspec => './KuiklyWhatsNew.podspec'
> ```
>
> 索引更新后，干净目录 + 仅 Podfile（`pod 'KuiklyWhatsNew', '~> 1.2.0'`）
> 执行 `pod install --repo-update` 已实测成功。

pod 名说明：`WhatsNew` 在 trunk 上已被 `me@patrickbalestra.com` 占用，故发布名为
`KuiklyWhatsNew`；framework 名与代码 `import` 不受影响。

### 4.3 已知小瑕疵（1.2.0）

1.2.0 的 `WhatsNew.xcframework.zip` 只打了 `WhatsNew.xcframework`，**没带 `LICENSE`**，
所以 lint / `pod install` 会打印 `[!] Unable to read the license file 'LICENSE'`
（podspec 里 `license[:file] => 'LICENSE'`）。这是警告，trunk 已正常接受，pod 也能装；
从 1.2.1 起按 [PUBLISHING.md](PUBLISHING.md) 的命令把 `LICENSE` 一起打进 zip。

## 5. 环境备注

- 本机 Xcode 16.2 出厂未带 iOS 平台，需先 `xcodebuild -downloadPlatform iOS`（约 8.7 GB）。
  未安装时 `pod spec lint` 会报
  `iOS 18.2 is not installed. To use with Xcode, first download and install the platform`。
- JitPack 在 Linux 上构建，能产出 Android AAR + JS klib + 公共元数据；
  iOS klib / XCFramework 必须在 macOS 上构建。
- JitPack 构建 js target 需要下载 Node.js / yarn（Kotlin/JS 插件自管理），
  `kotlin-js-store/yarn.lock` 已提交，避免 `Lock file was changed` 失败。
- `OpenKuiklyIOSRender` 依赖在本地 lint 时通过 `git url.insteadOf` 指向一份
  `core-render-ios` 子集仓库，以规避 `github.com` 拉取超时；正式 lint / push 使用 trunk 上的 2.30.0。
- 鸿蒙未验证：`core-render-ohos` **没有发布到 Maven Central，Tencent 镜像上也没有**
  （`maven-metadata.xml` 404），它由 KuiklyUI 仓库的 C++ / ArkTS 源码构建；
  `ohosArm64` 目标还需要 Kuikly 定制的 Kotlin `2.0.21-KBA-010`。
  因此 `whatsnew/build.ohos.gradle.kts` 只声明 DSL 层 `core`，render 层由宿主的鸿蒙工程提供。

## 6. 1.2.2 发布记录

1.2.2 是**行为性 bug 修复**：1.2.1 在 Android 上 `WhatsNewView` 根本渲染不出来（见 3.3）。

| 检查项 | 命令 / 地址 | 结果 |
|--------|-------------|------|
| Android 单测 | `./gradlew :whatsnew:testDebugUnitTest` | ✅ 14 tests / 0 failures |
| JS 单测 | `./gradlew :whatsnew:jsNodeTest` | ✅ 23 tests / 0 failures |
| 四目标编译 | `:whatsnew:compileDebugKotlinAndroid`、`compileKotlinJs`、`compileKotlinIosX64`、`compileKotlinIosArm64`、`compileKotlinIosSimulatorArm64` | ✅ 全部 BUILD SUCCESSFUL |
| Android 真实渲染 | `:androidApp:assembleDebug` + AVD 截图 | ✅ 见 3.2 |
| XCFramework | `./gradlew :whatsnew:assembleWhatsNewXCFramework` | ✅ 5m34s，`WhatsNew.xcframework`（含 `ios-arm64` + `ios-arm64_x86_64-simulator`） |
| GitHub Release | `https://github.com/Richard102257/kuikly-whatsnew-kit/releases/tag/v1.2.2` | ✅ `WhatsNew.xcframework.zip` 5,489,382 B，sha256 `00979ff05a84c01be3d8c7b6ab61c7b95541d8c65480344f30afaf6411560e18` |
| JitPack | `https://jitpack.io/#Richard102257/kuikly-whatsnew-kit/v1.2.2` | ✅ build `ok`；`whatsnew-1.2.2.pom` 200、`whatsnew-android-1.2.2.aar` 200 (76,226 B)、`whatsnew-js-1.2.2.klib` 200 (75,837 B) |
| CocoaPods trunk | `pod trunk push KuiklyWhatsNew.podspec` | ✅ 已发布；trunk API `versions` 含 `1.2.2`，CDN spec 200 |

对应提交：`b2e6893`（tag `v1.2.2`）。

### 6.1 关于 iOS pod 的说明

`pod spec lint` 通过（`KuiklyWhatsNew (1.2.2)` 校验成功，XCFramework 从 Release 下载解压后
在 iOS 18.2 模拟器上编译通过）。trunk 推送第一次遇到 `Net::OpenTimeout`（本机到
`trunk.cocoapods.org` 的网络抖动，不是 spec 问题），重试后成功：

```
$ curl -s https://trunk.cocoapods.org/api/v1/pods/KuiklyWhatsNew
versions = ['1.1.1', '1.2.0', '1.2.1', '1.2.2']

$ curl -sI https://cdn.cocoapods.org/Specs/a/6/0/KuiklyWhatsNew/1.2.2/KuiklyWhatsNew.podspec.json
200
```

推送结束时 `pod` 会打印 `Calling the GitHub commit API timed out` —— 那是 trunk 把 spec
镜像到 GitHub 仓库的后续步骤受 `github.com:443` 超时影响，**版本已经发布成功**。
