package io.github.Richard102257.whatsnew.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.TextView
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.WhatsNewIcon
import io.github.Richard102257.whatsnew.WhatsNewOverlay
import io.github.Richard102257.whatsnew.WhatsNewVersion
import io.github.Richard102257.whatsnew.createPersistentWhatsNewVersionStore
import io.github.Richard102257.whatsnew.whatsNewCollection
import io.github.Richard102257.whatsnew.whatsNewCurrentVersionOverride

/**
 * Android / H5 / 微信小程序 共用 Demo 页面
 *
 * 这里演示的是 WhatsNewKit 的默认用法：宿主 App 有自己的界面，新版本首次启动时
 * 把 [WhatsNewOverlay] 盖在上面（遮罩 + 圆角卡片），点「继续」写入存储并关闭弹层。
 * 库不接管宿主路由，所以任何 Kuikly 页面都能直接复用这段写法。
 *
 * 版本号：小程序开发版拿不到 `wx.getAccountInfoSync().miniProgram.version`，
 * Demo 里显式覆盖；正式项目里由 `getCurrentVersion()` 自动读取。
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

        // ── 宿主 App 的界面（弹层盖在它上面）──
        addChild(DivView()) {
            attr {
                flex(1f)
                flexDirection(FlexDirection.COLUMN)
                backgroundColor(0xFFF2F2F7)
                padding(top = 28f, left = 20f, bottom = 20f, right = 20f)
            }

            addChild(TextView()) {
                attr {
                    text("Kuikly WhatsNew")
                    fontSize(28f)
                    fontWeightBold()
                    color(0xFF1A1A1A)
                }
            }

            addChild(TextView()) {
                attr {
                    text("SvenTiigi/WhatsNewKit 的 Kuikly 跨端移植")
                    fontSize(13f)
                    color(0xFF8A8A8E)
                    lines(2)
                    textOverFlowWordWrapping()
                }
            }

            addChild(DivView()) { attr { height(22f) } }

            if (pending == null) {
                addCaughtUpBanner()
                addChild(DivView()) { attr { height(16f) } }
            }

            addHostRow("⚙️", "通用", "主题、语言、缓存")
            addChild(DivView()) { attr { height(10f) } }
            addHostRow("🔔", "通知", "更新提醒、角标")
            addChild(DivView()) { attr { height(10f) } }
            addHostRow("ℹ️", "关于", "版本 ${WhatsNewVersion.current()}")

            // 占位，把页脚压到底部
            addChild(DivView()) { attr { flex(1f) } }

            addChild(TextView()) {
                attr {
                    text("版本 ${WhatsNewVersion.current()} · Kuikly 2.4.0")
                    fontSize(12f)
                    color(0xFFAEAEB2)
                    textAlignCenter()
                }
            }
        }

        // ── 有新版本：弹层盖在宿主界面之上 ──
        if (pending != null) {
            addChild(WhatsNewOverlay()) {
                present(pending) {
                    this@WhatsNewDemoPage.environment.markAsPresented(pending.version)
                }
            }
        }
    }

}

private fun ViewContainer<*, *>.addCaughtUpBanner() {
        addChild(DivView()) {
            attr {
                flexDirection(FlexDirection.ROW)
                alignItems(FlexAlign.CENTER)
                backgroundColor(0xFFE8F7EE)
                borderRadius(14f)
                padding(top = 14f, left = 16f, bottom = 14f, right = 16f)
            }

            addChild(TextView()) {
                attr {
                    text("✅")
                    fontSize(20f)
                }
            }

            addChild(DivView()) { attr { width(10f) } }

            addChild(DivView()) {
                attr {
                    flex(1f)
                    flexDirection(FlexDirection.COLUMN)
                }

                addChild(TextView()) {
                    attr {
                        text("已经是最新版本 🎉")
                        fontSize(15f)
                        fontWeight600()
                        color(0xFF1B7F3B)
                    }
                }

                addChild(DivView()) { attr { height(4f) } }

                addChild(TextView()) {
                    attr {
                        text("当前版本 ${WhatsNewVersion.current()}，下次有更新会自动弹出")
                        fontSize(12f)
                        color(0xFF4C9A64)
                        lines(2)
                        textOverFlowWordWrapping()
                    }
                }
            }
        }
    }

private fun ViewContainer<*, *>.addHostRow(icon: String, title: String, detail: String) {
        addChild(DivView()) {
            attr {
                flexDirection(FlexDirection.ROW)
                alignItems(FlexAlign.CENTER)
                backgroundColor(Color.WHITE)
                borderRadius(14f)
                padding(top = 14f, left = 14f, bottom = 14f, right = 14f)
            }

            addChild(DivView()) {
                attr {
                    width(40f)
                    height(40f)
                    borderRadius(10f)
                    backgroundColor(0xFFF2F2F7)
                    flexDirection(FlexDirection.COLUMN)
                    justifyContent(FlexJustifyContent.CENTER)
                    alignItems(FlexAlign.CENTER)
                }

                addChild(TextView()) {
                    attr {
                        text(icon)
                        fontSize(20f)
                    }
                }
            }

            addChild(DivView()) { attr { width(12f) } }

            addChild(DivView()) {
                attr {
                    flex(1f)
                    flexDirection(FlexDirection.COLUMN)
                }

                addChild(TextView()) {
                    attr {
                        text(title)
                        fontSize(16f)
                        fontWeight600()
                        color(0xFF1A1A1A)
                    }
                }

                addChild(TextView()) {
                    attr {
                        text(detail)
                        fontSize(12f)
                        color(0xFF8A8A8E)
                        lines(2)
                        textOverFlowWordWrapping()
                    }
                }
            }

            addChild(TextView()) {
                attr {
                    text("›")
                    fontSize(20f)
                    color(0xFFC7C7CC)
                }
            }
        }
    }
