import XCTest

/// 只做一件事：像真人一样点一下弹层卡片上的「继续」主按钮。
/// 点击会走 `WhatsNewView` 的 primaryAction 回调 → `markAsPresented` → `NSUserDefaults`，
/// 之后重启 App 就能看到「已经是最新版本」。
///
/// 用归一化坐标而不是无障碍标签：Kuikly 的视图树默认不导出 accessibilityIdentifier，
/// 而截图需要的是「真的点到了那个位置」。
final class WhatsNewUITests: XCTestCase {

    func testTapPrimaryAction() throws {
        let app = XCUIApplication()
        app.launch()

        // 等首屏渲染完成（Kotlin/Native framework 初始化 + 页面 build/layout）
        sleep(5)

        let button = app.coordinate(withNormalizedOffset: CGVector(dx: 0.53, dy: 0.745))
        button.tap()

        // 等点击回调写完 NSUserDefaults
        sleep(2)
    }
}
