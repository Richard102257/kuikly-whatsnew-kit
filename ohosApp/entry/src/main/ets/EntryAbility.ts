import { UIAbility, AbilityConstant, Want } from '@kit.AbilityKit';

/**
 * 鸿蒙 Demo 应用入口
 * 演示如何注册 WhatsNew 组件到 Kuikly Render
 */
export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    // 初始化 Kuikly 引擎
    // 注册 WhatsNew 自定义组件
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    // 加载 Kuikly 页面
  }
}
