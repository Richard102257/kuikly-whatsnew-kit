package io.github.Richard102257.whatsnew.android

import android.content.Context
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import io.github.Richard102257.whatsnew.*
import com.tencent.kuikly.core.export.IKuiklyRenderExport
import com.tencent.kuikly.render.export.KRRenderViewExport

/**
 * Android 平台侧 WhatsNew 组件实现
 *
 * 按照 Kuikly 扩展库规范，此模块实现 Android 平台原生渲染逻辑。
 * 需要在宿主 App 中注册到 Render：
 *
 * override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
 *     super.registerExternalRenderView(kuiklyRenderExport)
 *     with(kuiklyRenderExport) {
 *         renderViewExport(WhatsNewConst.COMPONENT_NAME, { context ->
 *             WhatsNewViewImpl(context)
 *         })
 *     }
 * }
 */
class WhatsNewViewImpl(context: Context) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        setBackgroundColor(0xFFFFFFFF.toInt())
    }

    /**
     * 绑定 WhatsNew 数据并渲染
     */
    fun bind(whatsNew: WhatsNew) {
        removeAllViews()

        val layout = whatsNew.layout
        val padStart = dp(layout.contentPadding.start)
        val padTop = dp(layout.contentPadding.top)
        val padEnd = dp(layout.contentPadding.end)
        val padBottom = dp(layout.contentPadding.bottom)
        setPadding(padStart, padTop, padEnd, padBottom)

        // 标题
        val titleView = TextView(context).apply {
            text = whatsNew.title.text
            textSize = 32f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(whatsNew.title.foregroundColor.toInt())
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(layout.titleBottomSpacing)
            }
        }
        addView(titleView)

        // 功能列表
        whatsNew.features.forEachIndexed { index, feature ->
            addView(createFeatureView(feature))
            if (index < whatsNew.features.size - 1) {
                addView(View(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(layout.featureListSpacing))
                })
            }
        }

        // 底部 spacer
        addView(View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(layout.footerActionSpacing + 24f))
        })

        // 次按钮
        whatsNew.secondaryAction?.let { action ->
            addView(createSecondaryButton(action))
            addView(View(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(layout.footerActionSpacing))
            })
        }

        // 主按钮
        addView(createPrimaryButton(whatsNew.primaryAction))
    }

    private fun createFeatureView(feature: WhatsNewFeature): View {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = android.view.Gravity.TOP

            // 图标
            val iconContainer = FrameLayout(context).apply {
                val size = dp(56f)
                layoutParams = LayoutParams(size, size)
                background = createRoundedRect(feature.icon.tintColor(), 0.12f, dp(8f))
            }
            val iconView = ImageView(context).apply {
                layoutParams = LayoutParams(dp(28f), dp(28f)).apply {
                    gravity = android.view.Gravity.CENTER
                }
                // 设置图标（实际项目中加载真实图标）
                setColorFilter(feature.icon.tintColor().toInt())
            }
            iconContainer.addView(iconView)
            addView(iconContainer)

            // 间距
            addView(View(context).apply {
                layoutParams = LayoutParams(dp(16f), LayoutParams.WRAP_CONTENT)
            })

            // 文本
            addView(LinearLayout(context).apply {
                orientation = VERTICAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

                addView(TextView(context).apply {
                    text = feature.title
                    textSize = 17f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(0xFF1A1A1A.toInt())
                })

                feature.subtitle?.let { subtitle ->
                    addView(View(context).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(4f))
                    })
                    addView(TextView(context).apply {
                        text = subtitle
                        textSize = 15f
                        setTextColor(0xFF666666.toInt())
                    })
                }
            })
        }
    }

    private fun createPrimaryButton(action: WhatsNewPrimaryAction): View {
        return TextView(context).apply {
            text = action.title
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(action.foregroundColor.toIntColor())
            gravity = android.view.Gravity.CENTER
            background = createRoundedRect(action.backgroundColor, 1f, dp(action.cornerRadius))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(whatsNew?.layout?.primaryButtonHeight ?: 52f))
            isClickable = true
            setOnClickListener { action.onDismiss?.invoke() }
        }
    }

    private fun createSecondaryButton(action: WhatsNewSecondaryAction): View {
        return TextView(context).apply {
            text = action.title
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(action.foregroundColor.toIntColor())
            gravity = android.view.Gravity.CENTER
            background = createRoundedRect(0x00000000, 0f, dp(action.cornerRadius))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(whatsNew?.layout?.secondaryButtonHeight ?: 48f))
            isClickable = true
            setOnClickListener {
                when (action.action) {
                    is SecondaryActionType.OpenURL -> { /* 打开 URL */ }
                    is SecondaryActionType.Custom -> action.action.handler()
                }
            }
        }
    }

    // ─── 辅助方法 ───
    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

    private fun createRoundedRect(color: Long, alpha: Float, radius: Float): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = dp(radius).toFloat()
            setColor(((color.toIntColor() and 0xFFFFFF) or ((alpha * 255).toInt() shl 24)))
        }
    }
}

/**
 * 图标颜色扩展
 */
private fun WhatsNewIcon.tintColor(): Long {
    return when (this) {
        is WhatsNewIcon.SystemIcon -> 0xFF6200EE
        is WhatsNewIcon.UrlIcon -> 0xFF6200EE
    }
}

/**
 * Long 颜色值转为 Android 可用的 Int 颜色
 * 将 0xFFRRGGBB 格式的 Long 转为 Android ColorInt
 */
fun Long.toIntColor(): Int = this.toInt()
