package io.github.Richard102257.whatsnew

/**
 * Web（H5）/ 微信小程序平台实现
 *
 * 这两个环境共用同一份 Kotlin/JS 代码：
 * - 有 `wx.getStorageSync` 时按微信小程序处理（微信小程序 / 部分小程序容器）
 * - 否则按浏览器处理（H5，使用 `localStorage`）
 *
 * 都不依赖 Kuikly 的页面上下文，因此可以在 App 启动时直接调用，
 * 不需要先创建 Pager。
 */

// ─────────────────────────────────────────────────────────────
// 版本号
// ─────────────────────────────────────────────────────────────

/**
 * 微信小程序版本号
 *
 * `wx.getAccountInfoSync().miniProgram.version` 只在正式版 / 体验版返回真实版本号，
 * 开发者工具（开发版）返回空字符串，此时继续走后面的兜底逻辑。
 */
private fun weChatMiniProgramVersion(): String? {
    val version: dynamic = js(
        """(function () {
             try {
               if (typeof wx === 'undefined' || typeof wx.getAccountInfoSync !== 'function') {
                 return null;
               }
               var info = wx.getAccountInfoSync();
               var v = info && info.miniProgram ? info.miniProgram.version : null;
               return (typeof v === 'string' && v.length > 0) ? v : null;
             } catch (e) {
               return null;
             }
           })()"""
    )
    return normalizeVersion(version as? String)
}

/**
 * H5：`<meta name="app-version" content="1.2.0">`
 */
private fun metaTagVersion(): String? {
    val version: dynamic = js(
        """(function () {
             try {
               if (typeof document === 'undefined' || !document.querySelector) {
                 return null;
               }
               var meta = document.querySelector('meta[name="app-version"]');
               return meta ? meta.getAttribute('content') : null;
             } catch (e) {
               return null;
             }
           })()"""
    )
    return normalizeVersion(version as? String)
}

/**
 * H5：`window.__WHATSNEW_VERSION__ = "1.2.0"`
 *
 * 适合由构建产物 / 服务端模板注入版本号。
 */
private fun globalVersion(): String? {
    val version: dynamic = js(
        """(function () {
             try {
               if (typeof window === 'undefined') {
                 return null;
               }
               return window.__WHATSNEW_VERSION__ || null;
             } catch (e) {
               return null;
             }
           })()"""
    )
    return normalizeVersion(version as? String)
}

/** 过滤掉 "" / "trial" / "develop" 这类非版本号返回值 */
private fun normalizeVersion(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return raw.takeIf { it.firstOrNull()?.isDigit() == true }
}

actual fun getCurrentVersion(): WhatsNewVersion {
    val detected = weChatMiniProgramVersion()
        ?: metaTagVersion()
        ?: globalVersion()
        ?: "1.0.0"
    return WhatsNewVersion.parse(detected)
}

// ─────────────────────────────────────────────────────────────
// 持久化存储
// ─────────────────────────────────────────────────────────────

private const val STORAGE_KEY = "kuikly_whatsnew_presented_all"

/**
 * 统一的 JS 存储后端
 *
 * 优先使用微信小程序的 `wx.getStorageSync` / `wx.setStorageSync`，
 * 其次使用浏览器的 `localStorage`，两者都不可用（隐私模式 / SSR / node）时
 * [persistent] 为 false，调用方退化为内存存储，保证组件不崩溃。
 */
internal class JsStorageBackend {

    private val impl: dynamic = js(
        """(function () {
             var hasWx = (typeof wx !== 'undefined'
                          && typeof wx.getStorageSync === 'function'
                          && typeof wx.setStorageSync === 'function');
             var hasLocal = false;
             try {
               hasLocal = (typeof localStorage !== 'undefined' && localStorage !== null);
             } catch (e) {
               hasLocal = false;
             }
             return {
               persistent: hasWx || hasLocal,
               wechat: hasWx,
               get: function (key) {
                 try {
                   if (hasWx) {
                     var v = wx.getStorageSync(key);
                     return (v === '' || v === null || v === undefined) ? null : String(v);
                   }
                   if (hasLocal) {
                     return localStorage.getItem(key);
                   }
                 } catch (e) {
                   return null;
                 }
                 return null;
               },
               set: function (key, value) {
                 try {
                   if (hasWx) {
                     wx.setStorageSync(key, value);
                     return true;
                   }
                   if (hasLocal) {
                     localStorage.setItem(key, value);
                     return true;
                   }
                 } catch (e) {
                   return false;
                 }
                 return false;
               }
             };
           })()"""
    )

    /** 当前环境是否具备持久化能力（微信小程序 storage 或浏览器 localStorage） */
    val persistent: Boolean
        get() = impl.persistent as Boolean

    /** 是否运行在微信小程序容器里 */
    val wechat: Boolean
        get() = impl.wechat as Boolean

    fun get(key: String): String? = impl.get(key) as? String

    fun set(key: String, value: String): Boolean = impl.set(key, value) as Boolean
}

/** 当前环境是否具备持久化能力（微信小程序 storage 或浏览器 localStorage） */
val isWhatsNewStoragePersistent: Boolean
    get() = JsStorageBackend().persistent

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore =
    JsWhatsNewVersionStore()

/**
 * Web / 小程序版本的 [WhatsNewVersionStore]
 *
 * 存储格式与 Android / iOS / 鸿蒙一致：一个逗号分隔的已展示版本列表。
 */
class JsWhatsNewVersionStore internal constructor(
    private val backend: JsStorageBackend
) : WhatsNewVersionStore {

    constructor() : this(JsStorageBackend())

    private val presented: MutableSet<String> = readAll()

    override fun savePresentedVersion(version: String) {
        if (!presented.add(version)) return
        writeAll()
    }

    override fun getPresentedVersions(): Set<String> = presented.toSet()

    override fun hasPresented(version: String): Boolean = presented.contains(version)

    override fun clear() {
        presented.clear()
        writeAll()
    }

    private fun readAll(): MutableSet<String> {
        val raw = backend.get(STORAGE_KEY)
        if (raw.isNullOrEmpty()) return mutableSetOf()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()
    }

    private fun writeAll() {
        backend.set(STORAGE_KEY, presented.joinToString(","))
    }
}
