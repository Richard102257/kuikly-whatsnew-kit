package io.github.Richard102257.whatsnew

/**
 * Kuikly 跨端组件 —— WhatsNew 核心接口
 *
 * 按照 Kuikly 扩展库规范，此模块定义跨端层逻辑。
 * 各平台侧实现通过 Render 注册，连接到此跨端接口。
 *
 * 工程结构（参照 Kuikly 官方扩展库指引）：
 * ├── whatsnew/              ← 本模块：KMP 跨端层
 * ├── whatsnewAndroid/       ← Android 平台侧实现
 * ├── whatsnewIOS/           ← iOS 平台侧实现（podspec）
 * ├── whatsnewOhos/          ← 鸿蒙平台侧实现
 * └── androidApp/            ← Demo & 验证工程
 */

/**
 * WhatsNew 组件名称常量
 * 用于跨平台注册和查找
 */
object WhatsNewConst {
    const val COMPONENT_NAME = "WhatsNewView"
    const val VIEW_NAME = "WhatsNewView"
}

/**
 * WhatsNew 版本信息
 */
data class WhatsNewVersion(
    val major: Int,
    val minor: Int,
    val patch: Int = 0
) : Comparable<WhatsNewVersion> {

    override fun compareTo(other: WhatsNewVersion): Int {
        if (major != other.major) return major - other.major
        if (minor != other.minor) return minor - other.minor
        return patch - other.patch
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(versionString: String): WhatsNewVersion {
            val parts = versionString.split(".")
            return WhatsNewVersion(
                major = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
                patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            )
        }

        fun current(): WhatsNewVersion =
            whatsNewCurrentVersionOverride?.let { parse(it) } ?: getCurrentVersion()
    }
}

/**
 * WhatsNew 功能特性描述
 */
data class WhatsNewFeature(
    val icon: WhatsNewIcon,
    val title: String,
    val subtitle: String? = null
)

/**
 * 图标来源
 */
sealed class WhatsNewIcon {
    data class SystemIcon(val name: String) : WhatsNewIcon()
    data class UrlIcon(val url: String) : WhatsNewIcon()
}

/**
 * 主按钮配置
 */
data class WhatsNewPrimaryAction(
    val title: String = "Continue",
    val backgroundColor: Long = 0xFF6200EE,
    val foregroundColor: Long = 0xFFFFFFFF,
    val cornerRadius: Float = 12f,
    val onDismiss: (() -> Unit)? = null
)

/**
 * 次按钮配置
 */
data class WhatsNewSecondaryAction(
    val title: String,
    val foregroundColor: Long = 0xFF6200EE,
    val action: SecondaryActionType,
    val cornerRadius: Float = 12f
)

sealed class SecondaryActionType {
    data class OpenURL(val url: String) : SecondaryActionType()
    data class Custom(val handler: () -> Unit) : SecondaryActionType()
}

/**
 * 布局配置
 */
data class WhatsNewLayout(
    val contentPadding: PaddingValues = PaddingValues(24f, 48f, 24f, 24f),
    val titleBottomSpacing: Float = 32f,
    val featureListSpacing: Float = 20f,
    val footerActionSpacing: Float = 16f,
    val primaryButtonHeight: Float = 52f,
    val secondaryButtonHeight: Float = 48f,
    val showsScrollIndicators: Boolean = true
) {
    companion object {
        var default: WhatsNewLayout = WhatsNewLayout()
    }
}

data class PaddingValues(
    val top: Float = 0f,
    val start: Float = 0f,
    val bottom: Float = 0f,
    val end: Float = 0f
)

/**
 * 标题配置
 */
data class WhatsNewTitle(
    val text: String,
    val foregroundColor: Long = 0xFF000000
)

/**
 * WhatsNew 完整数据模型
 */
