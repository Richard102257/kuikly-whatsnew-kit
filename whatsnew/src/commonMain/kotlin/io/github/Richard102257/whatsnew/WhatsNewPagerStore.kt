package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.pager.Pager

/**
 * 基于 Kuikly [SharedPreferencesModule] 的跨端存储实现
 *
 * 和 `createPersistentWhatsNewVersionStore()` 的区别：
 *
 * | | `createPersistentWhatsNewVersionStore()` | 本类 / [createWhatsNewVersionStore] |
 * |---|---|---|
 * | 平台覆盖 | Android / iOS / H5 / 小程序 | **所有平台**，含鸿蒙 |
 * | 调用时机 | App 启动时（无需页面） | 必须在页面内（需要 Pager） |
 * | 底层 | 各平台 `actual` 实现 | Kuikly 官方模块（各 render 层自带） |
 *
 * 鸿蒙侧 `createPersistentWhatsNewVersionStore()` 目前只有内存实现，
 * 需要跨冷启动保留「已展示版本」时请改用本类（在页面里创建）。
 *
 * 存储格式与其它实现一致：一个逗号分隔的已展示版本列表。
 */
class KuiklySharedPreferencesWhatsNewVersionStore internal constructor(
    private val readValue: (String) -> String,
    private val writeValue: (String, String) -> Unit,
) : WhatsNewVersionStore {

    /**
     * @param prefs 通过 `acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)` 获取
     */
    constructor(prefs: SharedPreferencesModule) : this(
        readValue = { key -> prefs.getString(key) },
        writeValue = { key, value -> prefs.setString(key, value) },
    )

    override fun savePresentedVersion(version: String) {
        val presented = readAll()
        if (!presented.add(version)) return
        writeAll(presented)
    }

    override fun getPresentedVersions(): Set<String> = readAll()

    override fun hasPresented(version: String): Boolean = readAll().contains(version)

    override fun clear() {
        writeValue(KEY, "")
    }

    private fun readAll(): MutableSet<String> {
        val raw = readValue(KEY)
        if (raw.isEmpty()) return mutableSetOf()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()
    }

    private fun writeAll(presented: Set<String>) {
        writeValue(KEY, presented.joinToString(","))
    }

    private companion object {
        const val KEY = "kuikly_whatsnew_presented_all"
    }
}

/**
 * 在 Kuikly 页面里创建持久化存储（所有平台通用，含鸿蒙）
 *
 * `SharedPreferencesModule` 是 Kuikly 内置模块，Android / iOS / 鸿蒙 / H5 / 小程序
 * 的 render 层各自实现了它，因此不需要任何 `expect/actual`。
 *
 * 用法（页面内）：
 * ```
 * class HomePage : Pager() {
 *     private val environment by lazy {
 *         WhatsNewEnvironment(
 *             versionStore = createWhatsNewVersionStore(),   // this: Pager
 *             whatsNewCollection = collection
 *         )
 *     }
 * }
 * ```
 *
 * 注意：本方法依赖 Pager，只能在页面内调用；App 启动阶段（还没有 Pager 时）
 * 请用 `createPersistentWhatsNewVersionStore()`。
 */
fun Pager.createWhatsNewVersionStore(): WhatsNewVersionStore {
    val prefs = acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    return KuiklySharedPreferencesWhatsNewVersionStore(prefs)
}
