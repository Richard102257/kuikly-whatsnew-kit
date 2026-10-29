package com.kuikly.whatsnew

import com.kuikly.core.base.*
import com.kuikly.core.pager.Pager
import com.kuikly.core.views.DivView
import com.kuikly.core.views.TextView
import com.kuikly.core.event.Event
import com.kuikly.core.module.RouterModule

/**
 * WhatsNew 独立页面
 *
 * 可作为独立页面展示 WhatsNew 内容，支持自动检测新版本。
 *
 * 使用方式：
 * ```
 * // 在 RouterModule 中打开
 * router.openPage("WhatsNewPage", params)
 * ```
 */
class WhatsNewPage(
    private val environment: WhatsNewEnvironment
) : Pager() {

    override fun createRenderView() {
        super.createRenderView()

        val container = DivView()
        container.attr {
            flex(1f)
            flexDirectionFlexColumn()
            backgroundColor(0xFFFFFFFF)
        }

        // 获取需要展示的 WhatsNew
        val whatsNew = environment.whatsNew()

        if (whatsNew != null) {
            // 展示 WhatsNew
            val whatsNewView = WhatsNewView()
            whatsNewView.setup(whatsNew, onDismiss = {
                environment.markAsPresented(whatsNew.version)
                // 关闭页面
                closePage()
            })
            container.addSubView(whatsNewView)
        } else {
            // 没有需要展示的内容，显示主页面
            val mainContent = createMainContent()
            container.addSubView(mainContent)
        }

        getRenderView().addSubView(container)
    }

    private fun createMainContent(): DivView {
        val content = DivView()
        content.attr {
            flex(1f)
            flexDirectionFlexColumn()
            justifyContentCenter()
            alignItemsCenter()
        }

        val title = TextView()
        title.attr {
            text("Kuikly WhatsNewKit")
            fontSize(24f)
            fontWeightBold()
            color(0xFF667EEA)
            textAlignCenter()
        }
        content.addSubView(title)

        val subtitle = TextView()
        subtitle.attr {
            text("All caught up! No new updates.")
            fontSize(14f)
            color(0xFF888888)
            textAlignCenter()
        }
        content.addSubView(subtitle)

        return content
    }

    private fun closePage() {
        val router = acquireModule<RouterModule>(RouterModule.MODULE_NAME)
        router.closePage()
    }
}

/**
 * 便捷方法：创建带默认环境的 WhatsNewPage
 */
fun whatsNewPage(
    versionStore: WhatsNewVersionStore = InMemoryWhatsNewVersionStore(),
    collection: List<WhatsNew> = emptyList()
): WhatsNewPage {
    return WhatsNewPage(
        environment = WhatsNewEnvironment(
            versionStore = versionStore,
            whatsNewCollection = collection
        )
    )
}
