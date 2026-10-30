import { KuiklyNativeManager } from '@kuikly-open/render';
import Napi from 'libkuikly_entry.so';

/**
 * Kuikly 的 native 入口：把 NAPI 层的 initKuikly() 交给 render 层调用。
 * initKuikly() 是 Kuikly KSP 生成的 Kotlin 函数（见 :whatsnewOhos），
 * 内部注册 @Page 路由表并把控制权交回 ArkTS。
 */
class MyNativeManager extends KuiklyNativeManager {
  loadNative(): number {
    return Napi.initKuikly();
  }
}

const globalNativeManager = new MyNativeManager();

export default globalNativeManager;