data class WhatsNew(
    val version: WhatsNewVersion,
    val title: WhatsNewTitle,
    val features: List<WhatsNewFeature>,
    val primaryAction: WhatsNewPrimaryAction = WhatsNewPrimaryAction(),
    val secondaryAction: WhatsNewSecondaryAction? = null,
    val layout: WhatsNewLayout = WhatsNewLayout.default
)

/**
 * DSL Builder
 */
class WhatsNewBuilder {
    private var version: WhatsNewVersion = WhatsNewVersion.current()
    private var title: WhatsNewTitle = WhatsNewTitle("What's New")
    private val features = mutableListOf<WhatsNewFeature>()
    private var primaryAction: WhatsNewPrimaryAction = WhatsNewPrimaryAction()
    private var secondaryAction: WhatsNewSecondaryAction? = null
    private var layout: WhatsNewLayout = WhatsNewLayout.default

    fun version(versionString: String) = apply {
        version = WhatsNewVersion.parse(versionString)
    }

    fun version(major: Int, minor: Int, patch: Int = 0) = apply {
        version = WhatsNewVersion(major, minor, patch)
    }

    fun title(text: String, color: Long = 0xFF000000) = apply {
        title = WhatsNewTitle(text, color)
    }

    fun feature(
        icon: WhatsNewIcon,
        title: String,
        subtitle: String? = null
    ) = apply {
        features.add(WhatsNewFeature(icon, title, subtitle))
    }

    fun primaryAction(configure: WhatsNewPrimaryActionBuilder.() -> Unit) = apply {
        val builder = WhatsNewPrimaryActionBuilder(primaryAction)
        builder.configure()
        primaryAction = builder.build()
    }

    fun secondaryAction(configure: WhatsNewSecondaryActionBuilder.() -> Unit) = apply {
        val builder = WhatsNewSecondaryActionBuilder()
        builder.configure()
        secondaryAction = builder.build()
    }

    fun layout(configure: WhatsNewLayout.() -> WhatsNewLayout) = apply {
        layout = layout.configure()
    }

    fun build(): WhatsNew = WhatsNew(
        version = version,
        title = title,
        features = features.toList(),
        primaryAction = primaryAction,
        secondaryAction = secondaryAction,
        layout = layout
    )

    class WhatsNewPrimaryActionBuilder(private val source: WhatsNewPrimaryAction) {
        var title: String = source.title
        var backgroundColor: Long = source.backgroundColor
        var foregroundColor: Long = source.foregroundColor
        var cornerRadius: Float = source.cornerRadius
        var onDismiss: (() -> Unit)? = null

        fun build() = WhatsNewPrimaryAction(
            title = title,
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor,
            cornerRadius = cornerRadius,
            onDismiss = onDismiss
        )
    }

    class WhatsNewSecondaryActionBuilder {
        var title: String = ""
        var foregroundColor: Long = 0xFF6200EE
        var cornerRadius: Float = 12f
        private var action: SecondaryActionType? = null

        fun openURL(url: String) { action = SecondaryActionType.OpenURL(url) }
        fun custom(handler: () -> Unit) { action = SecondaryActionType.Custom(handler) }

        fun build() = WhatsNewSecondaryAction(
            title = title,
            foregroundColor = foregroundColor,
            action = action ?: SecondaryActionType.Custom {},
            cornerRadius = cornerRadius
        )
    }
}

fun whatsNew(configure: WhatsNewBuilder.() -> Unit): WhatsNew {
    val builder = WhatsNewBuilder()
    builder.configure()
    return builder.build()
}

/**
 * 平台特定：获取当前 App 版本
 */
expect fun getCurrentVersion(): WhatsNewVersion

/**
 * 手动覆盖当前版本号，优先级高于各平台的自动探测。
 *
 * Web / 微信小程序没有可靠的「原生版本号」来源（小程序开发版返回空字符串），
 * 这类场景建议在 App 启动时显式设置：
 * ```
 * whatsNewCurrentVersionOverride = "1.2.0"
 * ```
 * 设为 `null` 则回退到各平台的默认探测逻辑。
 */
var whatsNewCurrentVersionOverride: String? = null

