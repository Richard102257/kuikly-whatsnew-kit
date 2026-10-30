import {
  IKuiklyViewDelegate,
  KRRenderModuleExportCreator,
  KRRenderViewExportCreator
} from '@kuikly-open/render';

/**
 * 本 Demo 只用 Kuikly 的标准 View / Module，没有自定义渲染组件，
 * 所以两个注册表都返回空 Map。
 */
export class KuiklyViewDelegate extends IKuiklyViewDelegate {
  getCustomRenderViewCreatorRegisterMap(): Map<string, KRRenderViewExportCreator> {
    return new Map<string, KRRenderViewExportCreator>();
  }

  getCustomRenderModuleCreatorRegisterMap(): Map<string, KRRenderModuleExportCreator> {
    return new Map<string, KRRenderModuleExportCreator>();
  }
}
