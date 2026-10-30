# 微信小程序 / H5（Web）接入指南

> 从 **1.2.0** 起，`:whatsnew` 声明了 `js(IR)` 目标。
> 同一份 `commonMain` 代码（数据模型 + Kuikly DSL 视图）编译成 JS 后，
> 可以跑在 **微信小程序** 和 **H5** 上，UI 不需要重写。

---

## 0. 前置条件：Kuikly ≥ 2.4.0

Kuikly 从 **2.4.0** 才开始发布 js 产物（`core-js`、`core-render-web-base/h5/miniapp`）。
1.2.0 起本库的默认 `kuiklyVersion` 为 `2.4.0-2.0.21`：

| Kuikly 版本 | 有 js 产物 | 说明 |
|-------------|-----------|------|
| `2.1.1-2.0.21` | ❌ | 1.1.x 使用的版本，只有 Android / iOS / OHOS |
| `2.3.0-2.0.21` | ✅ | 可用 |
| `2.4.0-2.0.21` | ✅ | **默认值**，`core-render-web` 最高只到 2.4.0 |
| `2.4.2-2.0.21` | ⚠️ | `core` 有 js，但 `core-render-web` 没有 2.4.2，壳工程会对不上 |

Kotlin 版本不需要升级（仍是 2.0.21）。

---

## 1. 库在 JS 上做了什么

平台差异只有两处 `expect/actual`，js 侧实现位于
`whatsnew/src/jsMain/kotlin/io/github/Richard102257/whatsnew/Platform.kt`。

### 1.1 版本号 `getCurrentVersion()`

按顺序探测，命中即返回：

| 顺序 | 来源 | 适用环境 | 示例 |
|------|------|----------|------|
| 0 | `whatsNewCurrentVersionOverride` | 所有平台（手动覆盖，优先级最高） | `"1.2.0"` |
| 1 | `wx.getAccountInfoSync().miniProgram.version` | 微信小程序正式版 / 体验版 | `"1.2.0"` |
| 2 | `<meta name="app-version" content="…">` | H5 | `"1.2.0"` |
| 3 | `window.__WHATSNEW_VERSION__` | H5（构建期 / 服务端注入） | `"1.2.0"` |
| 4 | 兜底 | 任何环境 | `1.0.0` |

> ⚠️ **微信开发者工具（开发版）拿不到版本号**：`miniProgram.version` 返回空字符串，
> 体验版返回 `trial` 之类的非版本串。这两种都会被过滤掉并继续往下走，
> 所以在开发阶段建议显式设置覆盖值。

```kotlin
import io.github.Richard102257.whatsnew.whatsNewCurrentVersionOverride

fun main() {
    // 开发期固定版本；正式包删掉这行，让库自己去读
    whatsNewCurrentVersionOverride = "1.2.0"
}
```

H5 更推荐用 meta 标签，让版本号跟着构建产物走：

```html
<meta name="app-version" content="1.2.0">
```

### 1.2 持久化 `createPersistentWhatsNewVersionStore()`

`JsWhatsNewVersionStore` 会按可用性自动选后端：

| 后端 | 判定条件 | 读写 API |
|------|----------|----------|
| 微信小程序 | `typeof wx.getStorageSync === 'function' && typeof wx.setStorageSync === 'function'` | `wx.getStorageSync` / `wx.setStorageSync` |
| 浏览器 | `typeof localStorage !== 'undefined'` | `localStorage.getItem` / `setItem` |
| 内存兜底 | 以上都不满足（隐私模式、SSR、node） | `MutableSet`，不持久化 |

存储键与原生平台一致，格式也一致（逗号分隔的已展示版本列表）：

```
key:   kuikly_whatsnew_presented_all
value: 1.0.0,1.2.0
```

因此同一个版本号在 Android / iOS / 小程序上语义完全相同，逻辑层不需要分支。
可以用 `isWhatsNewStoragePersistent` 检查当前环境是否真的能持久化：

```kotlin
if (!isWhatsNewStoragePersistent) {
    // 例如提示用户「当前浏览器禁用了本地存储」
}
```

---

## 2. 宿主工程接入

库本身只产出 **klib**，不产出可执行 JS。真正跑起来需要宿主侧做两件事：
把库编进 bundle，再由 Kuikly 的 Web 渲染层（`core-render-web`）加载执行。

### 2.1 依赖

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// 业务模块（例如 shared / miniApp）的 build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.2.2")
            }
        }
    }
}
```

JitPack 会为 js target 额外产出 `whatsnew-js` 的 klib，Gradle 通过
`org.jetbrains.kotlin.platform.type=js` 自动选中，不需要额外配置。

### 2.2 业务模块需要声明 js 目标

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")                 // Kuikly 插件在 apply 阶段依赖 android 的 compileOnly 配置
    id("com.google.devtools.ksp")
    id("com.tencent.kuikly-open.kuikly")      // Kuikly 打包插件
}

kotlin {
    js(IR) {
        browser {
            webpackTask { outputFileName = "nativevue2.js" }
            commonWebpackConfig {
                output?.library = null
                devtool = "source-map"
            }
        }
        binaries.executable()                 // 必须：要产出一个可直接加载的 bundle
    }
}

dependencies {
    // @Page 注册由 Kuikly KSP 生成（只扫描本模块源码）
    compileOnly("com.tencent.kuikly-open:core-ksp:$kuiklyVersion") {
        add("kspJs", this)
    }
}
```

`buildscript` 里需要 Kuikly 插件本体：

