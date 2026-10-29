package io.github.Richard102257.whatsnew.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.Richard102257.whatsnew.WhatsNewConst
import io.github.Richard102257.whatsnew.initKuiklyWhatsNew
import io.github.Richard102257.whatsnew.shared.demoWhatsNewCollection
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.createPersistentWhatsNewVersionStore
import io.github.Richard102257.whatsnew.android.WhatsNewViewImpl
import com.tencent.kuikly.export.IKuiklyRenderExport

/**
 * Demo 应用入口
 *
 * 演示如何注册 WhatsNew 组件并自动展示
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 WhatsNewKit
        initKuiklyWhatsNew(applicationContext)

        // 创建环境和版本存储
        val environment = WhatsNewEnvironment(
            versionStore = createPersistentWhatsNewVersionStore(),
            whatsNewCollection = demoWhatsNewCollection
        )

        // 检查是否有需要展示的 WhatsNew
        val whatsNew = environment.whatsNew()
        if (whatsNew != null) {
            // 展示 WhatsNew（实际项目中使用 Kuikly Render 注册）
            // 这里简化为直接展示
            showWhatsNew(whatsNew, environment)
        } else {
            showMainContent()
        }
    }

    private fun showWhatsNew(whatsNew: io.github.Richard102257.whatsnew.WhatsNew, environment: WhatsNewEnvironment) {
        val view = WhatsNewViewImpl(this)
        view.bind(whatsNew)
        setContentView(view)
    }

    private fun showMainContent() {
        setContentView(R.layout.activity_main)
    }
}
