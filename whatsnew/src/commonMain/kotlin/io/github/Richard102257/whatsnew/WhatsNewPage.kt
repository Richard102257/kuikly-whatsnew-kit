package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView
import com.tencent.kuikly.core.module.RouterModule

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

    init {
        pageName = "WhatsNewPage"
    }

    override fun body(): ViewBuilder = {
        val whatsNew = environment.whatsNew()

        if (whatsNew != null) {
            addChild(WhatsNewView()) {
                setup(whatsNew, onDismiss = {
                    environment.markAsPresented(whatsNew.version)
                    closePage()
                })
            }
        } else {
            addChild(createMainContent()) { }
        }
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
        content.addChild(title) { }

        val subtitle = TextView()
        subtitle.attr {
            text("All caught up! No new updates.")
            fontSize(14f)
            color(0xFF888888)
            textAlignCenter()
        }
        content.addChild(subtitle) { }

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
