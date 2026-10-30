package io.github.Richard102257.whatsnew.shared

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.SafeArea
import com.tencent.kuikly.core.views.TextView
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.WhatsNewIcon
import io.github.Richard102257.whatsnew.WhatsNewVersion
import io.github.Richard102257.whatsnew.WhatsNewView
import io.github.Richard102257.whatsnew.createPersistentWhatsNewVersionStore
import io.github.Richard102257.whatsnew.whatsNewCollection

/**
 * iOS Demo 页面。
 *
 * 和 `:miniApp` 里的小程序 / H5 页面是同一个写法、同一份 `:whatsnew` 组件代码，
 * 区别只有两点：
 *  - 版本号不再 override，直接读 `CFBundleShortVersionString`（Info.plist 里是 1.2.0）
 *  - 存储走 `NSUserDefaults`（`:whatsnew` 的 iosMain actual）
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
        val pending = this@WhatsNewDemoPage.environment.whatsNew()
        // iOS 是全面屏：Kuikly 的 SafeAreaView 会按 pagerData.safeAreaInsets 留出刘海 / 底部横条。
        SafeArea {
            attr {
                flex(1f)
                backgroundColor(Color.WHITE)
            }
            if (pending != null) {
                addChild(WhatsNewView()) {
                    setup(pending) {
                        this@WhatsNewDemoPage.environment.markAsPresented(pending.version)
                    }
                }
            } else {
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
}
