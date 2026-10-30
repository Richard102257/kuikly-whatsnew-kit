package com.tencent.kuikly.core.android

import com.tencent.kuikly.core.IKuiklyCoreEntry
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.nvi.NativeBridge

/**
 * Kuikly 在 Android 侧通过 `Class.forName("com.tencent.kuikly.core.android.KuiklyCoreEntry")`
 * 找到页面注册入口（见 `KuiklyRenderJvmContextHandler` 的静态初始化）。
 *
 * 正常情况下这个类由 Kuikly KSP 在**业务模块**里生成。但本项目是 KMP + KSP：
 * KSP 给 `androidTarget` 生成的类只会进 `aar_main_jar`，不进 AGP 的
 * `runtime_library_classes_jar`，下游 App 打包时会被丢掉 —— 运行时报
 * `ClassNotFoundException: com.tencent.kuikly.core.android.KuiklyCoreEntry`。
 *
 * 所以这里在 App 模块手写一份等价入口（内容与 KSP 生成的 `KuiklyCoreEntry` 一致）。
 * 页面本身仍然是 `:miniApp` 的 commonMain 代码，和小程序 / H5 共用。
 */
class KuiklyCoreEntry : IKuiklyCoreEntry {

    private var hadRegisterNativeBridge: Boolean = false

    override var delegate: IKuiklyCoreEntry.Delegate? = null

    override fun callKotlinMethod(
        methodId: Int,
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
    ) {
        if (!hadRegisterNativeBridge) {
            triggerRegisterPages()
            hadRegisterNativeBridge = true
            val nativeBridge = NativeBridge()
            nativeBridge.delegate = object : NativeBridge.NativeBridgeDelegate {
                override fun callNative(
                    methodId: Int,
                    arg0: Any?,
                    arg1: Any?,
                    arg2: Any?,
                    arg3: Any?,
                    arg4: Any?,
                    arg5: Any?,
                ): Any? = delegate?.callNative(methodId, arg0, arg1, arg2, arg3, arg4, arg5)
            }
            BridgeManager.registerNativeBridge(arg0 as String, nativeBridge)
        }
        BridgeManager.callKotlinMethod(methodId, arg0, arg1, arg2, arg3, arg4, arg5)
    }

    override fun triggerRegisterPages() {
        BridgeManager.registerPageRouter(PAGE_NAME) {
            io.github.Richard102257.whatsnew.demo.WhatsNewDemoPage()
        }
    }

    private companion object {
        const val PAGE_NAME = "WhatsNewDemoPage"
    }
}
