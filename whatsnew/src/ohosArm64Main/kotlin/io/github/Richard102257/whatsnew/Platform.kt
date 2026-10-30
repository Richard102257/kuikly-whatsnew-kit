package io.github.Richard102257.whatsnew

/**
 * 鸿蒙平台实现
 *
 * 本库是纯 Kuikly DSL 组件，鸿蒙侧的渲染由 Kuikly 的 `core-render-ohos` 负责，
 * 这里只提供两处平台能力：
 *
 * 1. `getCurrentVersion()` —— **当前为占位实现**，鸿蒙的 `bundleManager` 是 ArkTS API，
 *    Kotlin/Native 侧没有直接绑定，所以这里固定返回 `1.0.0`。
 *    鸿蒙接入时请显式设置版本号：
 *    ```
 *    whatsNewCurrentVersionOverride = "1.2.0"
 *    ```
 *    （`WhatsNewVersion.current()` 会优先取这个值，所有平台通用。）
 *
 * 2. `createPersistentWhatsNewVersionStore()` —— 当前是**内存实现**，冷启动后丢失。
 *    需要跨会话保留时，在页面里改用 `createWhatsNewVersionStore()`
 *    （基于 Kuikly 内置的 `SharedPreferencesModule`，鸿蒙 render 层已实现）。
 *
 * 已知限制（未实机验证）：
 * - 构建需要 Kuikly 定制的 Kotlin `2.0.21-KBA-010`（标准 Kotlin 没有 `ohosArm64` 目标）
 * - `core-render-ohos` 没有发布到 Maven，需从 KuiklyUI 仓库源码构建，由宿主工程提供
 */

// ─── 版本获取 ───

/**
 * 获取当前 App 版本号
 *
 * 鸿蒙原生 `bundleManager.getBundleInfoForSelf()` 是 ArkTS API，
 * 这里没有绑定，固定返回 `1.0.0`。请用 `whatsNewCurrentVersionOverride` 指定真实版本。
 */
actual fun getCurrentVersion(): WhatsNewVersion {
    return try {
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
 * 从鸿蒙 BundleManager 获取版本号（占位）
 *
 * 对应的 ArkTS API：
 * ```
 * import bundleManager from '@ohos.bundle.bundleManager';
 * let info = bundleManager.getBundleInfoForSelf(bundleManager.BUNDLE_INFO_DEFAULT);
 * return info.versionName;
 * ```
 * Kotlin/Native 侧需要 cinterop 才能调用，本库暂未提供，返回空串让上层走兜底。
 */
private fun getOhosBundleVersion(): String {
    return ""
}

// ─── 持久化存储 ───

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore {
    return OhosWhatsNewVersionStore()
}

/**
 * 鸿蒙存储实现（**当前仅内存**）
 *
 * 进程内有效，冷启动后丢失 —— 即用户每次重新打开 App 都会再看到一次 WhatsNew。
 *
 * 需要真正落盘时，请在页面里改用 `createWhatsNewVersionStore()`
 * （基于 Kuikly 的 `SharedPreferencesModule`，鸿蒙 render 层已实现，跨端通用）：
 *
 * ```
 * class HomePage : Pager() {
 *     private val environment by lazy {
 *         WhatsNewEnvironment(versionStore = createWhatsNewVersionStore(), ...)
 *     }
 * }
 * ```
 *
 * 如果确实要走鸿蒙原生 `@ohos.data.preferences`，需要 Kotlin/Native cinterop，
 * 本库暂未提供；也可以在宿主侧实现 [WhatsNewVersionStore] 后注入
 * `WhatsNewEnvironment(versionStore = ...)`。
 */
class OhosWhatsNewVersionStore : WhatsNewVersionStore {

    private val memoryCache = mutableSetOf<String>()

    override fun savePresentedVersion(version: String) {
        memoryCache.add(version)
    }

    override fun getPresentedVersions(): Set<String> = memoryCache.toSet()

    override fun hasPresented(version: String): Boolean = memoryCache.contains(version)

    override fun clear() {
        memoryCache.clear()
    }
}
