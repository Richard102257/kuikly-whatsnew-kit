package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView
import com.tencent.kuikly.core.base.event.Event

/**
 * WhatsNew Kuikly DSL View 组件
 *
 * 跨端层声明，各平台侧通过 Render 注册实现原生渲染。
 * 组件名称通过 WhatsNewConst.VIEW_NAME 注册到 Kuikly Render。
 *
 * 使用方式（Kuikly DSL 页面中）：
 * ```
 * WhatsNewView {
 *     // 通过 attr 设置属性
 * }
 * ```
 *
 * 或者在 Compose DSL 中通过 MakeKuiklyComposeNode 嵌入：
 * ```
 * MakeKuiklyComposeNode<WhatsNewView>(
 *     factory = { WhatsNewView() },
 *     modifier = modifier,
 *     viewInit = { setup(whatsNew) }
 * )
 * ```
 */
class WhatsNewView : ViewContainer<ContainerAttr, Event>() {

    private var whatsNew: WhatsNew? = null
    private var dismissHandler: (() -> Unit)? = null

    override fun createAttr() = ContainerAttr()
    override fun createEvent() = Event()
    override fun viewName() = WhatsNewConst.VIEW_NAME

    /**
     * 设置 WhatsNew 数据并渲染
     */
    fun setup(whatsNew: WhatsNew, onDismiss: (() -> Unit)? = null) {
        this.whatsNew = whatsNew
        this.dismissHandler = onDismiss
        renderWhatsNew(whatsNew)
    }

    private fun renderWhatsNew(whatsNew: WhatsNew) {
        val layout = whatsNew.layout

        // 根容器样式
        attr {
            flex(1f)
            flexDirectionFlexColumn()
            justifyContentFlexStart()
            alignItemsStretch()
            backgroundColor(0xFFFFFFFF)
            paddingTop(layout.contentPadding.top)
            paddingLeft(layout.contentPadding.start)
            paddingRight(layout.contentPadding.end)
            paddingBottom(layout.contentPadding.bottom)
        }

        // 标题
        val titleView = TextView()
        titleView.attr {
            text(whatsNew.title.text)
            fontSize(32f)
            fontWeightBold()
            color(whatsNew.title.foregroundColor)
            textAlignCenter()
        }
        addChild(titleView) { }

        // 标题下方间距
        val titleSpacer = DivView()
        titleSpacer.attr { height(layout.titleBottomSpacing) }
        addChild(titleSpacer) { }

        // 功能列表
        val scrollContainer = DivView()
        scrollContainer.attr {
            flex(1f)
            flexDirectionFlexColumn()
            overflow(true)
        }

        whatsNew.features.forEachIndexed { index, feature ->
            scrollContainer.addChild(createFeatureView(feature)) { }
            if (index < whatsNew.features.size - 1) {
                val spacer = DivView()
                spacer.attr { height(layout.featureListSpacing) }
                scrollContainer.addChild(spacer) { }
            }
        }

        addChild(scrollContainer) { }

        // 底部按钮区域
        val footer = DivView()
        footer.attr {
            flexDirectionFlexColumn()
            widthPercent(100f)
        }

        // 底部间距
        val footerSpacer = DivView()
        footerSpacer.attr { height(layout.footerActionSpacing + 24f) }
        footer.addChild(footerSpacer) { }

        // 次按钮
        whatsNew.secondaryAction?.let { action ->
            footer.addChild(createSecondaryButton(action)) { }
            val actionSpacer = DivView()
            actionSpacer.attr { height(layout.footerActionSpacing) }
            footer.addChild(actionSpacer) { }
        }

        // 主按钮
        footer.addChild(createPrimaryButton(whatsNew.primaryAction)) { }

        addChild(footer) { }
    }

