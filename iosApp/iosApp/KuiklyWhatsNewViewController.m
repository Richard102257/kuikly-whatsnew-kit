#import "KuiklyWhatsNewViewController.h"

#import "KuiklyRenderViewControllerBaseDelegator.h"

/// framework 名 = Kotlin/Native 产物的 baseName（见 shared/build.gradle.kts 里的
/// cocoapods.framework.baseName）。渲染器会用 "shared" 拼出 SharedKuiklyCoreEntry。
static NSString *const kKuiklyFrameworkName = @"shared";

@implementation KuiklyWhatsNewViewController {
    KuiklyRenderViewControllerBaseDelegator *_delegator;
}

- (instancetype)initWithPageName:(NSString *)pageName {
    if (self = [super init]) {
        _delegator = [[KuiklyRenderViewControllerBaseDelegator alloc]
            initWithPageName:pageName
                    pageData:@{}
               frameworkName:kKuiklyFrameworkName];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [_delegator viewDidLoadWithView:self.view];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [_delegator viewWillAppear];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [_delegator viewDidLayoutSubviews];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [_delegator viewDidAppear];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_delegator viewWillDisappear];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [_delegator viewDidDisappear];
}

#pragma mark - KuiklyRenderViewControllerBaseDelegatorDelegate

- (void)onUnhandledException:(NSString *)exReason
                       stack:(NSString *)callstackStr
                        mode:(KuiklyContextMode)mode {
    NSLog(@"[Kuikly] unhandled exception: %@\n%@", exReason, callstackStr);
}

- (void)onPageLoadComplete:(BOOL)isSucceed
                     error:(NSError *)error
                      mode:(KuiklyContextMode)mode {
    NSLog(@"[Kuikly] page load complete: %d, error: %@", isSucceed, error);
}

@end
