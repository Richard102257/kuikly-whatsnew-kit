# miniApp —— 微信小程序 Demo（Kotlin 业务模块）

这个目录是**真实的 Kotlin/JS 业务模块**，用来验证库在 JS 工具链上可用：

- `build.gradle.kts`：`js(IR)` + `androidTarget`、Kuikly Gradle 插件、KSP
- `src/jsMain/kotlin/io/github/Richard102257/whatsnew/demo/WhatsNewDemoPage.kt`：
  `@Page("WhatsNewDemoPage")` 的 Kuikly 页面，里面 `addChild(WhatsNewView())`

小程序 / H5 的**容器代码**（wxml 模板、自定义组件、`wx` 桥接）由 Kuikly 官方
`miniApp-js` / `h5App-js` 壳工程提供，本仓库不重复实现。

## 构建

```bash
# 编译
./gradlew :miniApp:compileKotlinJs

# 打 bundle（内置包）
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage
# → miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip

# 打分包（配合壳工程的按需加载）
./gradlew :miniApp:packSplitJSBundleDebug -PpageNameList=WhatsNewDemoPage
```

产物 `nativevue2.js` 里已经包含页面、`whatsnew` 库和 Kuikly core。

## 跑起来

1. 克隆 KuiklyUI，进入 `miniApp-js`：`npm install && npm run build`
2. 把 `nativevue2.js` 复制到 `miniApp-js/src/bundles/`，再 `npm run build`
3. 微信开发者工具导入 `miniApp-js/dist`（AppID 可用测试号）
4. `dist/pages/index/index.js` 里写：

```javascript
var render = global.render
render.renderView({ pageName: "WhatsNewDemoPage" })
```

H5 用 `h5App-js` 壳工程，`npm run dev` 后访问
`http://localhost:8080/?page_name=WhatsNewDemoPage`。

## 说明

- 微信开发者工具（开发版）读不到 `wx.getAccountInfoSync().miniProgram.version`，
  所以 Demo 页面里显式设置了 `whatsNewCurrentVersionOverride = "1.2.0"`；
  正式项目删掉这行即可。
- 完整接入说明见 [../docs/MINIAPP_zh.md](../docs/MINIAPP_zh.md)。
