package io.github.Richard102257.whatsnew.ios

import io.github.Richard102257.whatsnew.*
import io.github.Richard102257.whatsnew.WhatsNewConst
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*

/**
 * iOS 平台侧 WhatsNew 组件实现
 *
 * 按照 Kuikly 扩展库规范，此模块实现 iOS 平台原生渲染逻辑。
 * iOS 侧组件通过运行时暴露给 Kuikly，无需手动注册。
 *
 * 在 Podfile 中引入：
 * pod 'whatsnewIOS', :path => '../'
 *
 * 注意：@Component 注解由 Kuikly 代码生成工具处理。
 * 在 Kotlin/Native 中，通过 Objective-C 运行时自动暴露。
 */
class WhatsNewViewImpl : UIView {

    private var whatsNew: WhatsNew? = null
    private var onDismiss: (() -> Unit)? = null
    private var secondaryActionHandler: (() -> Unit)? = null

    init {
        backgroundColor = UIColor.whiteColor
    }

    /**
     * 绑定 WhatsNew 数据并渲染
     */
    fun bind(whatsNew: WhatsNew, onDismiss: (() -> Unit)? = null) {
        this.whatsNew = whatsNew
        this.onDismiss = onDismiss
        this.secondaryActionHandler = whatsNew.secondaryAction?.let { action ->
            {
                when (action.action) {
                    is SecondaryActionType.OpenURL -> {
                        // 通过 Kuikly Module 打开 URL
                        openURL(action.action.url)
                    }
                    is SecondaryActionType.Custom -> action.action.handler()
                }
            }
        }
        setupUI(whatsNew)
    }

    private fun setupUI(whatsNew: WhatsNew) {
        // 清除旧子视图
        subviews.forEach { it.removeFromSuperview() }

        val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
        val padHorizontal = 24.0
        val contentWidth = screenWidth - padHorizontal * 2

        // 标题
        val titleLabel = UILabel().apply {
            text = whatsNew.title.text
            font = UIFont.boldSystemFontOfSize(32.0)
            textColor = UIColor.colorWithRed(
                ((whatsNew.title.foregroundColor shr 16) and 0xFF) / 255.0,
                green = ((whatsNew.title.foregroundColor shr 8) and 0xFF) / 255.0,
                blue = (whatsNew.title.foregroundColor and 0xFF) / 255.0,
                alpha = 1.0
            )
            textAlignment = NSTextAlignmentCenter
            numberOfLines = 0
            frame = CGRectMake(padHorizontal, 48.0, contentWidth, 40.0)
        }
        addSubview(titleLabel)

        var yOffset = titleLabel.frame.useContents { origin.y + size.height + whatsNew.layout.titleBottomSpacing }

        // 功能列表
        whatsNew.features.forEachIndexed { index, feature ->
            val featureView = createFeatureView(feature, yOffset, contentWidth, padHorizontal)
            addSubview(featureView)
            yOffset += 76.0
            if (index < whatsNew.features.size - 1) {
                yOffset += whatsNew.layout.featureListSpacing
            }
        }

        // 底部间距
        yOffset += whatsNew.layout.footerActionSpacing + 24.0

        // 次按钮
        whatsNew.secondaryAction?.let { action ->
            val btn = createSecondaryButton(action, yOffset, contentWidth, padHorizontal)
            addSubview(btn)
            yOffset += 48.0 + whatsNew.layout.footerActionSpacing
        }

        // 主按钮
        val primaryBtn = createPrimaryButton(whatsNew.primaryAction, yOffset, contentWidth, padHorizontal)
        addSubview(primaryBtn)
    }

