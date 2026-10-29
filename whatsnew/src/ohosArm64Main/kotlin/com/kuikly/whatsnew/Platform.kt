package com.kuikly.whatsnew

/**
 * 鸿蒙平台实现
 *
 * 鸿蒙侧通过 Kuikly Render 注册自定义组件。
 * 版本获取和持久化使用鸿蒙原生 API。
 *
 * 使用方式：
 * 在宿主 App 的 KuiklyViewDelegate 中注册：
 * override fun getCustomRenderViewCreatorRegisterMap(): Map<String, KRRenderViewCreator> {
 *     val map = mutableMapOf<String, KRRenderViewCreator>()
 *     map[WhatsNewConst.VIEW_NAME] = { WhatsNewOhsImpl() }
 *     return map
 * }
 */

// ─── 版本获取 ───

/**
 * 获取当前 App 版本号
 *
 * 优先从鸿蒙 BundleManager 获取，失败则返回默认值
 */
actual fun getCurrentVersion(): WhatsNewVersion {
    return try {
        // 通过鸿蒙 OH_GetBundleInfo 获取版本
        // 实际实现需要调用 @ohos.bundle.bundleManager
        val versionName = getOhosBundleVersion()
        if (versionName.isNotEmpty()) {
            WhatsNewVersion.parse(versionName)
        } else {
            WhatsNewVersion(1, 0, 0)
        }
    } catch (e: Exception) {
        WhatsNewVersion(1, 0, 0)
    }
}

/**
 * 从鸿蒙 BundleManager 获取版本号
 *
 * 对应鸿蒙 API:
 * import bundleManager from '@ohos.bundle.bundleManager';
 * let bundleInfo = bundleManager.getBundleInfoForSelf(bundleManager.BUNDLE_INFO_DEFAULT);
 * return bundleInfo.versionName;
 */
private fun getOhosBundleVersion(): String {
    // 实际项目中通过鸿蒙原生 API 获取
    // 此处为占位实现，实际由鸿蒙侧原生代码注入
    return ""
}

// ─── 持久化存储 ───

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore {
    return OhosWhatsNewVersionStore()
}

/**
 * 鸿蒙持久化存储实现
 *
 * 使用鸿蒙 Preferences (关系型存储) 实现跨进程持久化
 *
 * 对应鸿蒙 API:
 * import dataPreferences from '@ohos.data.preferences';
 * let prefs = await dataPreferences.getPreferences(context, 'whatsly_whatsnew');
 */
class OhosWhatsNewVersionStore : WhatsNewVersionStore {

    private val memoryCache = mutableSetOf<String>()
    private var prefsLoaded = false

    /**
     * 懒加载：首次访问时从鸿蒙 Preferences 读取
     */
    private fun ensurePrefsLoaded() {
        if (!prefsLoaded) {
            loadFromPreferences()
            prefsLoaded = true
        }
    }

    override fun savePresentedVersion(version: String) {
        ensurePrefsLoaded()
        memoryCache.add(version)
        saveToPreferences(version)
    }

    override fun getPresentedVersions(): Set<String> {
        ensurePrefsLoaded()
        return memoryCache.toSet()
    }

    override fun hasPresented(version: String): Boolean {
        ensurePrefsLoaded()
        return memoryCache.contains(version)
    }

    override fun clear() {
        memoryCache.clear()
        clearPreferences()
    }

    // ─── 鸿蒙 Preferences 操作（实际项目中替换为原生 API 调用）───

    private fun loadFromPreferences() {
        // 实际实现:
        // import dataPreferences from '@ohos.data.preferences';
        // let prefs = await dataPreferences.getPreferences(context, 'kuikly_whatsnew');
        // let all = await prefs.get('presented_versions', '') as string;
        // if (all.isNotEmpty()) memoryCache.addAll(all.split(','))
    }

    private fun saveToPreferences(version: String) {
        // 实际实现:
        // await prefs.put('presented_versions', memoryCache.joinToString(","));
        // await prefs.flush();
    }

    private fun clearPreferences() {
        // 实际实现:
        // await prefs.clear();
        // await prefs.flush();
    }
}
