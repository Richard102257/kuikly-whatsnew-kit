package com.kuikly.whatsnew

/**
 * Compose DSL 互操作层
 *
 * 提供在 Compose DSL 页面中嵌入 WhatsNewView 的能力。
 * 支持两种使用方式：
 *
 * 1. 自动展示（推荐）：
 * @Composable
 * fun MyApp() {
 *     WhatsNewComposeEnvironment(
 *         environment = myEnvironment,
 *         content = { MainContent() }
 *     )
 * }
 *
 * 2. 手动控制：
 * @Composable
 * fun MyScreen() {
 *     val provider = rememberWhatsNewProvider(environment)
 *     Button(onClick = { provider.autoPresentWhatsNew() }) {
 *         Text("Show What's New")
 *     }
 * }
 */

// ─── 状态管理 ───

/**
 * Compose 状态持有者
 */
class WhatsNewComposeState(
    val environment: WhatsNewEnvironment
) {
    var pendingWhatsNew: WhatsNew? = environment.whatsNew()
        private set

    var isPresenting: Boolean = false
        private set

    fun present(whatsNew: WhatsNew) {
        pendingWhatsNew = whatsNew
        isPresenting = true
    }

    fun dismiss() {
        pendingWhatsNew?.let { environment.markAsPresented(it.version) }
        pendingWhatsNew = null
        isPresenting = false
    }

    fun autoPresent() {
        val pending = environment.whatsNew()
        if (pending != null) {
            present(pending)
        }
    }
}

/**
 * 创建 Compose 状态（带 remember 语义）
 */
fun rememberWhatsNewState(
    environment: WhatsNewEnvironment
): WhatsNewComposeState {
    return WhatsNewComposeState(environment)
}

// ─── Provider ───

/**
 * Compose DSL 中的 WhatsNew 展示接口
 */
interface WhatsNewComposeProvider {
    fun presentWhatsNew(whatsNew: WhatsNew, onDismiss: () -> Unit = {})
    fun autoPresentWhatsNew()
    fun getPendingWhatsNew(): WhatsNew?
}

/**
 * 创建 Compose 提供者
 */
fun createWhatsNewComposeProvider(
    environment: WhatsNewEnvironment
): WhatsNewComposeProvider {
    return WhatsNewComposeProviderImpl(environment)
}

private class WhatsNewComposeProviderImpl(
    private val environment: WhatsNewEnvironment
) : WhatsNewComposeProvider {

    override fun presentWhatsNew(whatsNew: WhatsNew, onDismiss: () -> Unit) {
        environment.markAsPresented(whatsNew.version)
        onDismiss()
    }

    override fun autoPresentWhatsNew() {
        val pending = environment.whatsNew()
        if (pending != null) {
            presentWhatsNew(pending)
        }
    }

    override fun getPendingWhatsNew(): WhatsNew? {
        return environment.whatsNew()
    }
}

// ─── 弹窗/Sheet 展示 ───

/**
 * WhatsNew 展示方式
 */
enum class WhatsNewPresentationStyle {
    /** 全屏页面 */
    FULL_SCREEN,
    /** 底部弹出 Sheet */
    BOTTOM_SHEET,
    /** 居中弹窗 */
    DIALOG
}

/**
 * WhatsNew 弹窗配置
 */
data class WhatsNewSheetConfig(
    val style: WhatsNewPresentationStyle = WhatsNewPresentationStyle.BOTTOM_SHEET,
    val dismissOnOverlayTap: Boolean = true,
    val animationDurationMs: Int = 300
)
