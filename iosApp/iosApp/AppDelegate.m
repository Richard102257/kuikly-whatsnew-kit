#import "AppDelegate.h"
#import "KuiklyWhatsNewViewController.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];

    KuiklyWhatsNewViewController *vc =
        [[KuiklyWhatsNewViewController alloc] initWithPageName:@"WhatsNewDemoPage"];
    self.window.rootViewController = vc;
    [self.window makeKeyAndVisible];
    return YES;
}

@end
