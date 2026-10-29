package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView

/**
 * 全局默认环境，供 `router.openPage("WhatsNewPage")` 这类「无参创建」场景使用。
 *
 * 在 App 启动时设置一次即可：
 * ```
 * defaultWhatsNewEnvironment = WhatsNewEnvironment(
 *     versionStore = createPersistentWhatsNewVersionStore(),
 *     whatsNewCollection = collection
 * )
 * ```
 */
var defaultWhatsNewEnvironment: WhatsNewEnvironment? = null

/**
 * WhatsNew 独立页面
 *
 * 作为 Kuikly 页面直接展示 WhatsNew 内容，支持自动检测新版本。
 *
 * 使用方式一（推荐，直接创建）：
 * ```
 * WhatsNewPage(environment)
 * ```
 *
 * 使用方式二（路由打开，需先设置 [defaultWhatsNewEnvironment]）：
 * ```
 * router.openPage("WhatsNewPage")
 * ```
 */
class WhatsNewPage(
    private val injectedEnvironment: WhatsNewEnvironment? = defaultWhatsNewEnvironment
) : Pager() {

    init {
        pageName = "WhatsNewPage"
    }

    private val environment: WhatsNewEnvironment
        get() = injectedEnvironment ?: defaultWhatsNewEnvironment
            ?: error("WhatsNewPage: 请先设置 defaultWhatsNewEnvironment，或直接传入 environment")

    override fun body(): ViewBuilder = {
        val pending = this@WhatsNewPage.environment.whatsNew()

        if (pending != null) {
            addChild(WhatsNewView()) {
                setup(pending, onDismiss = {
                    this@WhatsNewPage.environment.markAsPresented(pending.version)
                    this@WhatsNewPage.closeSelf()
                })
            }
        } else {
            addChild(this@WhatsNewPage.createMainContent()) { }
        }
    }

    private fun createMainContent(): DivView {
        val content = DivView()
        content.attr {
            flex(1f)
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.CENTER)
            alignItems(FlexAlign.CENTER)
        }

        content.addChild(TextView()) {
            attr {
                text("Kuikly WhatsNewKit")
                fontSize(24f)
                fontWeightBold()
                color(0xFF667EEA)
                textAlignCenter()
            }
        }

        content.addChild(DivView()) {
            attr { height(8f) }
        }

        content.addChild(TextView()) {
            attr {
                text("All caught up! No new updates.")
                fontSize(14f)
                color(0xFF888888)
                textAlignCenter()
            }
        }

        return content
    }

    private fun closeSelf() {
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
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
        injectedEnvironment = WhatsNewEnvironment(
            versionStore = versionStore,
            whatsNewCollection = collection
        )
    )
}
