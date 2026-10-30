package io.github.Richard102257.whatsnew

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 纯逻辑单元测试：版本号解析 / 比较、内存版存储、DSL Builder 产物。
 *
 * 这些测试不依赖任何平台 API，Android / iOS / JVM 都能跑。
 */
class WhatsNewCoreTest {

    // ─── WhatsNewVersion ───

    @Test
    fun parsesMajorMinorPatch() {
        val version = WhatsNewVersion.parse("2.3.4")
        assertEquals(2, version.major)
        assertEquals(3, version.minor)
        assertEquals(4, version.patch)
    }

    @Test
    fun parseToleratesMissingOrInvalidParts() {
        assertEquals(WhatsNewVersion(2, 0, 0), WhatsNewVersion.parse("2"))
        assertEquals(WhatsNewVersion(2, 3, 0), WhatsNewVersion.parse("2.3"))
        assertEquals(WhatsNewVersion(0, 0, 0), WhatsNewVersion.parse("abc"))
        assertEquals(WhatsNewVersion(0, 0, 0), WhatsNewVersion.parse(""))
    }

    @Test
    fun comparesByMajorThenMinorThenPatch() {
        assertTrue(WhatsNewVersion(2, 0, 0) > WhatsNewVersion(1, 9, 9))
        assertTrue(WhatsNewVersion(1, 10, 0) > WhatsNewVersion(1, 9, 9))
        assertTrue(WhatsNewVersion(1, 9, 10) > WhatsNewVersion(1, 9, 9))
        assertEquals(0, WhatsNewVersion(1, 9, 9).compareTo(WhatsNewVersion(1, 9, 9)))
    }

    @Test
    fun rendersAsSemverString() {
        assertEquals("2.3.4", WhatsNewVersion(2, 3, 4).toString())
        assertEquals("2.3.0", WhatsNewVersion(2, 3).toString())
    }

    // ─── WhatsNewVersionStore ───

    @Test
    fun inMemoryStoreRemembersPresentedVersions() {
        val store = InMemoryWhatsNewVersionStore()
        assertFalse(store.hasPresented("1.0.0"))

        store.savePresentedVersion("1.0.0")
        assertTrue(store.hasPresented("1.0.0"))
        assertEquals(setOf("1.0.0"), store.getPresentedVersions())

        store.clear()
        assertFalse(store.hasPresented("1.0.0"))
        assertTrue(store.getPresentedVersions().isEmpty())
    }

    @Test
    fun inMemoryStoreIsIdempotent() {
        val store = InMemoryWhatsNewVersionStore()
        store.savePresentedVersion("1.0.0")
        store.savePresentedVersion("1.0.0")
        assertEquals(setOf("1.0.0"), store.getPresentedVersions())
    }

    // ─── DSL ───

    @Test
    fun builderProducesExpectedModel() {
        val whatsNew = whatsNew {
            version("2.1.0")
            title("What's New", color = 0xFF112233)
            feature(WhatsNewIcon.SystemIcon("star.fill"), "跨端", "一份代码跑多端")
            primaryAction {
                title = "Continue"
                backgroundColor = 0xFF6200EE
                foregroundColor = 0xFFFFFFFF
            }
            secondaryAction {
                title = "Learn More"
                openURL("https://example.com/changelog")
            }
        }

        assertEquals(WhatsNewVersion(2, 1, 0), whatsNew.version)
        assertEquals("What's New", whatsNew.title.text)
        assertEquals(0xFF112233, whatsNew.title.foregroundColor)
        assertEquals(1, whatsNew.features.size)
        assertEquals("跨端", whatsNew.features.first().title)
        assertEquals("Continue", whatsNew.primaryAction.title)

        val secondary = whatsNew.secondaryAction
        assertNotNull(secondary)
        val action = secondary.action
        assertTrue(action is SecondaryActionType.OpenURL)
        assertEquals("https://example.com/changelog", (action as SecondaryActionType.OpenURL).url)
    }

    @Test
    fun builderDefaultsToSingleFeatureListAndNoSecondaryAction() {
        val whatsNew = whatsNew { version(1, 0, 0) }

        assertEquals(WhatsNewVersion(1, 0, 0), whatsNew.version)
        assertEquals("What's New", whatsNew.title.text)
        assertTrue(whatsNew.features.isEmpty())
        assertNull(whatsNew.secondaryAction)
        assertEquals(WhatsNewLayout.default, whatsNew.layout)
    }

    @Test
    fun collectionBuilderKeepsDeclarationOrder() {
        val collection = whatsNewCollection {
            whatsNew("1.0.0") { title("first") }
            whatsNew("2.0.0") { title("second") }
            whatsNew(3, 0, 0) { title("third") }
        }

        assertEquals(3, collection.size)
        assertEquals(listOf("1.0.0", "2.0.0", "3.0.0"), collection.map { it.version.toString() })
        assertEquals(listOf("first", "second", "third"), collection.map { it.title.text })
    }

    @Test
    fun customLayoutOverridesDefaults() {
        val whatsNew = whatsNew {
            version("1.0.0")
            layout { copy(titleBottomSpacing = 99f) }
        }

        assertEquals(99f, whatsNew.layout.titleBottomSpacing)
    }

    // ─── KuiklySharedPreferencesWhatsNewVersionStore（Pager / 鸿蒙通用存储）───

    /** 模拟 SharedPreferencesModule 的读写，跨实例共享同一份后端 */
    private class FakePrefs {
        val values = mutableMapOf<String, String>()

        fun store() = KuiklySharedPreferencesWhatsNewVersionStore(
            readValue = { key -> values[key] ?: "" },
            writeValue = { key, value -> values[key] = value },
        )
    }

    @Test
    fun pagerStorePersistsAcrossInstances() {
        val prefs = FakePrefs()

        val first = prefs.store()
        assertFalse(first.hasPresented("1.2.0"))
        first.savePresentedVersion("1.2.0")
        assertTrue(first.hasPresented("1.2.0"))

        // 新实例 = 下次冷启动，仍能读到
        assertTrue(prefs.store().hasPresented("1.2.0"))
        assertEquals(setOf("1.2.0"), prefs.store().getPresentedVersions())
    }

    @Test
    fun pagerStoreUsesSameKeyAndFormatAsOtherPlatforms() {
        val prefs = FakePrefs()
        prefs.store().savePresentedVersion("1.0.0")
        prefs.store().savePresentedVersion("2.0.0")

        assertEquals("1.0.0,2.0.0", prefs.values["kuikly_whatsnew_presented_all"])
    }

    @Test
    fun pagerStoreClearRemovesEverything() {
        val prefs = FakePrefs()
        val store = prefs.store()
        store.savePresentedVersion("1.0.0")
        store.clear()

        assertFalse(prefs.store().hasPresented("1.0.0"))
        assertEquals(emptySet<String>(), prefs.store().getPresentedVersions())
    }

    @Test
    fun pagerStoreIgnoresDuplicateSaves() {
        val prefs = FakePrefs()
        val store = prefs.store()
        store.savePresentedVersion("1.0.0")
        store.savePresentedVersion("1.0.0")

        assertEquals("1.0.0", prefs.values["kuikly_whatsnew_presented_all"])
    }
}
