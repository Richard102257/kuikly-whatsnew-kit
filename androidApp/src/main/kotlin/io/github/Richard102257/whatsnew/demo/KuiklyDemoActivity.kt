package io.github.Richard102257.whatsnew.demo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import io.github.Richard102257.whatsnew.initKuiklyWhatsNew

/**
 * 用 Kuikly 渲染真实页面 [WhatsNewDemoPage]
 *
 * 页面代码在 `:miniApp` 的 commonMain 里，和微信小程序 / H5 用的是**同一份**，
 * 所以这里截出来的图就是库真实渲染出来的 UI，不是原生 View 仿写的。
 *
 * 这里走 Kuikly 官方推荐的「页面粒度接入」：`KuiklyRenderViewBaseDelegator.onAttach`。
 */
class KuiklyDemoActivity : AppCompatActivity(), KuiklyRenderViewBaseDelegatorDelegate {

    private lateinit var delegator: KuiklyRenderViewBaseDelegator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getCurrentVersion() / createPersistentWhatsNewVersionStore() 依赖 Context
        initKuiklyWhatsNew(applicationContext)

        // 白色状态栏 + 深色图标，截图时不会出现主题色条。
        // 必须在 setContentView 之后调用：之前 decorView 还没建好，insetsController 是 null。
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        val container = FrameLayout(this)
        setContentView(container)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        delegator = KuiklyRenderViewBaseDelegator(this)
        delegator.onAttach(
            container = container,
            contextCode = "",
            pageName = PAGE_NAME,
            pageData = emptyMap(),
        )
    }

    override fun onResume() {
        super.onResume()
        if (::delegator.isInitialized) delegator.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::delegator.isInitialized) delegator.onPause()
    }

    override fun onDestroy() {
        if (::delegator.isInitialized) delegator.onDetach()
        super.onDestroy()
    }

    private companion object {
        const val PAGE_NAME = "WhatsNewDemoPage"
    }
}
