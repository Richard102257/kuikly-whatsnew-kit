import {
  KRNativeRenderController,
  KRRecord,
  KRRenderNativeMode,
  Kuikly
} from '@kuikly-open/render';
import globalNativeManager from '../kuikly/MyNativeManager';
import { KuiklyViewDelegate } from '../kuikly/KuiklyViewDelegate';

/**
 * 宿主页面：整屏交给 Kuikly 的 WhatsNewDemoPage。
 * 页面里的 WhatsNew 弹层由 Kotlin 侧的 WhatsNewOverlay 绘制。
 */
@Entry
@Component
struct Index {
  private kuiklyViewDelegate = new KuiklyViewDelegate();
  private kuiklyController: KRNativeRenderController | null = null;

  build() {
    Stack() {
      Kuikly({
        pageName: 'WhatsNewDemoPage',
        pageData: {} as KRRecord,
        executeMode: KRRenderNativeMode.Native,
        contextCode: '',
        assetsDir: '',
        imeMode: false,
        onControllerReadyCallback: (controller: KRNativeRenderController) => {
          this.kuiklyController = controller;
        },
        nativeManager: globalNativeManager,
        delegate: this.kuiklyViewDelegate,
      })
    }
    .width('100%')
    .height('100%')
  }
}
