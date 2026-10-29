package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView

/**
 * WhatsNew Kuikly DSL 组件
 *
 * 纯跨端 DSL 实现：通过 [ViewContainer] 组合 `DivView` / `TextView`，
 * 由 Kuikly 渲染为各平台原生控件，无需任何平台侧代码。
 *
 * 使用方式（Kuikly DSL）：
 * ```
 * addChild(WhatsNewView()) {
 *     setup(whatsNew) { /* 用户点击主按钮后的回调 */ }
 * }
 * ```
 */
class WhatsNewView : ViewContainer<ContainerAttr, Event>() {

    private var whatsNew: WhatsNew? = null
    private var dismissHandler: (() -> Unit)? = null

    override fun createAttr() = ContainerAttr()
    override fun createEvent() = Event()
    override fun viewName() = WhatsNewConst.VIEW_NAME

    /**
     * 绑定 WhatsNew 数据并渲染
     */
    fun setup(whatsNew: WhatsNew, onDismiss: (() -> Unit)? = null) {
        this.whatsNew = whatsNew
        this.dismissHandler = onDismiss
        renderWhatsNew(whatsNew)
    }

    private fun renderWhatsNew(whatsNew: WhatsNew) {
        val layout = whatsNew.layout

        // ─── 根容器 ───
        attr {
            flex(1f)
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.FLEX_START)
            alignItems(FlexAlign.STRETCH)
            backgroundColor(0xFFFFFFFF)
            padding(
                top = layout.contentPadding.top,
                left = layout.contentPadding.start,
                bottom = layout.contentPadding.bottom,
                right = layout.contentPadding.end
            )
        }

        // ─── 标题 ───
        addChild(TextView()) {
            attr {
                text(whatsNew.title.text)
                fontSize(32f)
                fontWeightBold()
                color(whatsNew.title.foregroundColor)
                textAlignCenter()
            }
        }

        // ─── 标题下方间距 ───
        addChild(DivView()) {
            attr { height(layout.titleBottomSpacing) }
        }

        // ─── 功能列表（可滚动区域）───
        addChild(DivView()) {
            attr {
                flex(1f)
                flexDirection(FlexDirection.COLUMN)
                alignItems(FlexAlign.STRETCH)
                overflow(true)
            }

            whatsNew.features.forEachIndexed { index, feature ->
                addChild(createFeatureView(feature)) { }
                if (index < whatsNew.features.size - 1) {
                    addChild(DivView()) {
                        attr { height(layout.featureListSpacing) }
                    }
                }
            }
        }

        // ─── 底部按钮区 ───
        addChild(DivView()) {
            attr {
                flexDirection(FlexDirection.COLUMN)
                alignItems(FlexAlign.STRETCH)
            }

            addChild(DivView()) {
                attr { height(layout.footerActionSpacing + 24f) }
            }

            whatsNew.secondaryAction?.let { action ->
                addChild(createSecondaryButton(action, layout)) { }
                addChild(DivView()) {
                    attr { height(layout.footerActionSpacing) }
                }
            }

            addChild(createPrimaryButton(whatsNew.primaryAction, layout)) { }
        }
    }

    // ─── 单个功能条目 ───

    private fun createFeatureView(feature: WhatsNewFeature): DivView {
        val container = DivView()
        container.attr {
            flexDirection(FlexDirection.ROW)
            alignItems(FlexAlign.FLEX_START)
        }

        // 图标容器
        container.addChild(DivView()) {
            attr {
                width(56f)
                height(56f)
                borderRadius(14f)
                backgroundColor(0x1F6200EE)
                flexDirection(FlexDirection.COLUMN)
                justifyContent(FlexJustifyContent.CENTER)
                alignItems(FlexAlign.CENTER)
            }

            addChild(TextView()) {
                attr {
                    text(feature.icon.emoji)
                    fontSize(24f)
                }
            }
        }

        // 间距
        container.addChild(DivView()) {
            attr { width(16f) }
        }

        // 文本
        container.addChild(DivView()) {
            attr {
                flex(1f)
                flexDirection(FlexDirection.COLUMN)
                alignItems(FlexAlign.FLEX_START)
            }

            addChild(TextView()) {
                attr {
                    text(feature.title)
                    fontSize(17f)
                    fontWeight600()
                    color(0xFF1A1A1A)
                }
            }

            feature.subtitle?.let { subtitle ->
                addChild(DivView()) {
                    attr { height(4f) }
                }
                addChild(TextView()) {
                    attr {
                        text(subtitle)
                        fontSize(15f)
                        color(0xFF666666)
                    }
                }
            }
        }

        return container
    }

    // ─── 主按钮 ───

    private fun createPrimaryButton(action: WhatsNewPrimaryAction, layout: WhatsNewLayout): DivView {
        val button = DivView()
        button.attr {
            height(layout.primaryButtonHeight)
            borderRadius(action.cornerRadius)
            backgroundColor(action.backgroundColor)
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.CENTER)
            alignItems(FlexAlign.CENTER)
        }

        button.addChild(TextView()) {
            attr {
                text(action.title)
                fontSize(17f)
                fontWeight600()
                color(action.foregroundColor)
            }
        }

        button.event {
            click {
                action.onDismiss?.invoke()
                this@WhatsNewView.dismissHandler?.invoke()
            }
        }

        return button
    }

    // ─── 次按钮 ───

    private fun createSecondaryButton(action: WhatsNewSecondaryAction, layout: WhatsNewLayout): DivView {
        val button = DivView()
        button.attr {
            height(layout.secondaryButtonHeight)
            borderRadius(action.cornerRadius)
            backgroundColor(0x00000000)
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.CENTER)
            alignItems(FlexAlign.CENTER)
        }

        button.addChild(TextView()) {
            attr {
                text(action.title)
                fontSize(16f)
                fontWeight500()
                color(action.foregroundColor)
            }
        }

        button.event {
            click {
                when (val type = action.action) {
                    // URL 打开由宿主通过 whatsNewUrlOpener 注入
                    is SecondaryActionType.OpenURL -> whatsNewUrlOpener?.invoke(type.url)
                    is SecondaryActionType.Custom -> type.handler()
                }
            }
        }

        return button
    }
}

/**
 * 打开 URL 的默认实现，宿主可替换（例如接到 Kuikly 的 RouterModule / 系统浏览器）。
 */
var whatsNewUrlOpener: ((String) -> Unit)? = null

/**
 * 图标 emoji 映射（无图片资源时作为占位展示）
 */
internal val WhatsNewIcon.emoji: String
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
