import UIKit

/**
 * iOS Demo 应用入口
 *
 * iOS 侧 Kuikly 组件通过运行时暴露，无需手动注册。
 * 只需在 Podfile 中引入 whatsnewIOS 即可。
 */
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {

        window = UIWindow(frame: UIScreen.main.bounds)

        // 创建主页面（实际项目中使用 Kuikly 页面）
        let viewController = UIViewController()
        viewController.view.backgroundColor = .white

        let label = UILabel()
        label.text = "Kuikly WhatsNewKit - iOS Demo"
        label.font = .boldSystemFont(ofSize: 20)
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        viewController.view.addSubview(label)
        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: viewController.view.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: viewController.view.centerYAnchor)
        ])

        window?.rootViewController = UINavigationController(rootViewController: viewController)
        window?.makeKeyAndVisible()

        return true
    }
}
