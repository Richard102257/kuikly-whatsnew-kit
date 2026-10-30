# h5App —— H5 Demo 说明

H5 与微信小程序共用同一份 JS 产物：`miniApp` 模块编译出的 `nativevue2.js`
既能在小程序的 `wx` 容器里跑，也能在浏览器里跑，区别只在壳工程。

## 构建 bundle

```bash
./gradlew :miniApp:packLocalJSBundleDebug -PpageNameList=WhatsNewDemoPage
# → miniApp/build/outputs/kuikly/js/debug/local/nativevue2.zip
```

## 在浏览器里跑

H5 容器代码（`KuiklyRenderView`、bundle 清单、静态资源服务）由 Kuikly 官方
`h5App-js` 工程提供：

```bash
git clone https://github.com/Tencent-TDS/KuiklyUI
cd KuiklyUI/h5App-js
npm install
npm run build-bundles:dev -- WhatsNewDemoPage   # 或把 nativevue2.js 放进 src/bundles/
npm run dev
```

浏览器打开 `http://localhost:8080/?page_name=WhatsNewDemoPage`。

H5 侧的版本号来源（按优先级）：

```html
<!-- 方式 A：meta 标签 -->
<meta name="app-version" content="1.2.0">
```

```javascript
// 方式 B：构建期 / 服务端注入
window.__WHATSNEW_VERSION__ = "1.2.0"
```

已展示版本存放在 `localStorage`，键为 `kuikly_whatsnew_presented_all`。

完整说明见 [../docs/MINIAPP_zh.md](../docs/MINIAPP_zh.md)。
