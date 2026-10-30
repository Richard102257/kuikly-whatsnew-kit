#import <UIKit/UIKit.h>

/// 最小化的 Kuikly 宿主页面：把 KuiklyRenderViewControllerBaseDelegator 的
/// renderView 铺满整个屏幕，页面内容由 Kotlin/Native framework `shared` 渲染。
@interface KuiklyWhatsNewViewController : UIViewController

- (instancetype)initWithPageName:(NSString *)pageName;

@end
