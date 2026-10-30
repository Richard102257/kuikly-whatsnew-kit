package io.github.Richard102257.whatsnew.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Demo 入口：直接打开 Kuikly 渲染的 [KuiklyDemoActivity]。
 *
 * 这里不再用原生 View 仿写 WhatsNew 界面 —— 那样截出来的图不是库的真实渲染结果。
 * 页面本身在 `:miniApp/src/commonMain`，与小程序 / H5 共用。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, KuiklyDemoActivity::class.java))
        finish()
    }
}
