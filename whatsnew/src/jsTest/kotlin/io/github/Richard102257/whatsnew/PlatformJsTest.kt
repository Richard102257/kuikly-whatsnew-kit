package io.github.Richard102257.whatsnew

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Web / 微信小程序平台的 actual 实现测试
 *
 * 在 node 里伪造 `wx` / `localStorage` 全局对象，验证：
 * - 版本号探测（小程序 → H5 → 兜底）
 * - 存储后端选择（小程序 storage → localStorage → 内存）
 * - [WhatsNewEnvironment] 在 JS 上的自动展示流程
 *
 * 运行：`./gradlew :whatsnew:jsNodeTest`
 */
class PlatformJsTest {

    @BeforeTest
    fun resetGlobals() {
        whatsNewCurrentVersionOverride = null
        js(
            """(function () {
                 try { delete globalThis.wx; } catch (e) {}
                 try { delete globalThis.localStorage; } catch (e) {}
               })()"""
        )
    }

    // ─── 版本号探测 ───

    @Test
    fun readsVersionFromWeChatMiniProgram() {
        installWeChat(version = "3.2.1")
        assertEquals(WhatsNewVersion(3, 2, 1), getCurrentVersion())
    }

    @Test
    fun ignoresEmptyVersionReportedByDevTools() {
        // 微信开发者工具（开发版）返回空字符串
        installWeChat(version = "")
        assertEquals(WhatsNewVersion(1, 0, 0), getCurrentVersion())
    }

    @Test
    fun ignoresNonNumericWeChatVersion() {
        installWeChat(version = "trial")
        assertEquals(WhatsNewVersion(1, 0, 0), getCurrentVersion())
    }

    @Test
    fun overrideWinsOverPlatformDetection() {
        installWeChat(version = "3.2.1")
        whatsNewCurrentVersionOverride = "5.0.1"
        assertEquals(WhatsNewVersion(5, 0, 1), WhatsNewVersion.current())
    }

    // ─── 存储后端 ───

    @Test
    fun persistsToWeChatStorage() {
        installWeChat(version = "1.0.0")
        assertTrue(isWhatsNewStoragePersistent)

        val store = createPersistentWhatsNewVersionStore()
        assertFalse(store.hasPresented("1.2.0"))

        store.savePresentedVersion("1.2.0")
        assertTrue(store.hasPresented("1.2.0"))

        // 新实例 = 下次冷启动，仍能读到
        assertTrue(createPersistentWhatsNewVersionStore().hasPresented("1.2.0"))

        store.clear()
        assertFalse(createPersistentWhatsNewVersionStore().hasPresented("1.2.0"))
    }

    @Test
    fun persistsToLocalStorageInBrowser() {
        installLocalStorage()
        assertTrue(isWhatsNewStoragePersistent)

        JsWhatsNewVersionStore().savePresentedVersion("2.0.0")
        assertEquals(setOf("2.0.0"), JsWhatsNewVersionStore().getPresentedVersions())
    }

    @Test
    fun degradesToMemoryWithoutAnyStorage() {
        val store = JsWhatsNewVersionStore()
        assertFalse(isWhatsNewStoragePersistent)

        store.savePresentedVersion("1.0.0")
        assertTrue(store.hasPresented("1.0.0"))
        // 没有持久化能力时，新实例读不到（不崩溃即可）
        assertFalse(JsWhatsNewVersionStore().hasPresented("1.0.0"))
    }

    @Test
    fun keepsCommaSeparatedFormatCompatibleWithNativePlatforms() {
        installLocalStorage()
        JsWhatsNewVersionStore().savePresentedVersion("1.0.0")
        JsWhatsNewVersionStore().savePresentedVersion("2.0.0")

        val raw = js("globalThis.localStorage.getItem('kuikly_whatsnew_presented_all')") as String
        assertEquals("1.0.0,2.0.0", raw)
    }

    // ─── 端到端（环境 + 自动展示）───

    @Test
    fun environmentAutoPresentsOnceOnMiniProgram() {
        installWeChat(version = "2.0.0")

        val environment = WhatsNewEnvironment(
            versionStore = createPersistentWhatsNewVersionStore(),
            whatsNewCollection = whatsNewCollection {
                whatsNew("2.0.0") { title("v2") }
            }
        )

        assertEquals("v2", environment.whatsNew()?.title?.text)

        environment.markAsPresented(WhatsNewVersion(2, 0, 0))
        assertNull(environment.whatsNew())
        assertTrue(environment.hasPresented(WhatsNewVersion(2, 0, 0)))
    }
}

// ─────────────────────────────────────────────────────────────
// 伪造的运行时全局对象
// ─────────────────────────────────────────────────────────────

/** 安装一个内存版微信小程序 `wx`，带可选的版本号 */
private fun installWeChat(version: String) {
    val backing: dynamic = js("({})")

    val accountInfo: dynamic = js("({})")
    accountInfo.miniProgram = js("({})")
    accountInfo.miniProgram.version = version

    val wx: dynamic = js("({})")
    wx.getStorageSync = { key: String -> backing[key] ?: "" }
    wx.setStorageSync = { key: String, value: String -> backing[key] = value }
    wx.getAccountInfoSync = { accountInfo }

    js("globalThis").wx = wx
}

/** 安装一个内存版浏览器 `localStorage` */
private fun installLocalStorage() {
    val backing: dynamic = js("({})")

    val storage: dynamic = js("({})")
    storage.getItem = { key: String -> backing[key] ?: null }
    storage.setItem = { key: String, value: String -> backing[key] = value }
    storage.removeItem = { key: String -> js("(function(k){ delete globalThis.__ls_backing__[k]; })()") }

    js("globalThis").__ls_backing__ = backing
    js("globalThis").localStorage = storage
}
