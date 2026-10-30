package io.github.Richard102257.whatsnew.ohos

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.SafeArea
import com.tencent.kuikly.core.views.TextView
import io.github.Richard102257.whatsnew.WhatsNewEnvironment
import io.github.Richard102257.whatsnew.WhatsNewIcon
import io.github.Richard102257.whatsnew.WhatsNewOverlay
import io.github.Richard102257.whatsnew.WhatsNewVersion
import io.github.Richard102257.whatsnew.createWhatsNewVersionStore
import io.github.Richard102257.whatsnew.whatsNewCollection
import io.github.Richard102257.whatsnew.whatsNewCurrentVersionOverride

/**
 * 鸿蒙 Demo 页面。
 *
 * 和 Android / iOS / H5 / 小程序是同一份 `:whatsnew` 组件代码，只有两处平台差异：
 *
 *  - **版本号**：`getCurrentVersion()` 的鸿蒙 actual 目前是占位实现（`bundleManager`
 *    是 ArkTS API，Kotlin/Native 侧没有绑定）。这里改成读 render 层透传的
 *    `pageData.appVersion` —— OHOS 渲染层用 `bundleManager.getBundleInfoForSelf()`
 *    取到的真实 `versionName`，再由 [whatsNewCurrentVersionOverride] 交给库。
 *  - **存储**：用 `createWhatsNewVersionStore()`（Kuikly 内置 `SharedPreferencesModule`，
 *    鸿蒙渲染层已实现），跨冷启动保留「已展示版本」。
 *
 * 呈现方式与另外四端一致：宿主界面铺满屏幕，[WhatsNewOverlay] 作为兄弟节点后加入
 * （`position: absolute; inset: 0`），弹层盖在宿主界面之上。弹层放在 `SafeArea` 外面，
 * 遮罩才能盖住状态栏。
 */
@Page("WhatsNewDemoPage")
class WhatsNewOhosDemoPage : Pager() {

    private val environment: WhatsNewEnvironment by lazy {
        // 渲染层透传的真实 versionName（app.json5 的 versionName）
        val appVersion = pageData.appVersion.ifEmpty { "1.0.0" }
        whatsNewCurrentVersionOverride = appVersion

        WhatsNewEnvironment(
            versionStore = createWhatsNewVersionStore(),
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
                        subtitle = "鸿蒙走 Kuikly SharedPreferences，冷启动后仍然记得"
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
    }

    init {
        pageName = "WhatsNewDemoPage"
    }

    override fun body(): ViewBuilder = {
        val pending = this@WhatsNewOhosDemoPage.environment.whatsNew()

        // ── 宿主 App 的界面（弹层盖在它上面）──
        SafeArea {
            attr {
                flex(1f)
                backgroundColor(0xFFF2F2F7)
            }

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
        }

        // ── 有新版本：弹层盖在宿主界面之上 ──
        if (pending != null) {
            addChild(WhatsNewOverlay()) {
                present(pending) {
                    this@WhatsNewOhosDemoPage.environment.markAsPresented(pending.version)
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
