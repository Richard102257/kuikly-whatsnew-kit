package io.github.Richard102257.whatsnew.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.WhatsNewIcon
import io.github.Richard102257.whatsnew.WhatsNewVersion
import io.github.Richard102257.whatsnew.WhatsNewView
import io.github.Richard102257.whatsnew.createPersistentWhatsNewVersionStore
import io.github.Richard102257.whatsnew.whatsNewCollection
import io.github.Richard102257.whatsnew.whatsNewCurrentVersionOverride

/**
 * 微信小程序 / H5 Demo 页面
 *
 * 这是「宿主页面」的写法：页面自己负责把 [WhatsNewView] 挂到视图树上，
 * 库不依赖宿主的路由 / KSP 注册，所以任何 Kuikly 页面都能直接复用。
 *
 * 版本号：小程序开发版拿不到 `wx.getAccountInfoSync().miniProgram.version`，
 * Demo 里显式覆盖；正式项目里由 [getCurrentVersion] 自动读取。
 */
@Page("WhatsNewDemoPage")
class WhatsNewDemoPage : Pager() {

    private val environment = WhatsNewEnvironment(
        versionStore = createPersistentWhatsNewVersionStore(),
        whatsNewCollection = whatsNewCollection {
            whatsNew("1.2.0") {
                title("What's New in 1.2.0")
                feature(
                    icon = WhatsNewIcon.SystemIcon("star.fill"),
                    title = "微信小程序 / H5 支持",
                    subtitle = "同一份 commonMain 代码，编译成 JS 即可在小程序里运行"
                )
                feature(
                    icon = WhatsNewIcon.SystemIcon("bolt.fill"),
                    title = "零平台代码",
                    subtitle = "只有版本号和存储两处 expect/actual"
                )
                feature(
                    icon = WhatsNewIcon.SystemIcon("lock.fill"),
                    title = "原生存储",
                    subtitle = "小程序走 wx.setStorageSync，H5 走 localStorage"
                )
                primaryAction {
                    title = "继续"
                    backgroundColor = 0xFF6200EE
                    foregroundColor = 0xFFFFFFFF
                }
                secondaryAction {
                    title = "查看更新日志"
                    openURL("https://github.com/Richard102257/kuikly-whatsnew-kit")
                }
            }
        }
    )

    init {
        pageName = "WhatsNewDemoPage"
    }

    override fun body(): ViewBuilder = {
        // 小程序开发版没有版本号，这里显式指定；正式项目删掉这行即可
        whatsNewCurrentVersionOverride = "1.2.0"

        val pending = this@WhatsNewDemoPage.environment.whatsNew()
        if (pending != null) {
            addChild(WhatsNewView()) {
                setup(pending) {
                    this@WhatsNewDemoPage.environment.markAsPresented(pending.version)
                }
            }
        } else {
            // 注意：子视图必须在 addChild(...) { } 的闭包里创建。
            // 先 `DivView()` 出来再 `addChild(它)` 的话 pagerId 为空，attr 会静默失效。
            addChild(DivView()) {
                attr {
                    flex(1f)
                    flexDirection(FlexDirection.COLUMN)
                    justifyContent(FlexJustifyContent.CENTER)
                    alignItems(FlexAlign.CENTER)
                    backgroundColor(Color.WHITE)
                }

                addChild(TextView()) {
                    attr {
                        text("已经是最新版本 🎉")
                        fontSize(18f)
                        color(0xFF333333)
                    }
                }

                addChild(DivView()) {
                    attr { height(8f) }
                }

                addChild(TextView()) {
                    attr {
                        text("当前版本 ${WhatsNewVersion.current()}")
                        fontSize(13f)
                        color(0xFF999999)
                    }
                }
            }
        }
    }
}
