package com.kuikly.whatsnew

/**
 * 鸿蒙平台实现
 */
actual fun getCurrentVersion(): WhatsNewVersion {
    // 鸿蒙平台通过系统 API 获取版本号
    // 实际实现需要调用鸿蒙原生 API
    return WhatsNewVersion(1, 0, 0)
}

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore {
    return OhosWhatsNewVersionStore()
}

/**
 * 鸿蒙持久化存储实现
 * 使用鸿蒙 Preferences 或关系型数据库
 */
class OhosWhatsNewVersionStore : WhatsNewVersionStore {
    private val presentedVersions = mutableSetOf<String>()

    override fun savePresentedVersion(version: String) {
        presentedVersions.add(version)
        // TODO: 写入鸿蒙 Preferences
    }

    override fun getPresentedVersions(): Set<String> {
        return presentedVersions.toSet()
    }

    override fun hasPresented(version: String): Boolean {
        return presentedVersions.contains(version)
    }

    override fun clear() {
        presentedVersions.clear()
    }
}
