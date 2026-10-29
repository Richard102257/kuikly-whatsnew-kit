package com.kuikly.whatsnew.ios

import com.kuikly.whatsnew.*
import com.kuikly.whatsnew.WhatsNewConst
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
 */
@Component(WhatsNewConst.COMPONENT_NAME)
class WhatsNewViewImpl : UIView {

    private var whatsNew: WhatsNew? = null
    private var onDismiss: (() -> Unit)? = null

    init {
        backgroundColor = UIColor.whiteColor
    }

    fun bind(whatsNew: WhatsNew, onDismiss: (() -> Unit)? = null) {
        this.whatsNew = whatsNew
        this.onDismiss = onDismiss
        setupUI(whatsNew)
    }

    private fun setupUI(whatsNew: WhatsNew) {
        // 标题
        val titleLabel = UILabel().apply {
            text = whatsNew.title.text
            font = UIFont.boldSystemFontOfSize(32.0)
            textColor = UIColor.blackColor
            textAlignment = NSTextAlignmentCenter
            frame = CGRectMake(24.0, 48.0, bounds.useContents { size.width - 48 }, 40.0)
        }
        addSubview(titleLabel)

        var yOffset = titleLabel.frame.useContents { origin.y + size.height + 32 }

        // 功能列表
        whatsNew.features.forEach { feature ->
            val featureView = createFeatureView(feature, yOffset)
            addSubview(featureView)
            yOffset += 76
        }

        // 按钮
        yOffset += 24
        whatsNew.secondaryAction?.let { action ->
            val btn = createSecondaryButton(action, yOffset)
            addSubview(btn)
            yOffset += 60
        }

        val primaryBtn = createPrimaryButton(whatsNew.primaryAction, yOffset)
        addSubview(primaryBtn)
    }

    private fun createFeatureView(feature: WhatsNewFeature, y: CGFloat): UIView {
        val container = UIView().apply {
            frame = CGRectMake(24.0, y, bounds.useContents { size.width - 48 }, 56.0)
        }

        // 图标背景
        val iconBg = UIView().apply {
            frame = CGRectMake(0.0, 0.0, 56.0, 56.0)
            backgroundColor = UIColor.colorWithRed(0.38f, green = 0.48f, blue = 0.93f, alpha = 0.12f)
            layer.cornerRadius = 14.0
        }
        container.addSubview(iconBg)

        // 标题
        val titleLabel = UILabel().apply {
            text = feature.title
            font = UIFont.systemFontOfSize(17.0, weight = UIFontWeightSemibold)
            textColor = UIColor.colorWithRed(0.1f, green = 0.1f, blue = 0.1f, alpha = 1.0f)
            frame = CGRectMake(72.0, 4.0, container.bounds.useContents { size.width - 72 }, 22.0)
        }
        container.addSubview(titleLabel)

        // 副标题
        feature.subtitle?.let { subtitle ->
            val subLabel = UILabel().apply {
                text = subtitle
                font = UIFont.systemFontOfSize(15.0)
                textColor = UIColor.colorWithRed(0.4f, green = 0.4f, blue = 0.4f, alpha = 1.0f)
                frame = CGRectMake(72.0, 30.0, container.bounds.useContents { size.width - 72 }, 20.0)
            }
            container.addSubview(subLabel)
        }

        return container
    }

    private fun createPrimaryButton(action: WhatsNewPrimaryAction, y: CGFloat): UIButton {
        return UIButton.buttonWithType(UIButtonTypeSystem).apply {
            frame = CGRectMake(24.0, y, bounds.useContents { size.width - 48 }, 52.0)
            setTitle(action.title, forState = UIControlStateNormal)
            titleLabel?.font = UIFont.systemFontOfSize(17.0, weight = UIFontWeightSemibold)
            setTitleColor(UIColor.whiteColor, forState = UIControlStateNormal)
            backgroundColor = UIColor.colorWithRed(0.38f, green = 0.48f, blue = 0.93f, alpha = 1.0f)
            layer.cornerRadius = 14.0
            addTarget(this, action = platform.ObjCObjectImpl.safe_sel("primaryTapped"), forControlEvents = UIControlEventTouchUpInside)
        }
    }

    private fun createSecondaryButton(action: WhatsNewSecondaryAction, y: CGFloat): UIButton {
        return UIButton.buttonWithType(UIButtonTypeSystem).apply {
            frame = CGRectMake(24.0, y, bounds.useContents { size.width - 48 }, 48.0)
            setTitle(action.title, forState = UIControlStateNormal)
            titleLabel?.font = UIFont.systemFontOfSize(16.0, weight = UIFontWeightMedium)
            setTitleColor(UIColor.colorWithRed(0.38f, green = 0.48f, blue = 0.93f, alpha = 1.0f), forState = UIControlStateNormal)
            addTarget(this, action = platform.ObjCObjectImpl.safe_sel("secondaryTapped"), forControlEvents = UIControlEventTouchUpInside)
        }
    }
}

/**
 * 组件注册宏（简化示意）
 */
annotation class Component(val name: String)
