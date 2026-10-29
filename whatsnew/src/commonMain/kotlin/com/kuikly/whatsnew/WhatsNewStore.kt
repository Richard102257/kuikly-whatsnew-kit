package com.kuikly.whatsnew

/**
 * 版本存储协议
 * 各平台提供自己的持久化实现
 */
interface WhatsNewVersionStore {
    fun savePresentedVersion(version: String)
    fun getPresentedVersions(): Set<String>
    fun hasPresented(version: String): Boolean
    fun clear()
}

/**
 * 内存实现（测试用）
 */
class InMemoryWhatsNewVersionStore : WhatsNewVersionStore {
    private val presentedVersions = mutableSetOf<String>()

    override fun savePresentedVersion(version: String) {
        presentedVersions.add(version)
    }

    override fun getPresentedVersions(): Set<String> = presentedVersions.toSet()

    override fun hasPresented(version: String): Boolean = presentedVersions.contains(version)

    override fun clear() = presentedVersions.clear()
}

/**
 * 平台特定持久化存储
 */
expect fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore

/**
 * 环境管理：版本匹配 + 自动展示
 */
class WhatsNewEnvironment(
    private val versionStore: WhatsNewVersionStore = InMemoryWhatsNewVersionStore(),
    private val whatsNewCollection: List<WhatsNew> = emptyList(),
    private val defaultLayout: WhatsNewLayout = WhatsNewLayout.default
) {
    val currentVersion: WhatsNewVersion by lazy { WhatsNewVersion.current() }

    fun whatsNew(): WhatsNew? {
        return findMatchingWhatsNew(currentVersion)
    }

    private fun findMatchingWhatsNew(version: WhatsNewVersion): WhatsNew? {
        // 精确匹配
        val exactMatch = whatsNewCollection
            .filter { it.version == version }
            .sortedBy { it.version }
            .lastOrNull()

        if (exactMatch != null && !versionStore.hasPresented(exactMatch.version.toString())) {
            return applyDefaultLayout(exactMatch)
        }

        // Patch 版本回退
        if (version.patch != 0) {
            val fallbackVersion = WhatsNewVersion(version.major, version.minor, 0)
            val fallbackMatch = whatsNewCollection
                .filter { it.version == fallbackVersion }
                .sortedBy { it.version }
                .lastOrNull()

            if (fallbackMatch != null && !versionStore.hasPresented(fallbackMatch.version.toString())) {
                return applyDefaultLayout(fallbackMatch)
            }
        }

        // 最新未展示版本
        val latestUnpresented = whatsNewCollection
            .filter { !versionStore.hasPresented(it.version.toString()) }
            .filter { it.version <= version }
            .sortedByDescending { it.version }
            .firstOrNull()

        return latestUnpresented?.let { applyDefaultLayout(it) }
    }

    private fun applyDefaultLayout(whatsNew: WhatsNew): WhatsNew {
        return if (whatsNew.layout == WhatsNewLayout.default) {
            whatsNew.copy(layout = defaultLayout)
        } else {
            whatsNew
        }
    }

    fun markAsPresented(version: WhatsNewVersion) {
        versionStore.savePresentedVersion(version.toString())
    }

    fun hasPresented(version: WhatsNewVersion): Boolean {
        return versionStore.hasPresented(version.toString())
    }
}

/**
 * 集合 DSL Builder
 */
class WhatsNewCollectionBuilder {
    private val whatsNewList = mutableListOf<WhatsNew>()

    fun whatsNew(version: String, configure: WhatsNewBuilder.() -> Unit) {
        val builder = WhatsNewBuilder()
        builder.version(version)
        builder.configure()
        whatsNewList.add(builder.build())
    }

    fun whatsNew(major: Int, minor: Int, patch: Int = 0, configure: WhatsNewBuilder.() -> Unit) {
        val builder = WhatsNewBuilder()
        builder.version(major, minor, patch)
        builder.configure()
        whatsNewList.add(builder.build())
    }

    fun build(): List<WhatsNew> = whatsNewList.toList()
}

fun whatsNewCollection(configure: WhatsNewCollectionBuilder.() -> Unit): List<WhatsNew> {
    val builder = WhatsNewCollectionBuilder()
    builder.configure()
    return builder.build()
}