    private fun createFeatureView(feature: WhatsNewFeature): DivView {
        val container = DivView()
        container.attr {
            flexDirectionFlexRow()
            alignItemsFlexStart()
            widthPercent(100f)
        }

        // 图标容器
        val iconContainer = DivView()
        iconContainer.attr {
            width(56f)
            height(56f)
            borderRadius(14f)
            backgroundColor(0x1F6200EE)
            justifyContentCenter()
            alignItemsCenter()
        }

        // 图标（使用 TextView 显示 emoji 作为占位）
        val iconText = TextView()
        iconText.attr {
            text(feature.icon.emoji)
            fontSize(24f)
        }
        iconContainer.addChild(iconText) { }
        container.addChild(iconContainer) { }

        // 间距
        val spacer = DivView()
        spacer.attr { width(16f) }
        container.addChild(spacer) { }

        // 文本容器
        val textContainer = DivView()
        textContainer.attr {
            flex(1f)
            flexDirectionFlexColumn()
        }

        val titleView = TextView()
        titleView.attr {
            text(feature.title)
            fontSize(17f)
            fontWeight600()
            color(0xFF1A1A1A)
        }
        textContainer.addChild(titleView) { }

        feature.subtitle?.let { subtitle ->
            val subSpacer = DivView()
            subSpacer.attr { height(4f) }
            textContainer.addChild(subSpacer) { }

            val subtitleView = TextView()
            subtitleView.attr {
                text(subtitle)
                fontSize(15f)
                color(0xFF666666)
            }
            textContainer.addChild(subtitleView) { }
        }

        container.addChild(textContainer) { }
        return container
    }

    private fun createPrimaryButton(action: WhatsNewPrimaryAction): DivView {
        val button = DivView()
        button.attr {
            widthPercent(100f)
            height(action.cornerRadius * 4.3f)
            borderRadius(action.cornerRadius)
            backgroundColor(action.backgroundColor)
            justifyContentCenter()
            alignItemsCenter()
        }

        val btnText = TextView()
        btnText.attr {
            text(action.title)
            fontSize(17f)
            fontWeight600()
            color(action.foregroundColor)
        }
        button.addChild(btnText) { }

        button.event {
            click {
                action.onDismiss?.invoke()
                dismissHandler?.invoke()
            }
        }

        return button
    }

    private fun createSecondaryButton(action: WhatsNewSecondaryAction): DivView {
        val button = DivView()
        button.attr {
            widthPercent(100f)
            height(action.cornerRadius * 4f)
            borderRadius(action.cornerRadius)
            borderWidth(1f)
            borderColor(action.foregroundColor)
            justifyContentCenter()
            alignItemsCenter()
        }

        val btnText = TextView()
        btnText.attr {
            text(action.title)
            fontSize(16f)
            fontWeight500()
            color(action.foregroundColor)
        }
        button.addChild(btnText) { }

        button.event {
            click {
                when (action.action) {
                    is SecondaryActionType.OpenURL -> {
                        // 通过 Kuikly Module 打开 URL
                    }
                    is SecondaryActionType.Custom -> action.action.handler()
                }
            }
        }

        return button
    }
}

/**
 * 图标 emoji 扩展
 */
private val WhatsNewIcon.emoji: String
    get() = when (this) {
        is WhatsNewIcon.SystemIcon -> when (name) {
            "star.fill" -> "⭐"
            "heart.fill" -> "❤️"
            "bolt.fill" -> "⚡"
            "globe" -> "🌍"
            "wand.and.stars" -> "✨"
            "paintbrush.fill" -> "🎨"
            "bell.fill" -> "🔔"
            "gear" -> "⚙️"
            "lock.fill" -> "🔒"
            "camera.fill" -> "📷"
            "map.fill" -> "🗺️"
            "message.fill" -> "💬"
            "music.note" -> "🎵"
            "video.fill" -> "📹"
            "speedometer" -> "⚡"
            "flame.fill" -> "🔥"
            "leaf.fill" -> "🍃"
            "shield.fill" -> "🛡️"
            "trophy.fill" -> "🏆"
            else -> "⭐"
        }
        is WhatsNewIcon.UrlIcon -> "🖼️"
    }
