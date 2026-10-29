import { UIAbility, AbilityConstant, Want } from '@kit.AbilityKit';
import { window } from '@kit.ArkUI';

/**
 * 鸿蒙 Demo 应用入口
 *
 * 演示如何注册 WhatsNew 组件到 Kuikly Render：
 *
 * 1. 在 Kuikly 初始化时注册自定义组件
 * 2. WhatsNewOhsViewDelegate 提供组件创建器
 */
export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    console.info('[WhatsNewKit] EntryAbility onCreate');
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    windowStage.loadContent('pages/Index', (err, data) => {
      if (err.code) {
        console.error('[WhatsNewKit] Failed to load content: ' + JSON.stringify(err));
        return;
      }
      console.info('[WhatsNewKit] Content loaded successfully');
    });
  }

  onForeground(): void {
    console.info('[WhatsNewKit] EntryAbility onForeground');
  }

  onBackground(): void {
    console.info('[WhatsNewKit] EntryAbility onBackground');
  }

  onDestroy(): void {
    console.info('[WhatsNewKit] EntryAbility onDestroy');
  }
}
