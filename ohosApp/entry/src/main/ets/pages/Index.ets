import { WhatsNewOhsViewDelegate } from '@kuikly/whatsnew';

/**
 * 鸿蒙 Demo 首页
 */
@Entry
@Component
struct Index {
  build() {
    Column() {
      Text('Kuikly WhatsNewKit')
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .fontColor('#667EEA')
        .margin({ top: 60 })

      Text('HarmonyOS Demo')
        .fontSize(16)
        .fontColor('#888888')
        .margin({ top: 12 })

      Text('Cross-platform changelog component')
        .fontSize(14)
        .fontColor('#AAAAAA')
        .margin({ top: 8 })
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .backgroundColor('#F5F5F5')
  }
}