    private fun createFeatureView(feature: WhatsNewFeature, y: CGFloat, contentWidth: CGFloat, padX: CGFloat): UIView {
        val container = UIView().apply {
            frame = CGRectMake(padX, y, contentWidth, 56.0)
        }

        // 图标背景
        val tintColor = getIconTintColor(feature.icon)
        val iconBg = UIView().apply {
            frame = CGRectMake(0.0, 0.0, 56.0, 56.0)
            backgroundColor = UIColor.colorWithRed(
                ((tintColor shr 16) and 0xFF) / 255.0,
                green = ((tintColor shr 8) and 0xFF) / 255.0,
                blue = (tintColor and 0xFF) / 255.0,
                alpha = 0.12
            )
            layer.cornerRadius = 14.0
        }
        container.addSubview(iconBg)

        // 图标（emoji 占位）
        val iconLabel = UILabel().apply {
            text = getIconEmoji(feature.icon)
            font = UIFont.systemFontOfSize(24.0)
            textAlignment = NSTextAlignment.Center
            frame = CGRectMake(0.0, 16.0, 56.0, 24.0)
        }
        iconBg.addSubview(iconLabel)

        // 标题
        val titleLabel = UILabel().apply {
            text = feature.title
            font = UIFont.systemFontOfSize(17.0, weight = UIFontWeightSemibold)
            textColor = UIColor.colorWithRed(0.1, green = 0.1, blue = 0.1, alpha = 1.0)
            frame = CGRectMake(72.0, 4.0, contentWidth - 72.0, 22.0)
        }
        container.addSubview(titleLabel)

        // 副标题
        feature.subtitle?.let { subtitle ->
            val subLabel = UILabel().apply {
                text = subtitle
                font = UIFont.systemFontOfSize(15.0)
                textColor = UIColor.colorWithRed(0.4, green = 0.4, blue = 0.4, alpha = 1.0)
                frame = CGRectMake(72.0, 30.0, contentWidth - 72.0, 20.0)
            }
            container.addSubview(subLabel)
        }

        return container
    }

    private fun createPrimaryButton(action: WhatsNewPrimaryAction, y: CGFloat, contentWidth: CGFloat, padX: CGFloat): UIButton {
        return UIButton.buttonWithType(UIButtonTypeSystem).apply {
            frame = CGRectMake(padX, y, contentWidth, 52.0)
            setTitle(action.title, forState = UIControlStateNormal)
            titleLabel?.font = UIFont.systemFontOfSize(17.0, weight = UIFontWeightSemibold)
            setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
            backgroundColor = UIColor.colorWithRed(
                ((action.backgroundColor shr 16) and 0xFF) / 255.0,
                green = ((action.backgroundColor shr 8) and 0xFF) / 255.0,
                blue = (action.backgroundColor and 0xFF) / 255.0,
                alpha = 1.0
            )
            layer.cornerRadius = 14.0
            addTarget(this@WhatsNewViewImpl, sel_registerName("primaryButtonTapped"), forControlEvents = UIControlEventTouchUpInside)
        }
    }

    private fun createSecondaryButton(action: WhatsNewSecondaryAction, y: CGFloat, contentWidth: CGFloat, padX: CGFloat): UIButton {
        return UIButton.buttonWithType(UIButtonTypeSystem).apply {
            frame = CGRectMake(padX, y, contentWidth, 48.0)
            setTitle(action.title, forState = UIControlStateNormal)
            titleLabel?.font = UIFont.systemFontOfSize(16.0, weight = UIFontWeightMedium)
            setTitleColor(UIColor.colorWithRed(
                ((action.foregroundColor shr 16) and 0xFF) / 255.0,
                green = ((action.foregroundColor shr 8) and 0xFF) / 255.0,
                blue = (action.foregroundColor and 0xFF) / 255.0,
                alpha = 1.0
            ), forState = UIControlStateNormal)
            layer.cornerRadius = 12.0
            layer.borderWidth = 1.0
            layer.borderColor = UIColor.colorWithRed(
                ((action.foregroundColor shr 16) and 0xFF) / 255.0,
                green = ((action.foregroundColor shr 8) and 0xFF) / 255.0,
                blue = (action.foregroundColor and 0xFF) / 255.0,
                alpha = 1.0
            ).CGColor
            addTarget(this@WhatsNewViewImpl, sel_registerName("secondaryButtonTapped"), forControlEvents = UIControlEventTouchUpInside)
        }
    }

    // ─── 按钮响应 ───

    @ObjCAction
    fun primaryButtonTapped() {
        whatsNew?.primaryAction?.onDismiss?.invoke()
        onDismiss?.invoke()
    }

    @ObjCAction
    fun secondaryButtonTapped() {
        secondaryActionHandler?.invoke()
    }

    // ─── 辅助方法 ───

    private fun openURL(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    private fun getIconEmoji(icon: WhatsNewIcon): String {
        return when (icon) {
            is WhatsNewIcon.SystemIcon -> when (icon.name) {
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
    }

    private fun getIconTintColor(icon: WhatsNewIcon): Long {
        return when (icon) {
            is WhatsNewIcon.SystemIcon -> 0xFF6200EE
            is WhatsNewIcon.UrlIcon -> 0xFF6200EE
        }
    }
}
