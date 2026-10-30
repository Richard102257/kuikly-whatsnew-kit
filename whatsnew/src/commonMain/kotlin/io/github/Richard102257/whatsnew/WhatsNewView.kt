package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.base.ViewConst
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
 *
 * ⚠️ 所有子视图都必须在 `addChild(xxx) { ... }` 的 init 闭包里构建。
 * Kuikly 的 `pagerId` 是在 `internalAddChild` 时从父容器继承的，
 * 「先 `DivView()` 出来、配置好、再 `addChild(它)`」会让 `attr {}` 静默失效
 * （pagerId 为空时 `attr` 直接 return），并且给它的子节点 `addChild` 会抛
 * `pager id is empty`。
 */
class WhatsNewView : ViewContainer<ContainerAttr, Event>() {

    private var whatsNew: WhatsNew? = null
    private var dismissHandler: (() -> Unit)? = null
    private var cardCornerRadius: Float = 0f

    override fun createAttr() = ContainerAttr()
    override fun createEvent() = Event()
    // 纯 DSL 组合组件：挂在内置的 "view" 原生节点上，不注册任何平台自定义 View
    override fun viewName() = ViewConst.TYPE_VIEW

    /**
     * 绑定 WhatsNew 数据并渲染
     *
     * @param cornerRadius 大于 0 时把整块内容画成圆角卡片（配合 [WhatsNewOverlay] 使用）
     */
    fun setup(
        whatsNew: WhatsNew,
        cornerRadius: Float = 0f,
        onDismiss: (() -> Unit)? = null,
    ) {
        this.whatsNew = whatsNew
        this.cardCornerRadius = cornerRadius
        this.dismissHandler = onDismiss
        renderWhatsNew(whatsNew)
    }

    private fun renderWhatsNew(whatsNew: WhatsNew) {
        val layout = whatsNew.layout
        val radius = cardCornerRadius

        // ─── 根容器 ───
        attr {
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.FLEX_START)
            alignItems(FlexAlign.STRETCH)
            backgroundColor(0xFFFFFFFF)
            if (radius > 0f) {
                // 卡片模式：高度由内容撑开，交给 WhatsNewOverlay 居中
                borderRadius(radius)
                overflow(true)
            } else {
                // 整页模式：铺满宿主页面
                flex(1f)
            }
            padding(
                top = layout.contentPadding.top,
                left = layout.contentPadding.start,
                bottom = layout.contentPadding.bottom,
                right = layout.contentPadding.end
            )
        }

        // ─── 标题 ───
        // 用一层 flex 容器做水平居中：Kuikly 的 TextView 默认按内容宽度测量，
        // 只写 textAlignCenter() 在父容器里并不会真的居中。
        addChild(DivView()) {
            attr {
                flexDirection(FlexDirection.COLUMN)
                alignItems(FlexAlign.CENTER)
            }

            addChild(TextView()) {
                attr {
                    text(whatsNew.title.text)
                    fontSize(32f)
                    fontWeightBold()
                    color(whatsNew.title.foregroundColor)
                    textAlignCenter()
                    // Kuikly 默认单行 + 省略号，这里放开到 10 行（lines(0) 在 Android 上会
                    // 让 TextView.setMaxLines(0)，整段文字直接不显示）
                    lines(10)
                    textOverFlowWordWrapping()
                }
            }
        }

        // ─── 标题下方间距 ───
        addChild(DivView()) {
            attr { height(layout.titleBottomSpacing) }
        }

        // ─── 功能列表（整页模式下占满剩余高度并可滚动；卡片模式下按内容撑开）───
        addChild(DivView()) {
            attr {
                if (radius <= 0f) {
                    flex(1f)
                }
                flexDirection(FlexDirection.COLUMN)
                alignItems(FlexAlign.STRETCH)
                overflow(true)
            }

            whatsNew.features.forEachIndexed { index, feature ->
                addFeatureView(feature)
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
                addSecondaryButton(action, layout)
                addChild(DivView()) {
                    attr { height(layout.footerActionSpacing) }
                }
            }

            addPrimaryButton(whatsNew.primaryAction, layout, this@WhatsNewView.dismissHandler)
        }
    }
}

// ─── 单个功能条目 ───

private fun ViewContainer<*, *>.addFeatureView(feature: WhatsNewFeature) {
        addChild(DivView()) {
            attr {
                flexDirection(FlexDirection.ROW)
                alignItems(FlexAlign.FLEX_START)
            }

            // 图标容器
            addChild(DivView()) {
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
            addChild(DivView()) {
                attr { width(16f) }
            }

            // 文本
            addChild(DivView()) {
                attr {
                    flex(1f)
                    flexDirection(FlexDirection.COLUMN)
                    alignItems(FlexAlign.STRETCH)
                }

                addChild(TextView()) {
                    attr {
                        text(feature.title)
                        fontSize(17f)
                        fontWeight600()
                        color(0xFF1A1A1A)
                        lines(10)
                        textOverFlowWordWrapping()
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
                            lines(10)
                            textOverFlowWordWrapping()
                        }
                    }
                }
            }
        }
    }

// ─── 主按钮 ───

private fun ViewContainer<*, *>.addPrimaryButton(
    action: WhatsNewPrimaryAction,
    layout: WhatsNewLayout,
    dismissHandler: (() -> Unit)?,
) {
        addChild(DivView()) {
            attr {
                height(layout.primaryButtonHeight)
                borderRadius(action.cornerRadius)
                backgroundColor(action.backgroundColor)
                flexDirection(FlexDirection.COLUMN)
                justifyContent(FlexJustifyContent.CENTER)
                alignItems(FlexAlign.CENTER)
            }

            addChild(TextView()) {
                attr {
                    text(action.title)
                    fontSize(17f)
                    fontWeight600()
                    color(action.foregroundColor)
                }
            }

            event {
                click {
                    action.onDismiss?.invoke()
                    dismissHandler?.invoke()
                }
            }
        }
    }

// ─── 次按钮 ───

private fun ViewContainer<*, *>.addSecondaryButton(
    action: WhatsNewSecondaryAction,
    layout: WhatsNewLayout,
) {
        addChild(DivView()) {
            attr {
                height(layout.secondaryButtonHeight)
                borderRadius(action.cornerRadius)
                backgroundColor(0x00000000)
                flexDirection(FlexDirection.COLUMN)
                justifyContent(FlexJustifyContent.CENTER)
                alignItems(FlexAlign.CENTER)
            }

            addChild(TextView()) {
                attr {
                    text(action.title)
                    fontSize(16f)
                    fontWeight500()
                    color(action.foregroundColor)
                }
            }

            event {
                click {
                    when (val type = action.action) {
                        // URL 打开由宿主通过 whatsNewUrlOpener 注入
                        is SecondaryActionType.OpenURL -> whatsNewUrlOpener?.invoke(type.url)
                        is SecondaryActionType.Custom -> type.handler()
                    }
                }
        }
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