```kotlin
buildscript {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    dependencies {
        classpath("com.tencent.kuikly-open:core-gradle-plugin:2.4.0-2.0.21")
    }
}
```

### 2.3 页面写法：自己挂 `WhatsNewView`

**推荐**在宿主自己的页面里 `addChild`，库不依赖宿主的路由：

```kotlin
@Page("HomePage")
internal class HomePage : Pager() {

    private val environment = WhatsNewEnvironment(
        versionStore = createPersistentWhatsNewVersionStore(),
        whatsNewCollection = whatsNewCollection {
            whatsNew("1.2.0") {
                title("What's New")
                feature(WhatsNewIcon.SystemIcon("star.fill"), "新功能", "描述")
                primaryAction { title = "继续" }
            }
        }
    )

    override fun body(): ViewBuilder = {
        val pending = this@HomePage.environment.whatsNew()
        if (pending != null) {
            addChild(WhatsNewView()) {
                setup(pending) {
                    this@HomePage.environment.markAsPresented(pending.version)
                }
            }
        } else {
            addChild(createHomeContent()) { }
        }
    }
}
```

> ⚠️ **`WhatsNewPage` 不能直接按名字路由打开。**
> Kuikly 的 `@Page` 注解是 `SOURCE` 保留，且 KSP 只扫描**当前模块**源码，
> 所以库里的 `WhatsNewPage` 不会被宿主的 KSP 注册，`router.openPage("WhatsNewPage")` 找不到它。
> 需要独立页面时，在自己的模块里包一层：
>
> ```kotlin
> @Page("WhatsNewPage")
> internal class AppWhatsNewPage : WhatsNewPage(defaultWhatsNewEnvironment) {
>     init { pageName = "WhatsNewPage" }
> }
> ```

### 2.4 打包

```bash
# 单 bundle（内置到小程序主包 / H5 静态资源）
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage

# 分包（配合 miniApp-js / h5App-js 的按需加载）
./gradlew :miniApp:packSplitJSBundleDebug -PpageNameList=WhatsNewDemoPage
```

产物：

```
miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip
├── nativevue2.js     # 页面 + 库 + Kuikly core，webpack 打包
└── config.json
```

### 2.5 装进微信小程序

微信小程序的容器代码由 Kuikly 官方的 `miniApp-js` 工程提供（它负责
`wx` 侧的 wxml 模板、自定义组件、事件转发、模块桥接）：

1. `git clone https://github.com/Tencent-TDS/KuiklyUI`
2. `cd KuiklyUI/miniApp-js && npm install && npm run build`
3. 把上一步的 `nativevue2.js` 放进 `miniApp-js/src/bundles/`，重新 `npm run build`
4. 微信开发者工具导入 `miniApp-js/dist`，填写 AppID（可用测试号）
5. `dist/pages/index/index.js` 里指定页面名：

```javascript
var render = global.render
render.renderView({ pageName: "WhatsNewDemoPage" })
```

H5 同理，壳工程换成 `h5App-js`（`npm run dev` 起本地服务，`?page_name=WhatsNewDemoPage`）。

---

## 3. 本仓库的 Demo

`miniApp/` 是一个真实的 Kotlin 业务模块（不是占位），只包含 Demo 页面：

```
miniApp/
├── build.gradle.kts                                  # js + android 目标，Kuikly 插件 + KSP
├── src/jsMain/kotlin/.../WhatsNewDemoPage.kt         # @Page("WhatsNewDemoPage") + WhatsNewView
└── README.md
```

已实际验证（见 [VERIFICATION.md](VERIFICATION.md)）：

```bash
./gradlew :miniApp:compileKotlinJs
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage
# → miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip (nativevue2.js ≈ 1.87 MB)
```

KSP 生成的入口里可以看到页面注册：

```kotlin
BridgeManager.registerPageRouter("WhatsNewDemoPage") {
    io.github.Richard102257.whatsnew.demo.WhatsNewDemoPage()
}
```

---

## 4. 常见问题

**Q：小程序里滚动不生效？**
`WhatsNewView` 的功能列表用的是 `overflow(true)` 的 `DivView`，在小程序里依赖
`scroll-view` 的映射。内容特别长时建议把 `WhatsNewLayout.contentPadding` 调小，
或在外层再包一层 `Scroller`。

**Q：包体积？**
`nativevue2.js` 里绝大部分是 Kuikly core 与 Kotlin stdlib。小程序主包有 2 MB 限制，
页面多时用 `packSplitJSBundleDebug` 分包，或把 Demo 页放进分包。

**Q：H5 里 `localStorage` 被禁用怎么办？**
库会自动退化成内存存储（不崩溃），但「已展示版本」不会跨会话保留，
用户会重复看到 WhatsNew。两种解法：

1. 在页面里改用 `createWhatsNewVersionStore()`（基于 Kuikly 的
   `SharedPreferencesModule`，H5 / 小程序 render 层都有实现，走的是 Kuikly 自己的存储桥）：
   ```kotlin
   class HomePage : Pager() {
       private val environment by lazy {
           WhatsNewEnvironment(versionStore = createWhatsNewVersionStore(), ...)
       }
   }
   ```
2. 自己实现 `WhatsNewVersionStore`，把已展示版本写进 Cookie 或服务端。

**Q：能不能不用 Kuikly 的 Web 渲染层？**
不能。`WhatsNewView` 是 Kuikly DSL 组件，最终由 `core-render-web` 转成
`div` / `text` / `scroll-view`。脱离 Kuikly 运行时它只是一个数据模型。
