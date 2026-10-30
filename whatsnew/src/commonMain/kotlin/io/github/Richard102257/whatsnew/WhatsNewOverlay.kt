package io.github.Richard102257.whatsnew

import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.views.DivView

/**
 * WhatsNew 模态弹层
 *
 * 对应 WhatsNewKit 里 `present(whatsNewViewController)` 的呈现方式：
 * 一层半透明遮罩 + 居中的圆角卡片，盖在宿主 App 的界面上方。
 *
 * ```
 * addChild(WhatsNewOverlay()) {
 *     present(whatsNew) { environment.markAsPresented(whatsNew.version) }
 * }
 * ```
 *
 * 弹层本身铺满父容器（`position: absolute; inset: 0`），所以它的父容器
 * 必须是页面根节点或同样铺满屏幕的容器；宿主界面要先于它 `addChild`，
 * 后加的在上面。
 *
 * 只想做整页展示（不需要弹层）时用 [WhatsNewView] 或 [WhatsNewPage] 即可。
 */
class WhatsNewOverlay : ViewContainer<ContainerAttr, Event>() {

    override fun createAttr() = ContainerAttr()
    override fun createEvent() = Event()
    override fun viewName() = ViewConst.TYPE_VIEW

    /**
     * 渲染遮罩 + 卡片
     *
     * @param whatsNew 要展示的内容
     * @param onDismiss 点主按钮后的回调（一般在这里 `markAsPresented`）
     * @param dimColor 遮罩颜色，默认 40% 黑
     * @param cardCornerRadius 卡片圆角
     * @param horizontalInset 卡片左右留白
     * @param verticalInset 卡片上下留白
     */
    fun present(
        whatsNew: WhatsNew,
        dimColor: Long = 0x66000000,
        cardCornerRadius: Float = 28f,
        horizontalInset: Float = 20f,
        verticalInset: Float = 72f,
        onDismiss: (() -> Unit)? = null,
    ) {
        // 遮罩：铺满父容器，吃掉底下宿主界面的点击。
        // 卡片留白用 padding 而不是子视图 margin：H5 渲染器里「stretch + 子视图左右 margin」
        // 只扣掉左边距，卡片会从右侧溢出屏幕（Android 上正常，H5 上标题被裁掉）。
        attr {
            absolutePositionAllZero()
            backgroundColor(dimColor)
            flexDirection(FlexDirection.COLUMN)
            justifyContent(FlexJustifyContent.CENTER)
            alignItems(FlexAlign.STRETCH)
            padding(
                top = verticalInset,
                left = horizontalInset,
                bottom = verticalInset,
                right = horizontalInset
            )
        }

        // 卡片就是 WhatsNewView 本身：圆角交给它画，避免子视图的白色背景把圆角盖成直角
        addChild(WhatsNewView()) {
            setup(whatsNew, cornerRadius = cardCornerRadius, onDismiss = onDismiss)
        }

        // 注意：这里故意不在遮罩上挂 click 关闭。Kuikly 的 click 会从子视图冒泡到遮罩，
        // 点卡片里的按钮会同时触发「按钮」和「遮罩」两个回调（实测会把版本写两次）。
        // 需要「点空白关闭」时，建议在宿主里单独加一层只覆盖空白区域的可点区域。
    }
}
