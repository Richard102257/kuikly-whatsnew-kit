package io.github.Richard102257.whatsnew.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.Richard102257.whatsnew.WhatsNew
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.WhatsNewIcon
import io.github.Richard102257.whatsnew.createPersistentWhatsNewVersionStore
import io.github.Richard102257.whatsnew.initKuiklyWhatsNew
import io.github.Richard102257.whatsnew.shared.demoWhatsNewCollection

/**
 * Demo 应用入口
 *
 * 演示「版本匹配 + 持久化」这一层：
 * - 首次启动（versionName = 2.0.0）会匹配到 2.0.0 的 WhatsNew
 * - 点击 Continue 后写入 SharedPreferences，再次启动不再弹出
 *
 * 真正的 UI 由 Kuikly DSL 的 `WhatsNewView` 渲染（见 README「在页面中使用」），
 * 这里用原生 View 展示匹配结果，方便在没有 Kuikly 页面容器时验证逻辑。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getCurrentVersion() / createPersistentWhatsNewVersionStore() 依赖 Context
        initKuiklyWhatsNew(applicationContext)

        val environment = WhatsNewEnvironment(
            versionStore = createPersistentWhatsNewVersionStore(),
            whatsNewCollection = demoWhatsNewCollection
        )

        val pending = environment.whatsNew()
        if (pending != null) {
            showWhatsNew(pending, environment)
        } else {
            showMainContent()
        }
    }

    private fun showWhatsNew(whatsNew: WhatsNew, environment: WhatsNewEnvironment) {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        column.addView(text(whatsNew.title.text, 28f, Color.BLACK, bold = true))
        column.addView(text("v${whatsNew.version}", 13f, Color.GRAY))

        whatsNew.features.forEach { feature ->
            column.addView(
                text("${feature.icon.emojiForDemo()}  ${feature.title}", 16f, Color.BLACK, bold = true)
                    .apply { setPadding(0, dp(20), 0, 0) }
            )
            feature.subtitle?.let {
                column.addView(text(it, 14f, Color.DKGRAY))
            }
        }

        column.addView(
            Button(this).apply {
                text = whatsNew.primaryAction.title
                setBackgroundColor(whatsNew.primaryAction.backgroundColor.toInt())
                setTextColor(whatsNew.primaryAction.foregroundColor.toInt())
                setPadding(dp(16), dp(12), dp(16), dp(12))
                setOnClickListener {
                    environment.markAsPresented(whatsNew.version)
                    showMainContent()
                }
            }.also { it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(32) } }
        )

        setContentView(ScrollView(this).apply { addView(column) })
    }

    private fun showMainContent() {
        setContentView(R.layout.activity_main)
    }

    private fun text(value: String, sizeSp: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = sizeSp
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** 演示用：把 SystemIcon 名称映射成 emoji（库内部渲染用同一套映射） */
    private fun WhatsNewIcon.emojiForDemo(): String = when (this) {
        is WhatsNewIcon.SystemIcon -> when (name) {
            "star.fill" -> "⭐"
            "bolt.fill" -> "⚡"
            "globe" -> "🌍"
            "wand.and.stars" -> "✨"
            "paintbrush.fill" -> "🎨"
            "speedometer" -> "⚡"
            else -> "•"
        }
        is WhatsNewIcon.UrlIcon -> "🖼️"
    }
}
