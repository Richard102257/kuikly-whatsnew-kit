package com.kuikly.whatsnew.ohos

import com.kuikly.whatsnew.*
import com.kuikly.core.export.KRRenderViewExportCreator
import com.tencent.kuikly.adapter.IKuiklyViewDelegate
import com.kuikly.core.base.*

/**
 * 鸿蒙平台侧 WhatsNew 组件实现
 *
 * 按照 Kuikly 扩展库规范，鸿蒙侧需要在宿主 App 中注册：
 *
 * export class KuiklyViewDelegate extends IKuiklyViewDelegate {
 *   getCustomRenderViewCreatorRegisterMap(): Map<string, KRRenderViewCreator> {
 *     const map = new Map();
 *     map.set(WhatsNewConst.VIEW_NAME, () => new WhatsNewOhsImpl());
 *     return map;
 *   }
 * }
 */
class WhatsNewOhsImpl : ViewContainer<ContainerAttr, Event>() {

    private var whatsNew: WhatsNew? = null
    private var dismissHandler: (() -> Unit)? = null

    override fun createAttr() = ContainerAttr()
    override fun createEvent() = Event()
    override fun viewName() = WhatsNewConst.VIEW_NAME

    fun bind(whatsNew: WhatsNew, onDismiss: (() -> Unit)? = null) {
        this.whatsNew = whatsNew
        this.dismissHandler = onDismiss
        renderView(whatsNew)
    }

    private fun renderView(whatsNew: WhatsNew) {
        attr {
            flex(1f)
            flexDirectionFlexColumn()
            backgroundColor(0xFFFFFFFF)
            paddingTop(whatsNew.layout.contentPadding.top)
            paddingLeft(whatsNew.layout.contentPadding.start)
            paddingRight(whatsNew.layout.contentPadding.end)
            paddingBottom(whatsNew.layout.contentPadding.bottom)
        }

        // 标题
        val title = TextView()
        title.attr {
            text(whatsNew.title.text)
            fontSize(32f)
            fontWeightBold()
            color(whatsNew.title.foregroundColor)
            textAlignCenter()
        }
        addSubView(title)

        // 间距
        val titleSpacer = DivView()
        titleSpacer.attr { height(whatsNew.layout.titleBottomSpacing) }
        addSubView(titleSpacer)

        // 功能列表
        whatsNew.features.forEachIndexed { index, feature ->
            addSubView(createFeatureView(feature))
            if (index < whatsNew.features.size - 1) {
                val spacer = DivView()
                spacer.attr { height(whatsNew.layout.featureListSpacing) }
                addSubView(spacer)
            }
        }

        // 底部间距
        val footerSpacer = DivView()
        footerSpacer.attr { height(whatsNew.layout.footerActionSpacing + 24f) }
        addSubView(footerSpacer)

        // 次按钮
        whatsNew.secondaryAction?.let { action ->
            addSubView(createSecondaryButton(action))
            val actionSpacer = DivView()
            actionSpacer.attr { height(whatsNew.layout.footerActionSpacing) }
            addSubView(actionSpacer)
        }

        // 主按钮
        addSubView(createPrimaryButton(whatsNew.primaryAction))
    }

    private fun createFeatureView(feature: WhatsNewFeature): DivView {
        return DivView().apply {
            attr {
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
                backgroundColor(0xFF6200EE, alpha = 0.12f)
                justifyContentCenter()
                alignItemsCenter()
            }
            addSubView(iconContainer)

            // 间距
            val spacer = DivView()
            spacer.attr { width(16f) }
            addSubView(spacer)

            // 文本
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
            textContainer.addSubView(titleView)

            feature.subtitle?.let { subtitle ->
                val subSpacer = DivView()
                subSpacer.attr { height(4f) }
                textContainer.addSubView(subSpacer)

                val subView = TextView()
                subView.attr {
                    text(subtitle)
                    fontSize(15f)
                    color(0xFF666666)
                }
                textContainer.addSubView(subView)
            }

            addSubView(textContainer)
        }
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
        button.addSubView(btnText)

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
        button.addSubView(btnText)

        button.event {
            click {
                when (action.action) {
                    is SecondaryActionType.OpenURL -> { /* 打开 URL */ }
                    is SecondaryActionType.Custom -> action.action.handler()
                }
            }
        }

        return button
    }
}

/**
 * 鸿蒙侧注册辅助类
 */
class WhatsNewOhosViewDelegate : IKuiklyViewDelegate {
    override fun getCustomRenderViewCreatorRegisterMap(): Map<String, KRRenderViewExportCreator> {
        return mapOf(
            WhatsNewConst.VIEW_NAME to { WhatsNewOhsImpl() }
        )
    }
}
