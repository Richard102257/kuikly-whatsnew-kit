package com.kuikly.whatsnew

/**
 * Compose DSL 互操作层
 *
 * 提供在 Compose DSL 页面中嵌入 WhatsNewView 的能力。
 *
 * 使用方式：
 * ```
 * @Composable
 * fun MyScreen() {
 *     WhatsNewComposeView(
 *         environment = myEnvironment,
 *         onDismiss = { /* ... */ }
 *     )
 * }
 * ```
 *
 * 注意：此文件中的 Compose 互操作 API 需要在有 Compose 依赖的模块中实现。
 * 这里仅声明接口，实际实现在 compose-interop 子模块中。
 */

/**
 * Compose DSL 中的 WhatsNew 展示接口
 */
interface WhatsNewComposeProvider {
    /**
     * 展示 WhatsNew 弹窗
     */
    fun presentWhatsNew(whatsNew: WhatsNew, onDismiss: () -> Unit = {})

    /**
     * 自动检测并展示 WhatsNew
     */
    fun autoPresentWhatsNew()

    /**
     * 获取当前待展示的 WhatsNew
     */
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
        // 实际实现由 compose-interop 模块提供
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
