# Kuikly WhatsNewKit 集成教程

> 从零开始，5 分钟集成 Kuikly WhatsNewKit 到你的跨端项目

---

## 📋 目录

- [方式一：Maven 依赖（推荐）](#方式一maven-依赖推荐)
- [方式二：JitPack](#方式二jitpack)
- [方式三：源码集成](#方式三源码集成)
- [方式四：Git Submodule](#方式四git-submodule)
- [Compose DSL 完整教程](#compose-dsl-完整教程)
- [Kuikly DSL 完整教程](#kuikly-dsl-完整教程)
- [常见问题](#常见问题)

---

## 方式一：Maven 依赖（推荐）

### 步骤 1：添加仓库

在项目根目录 `settings.gradle.kts` 中确保有 Maven Central：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // 如果发布到 JitPack，添加：
        // maven { url = uri("https://jitpack.io") }
    }
}
```

### 步骤 2：添加依赖

在共享模块的 `build.gradle.kts` 中：

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // 唯一模块：纯 Kuikly DSL 组件，全平台通用
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.0")
            }
        }
    }
}
```

### 步骤 3：同步项目

```bash
./gradlew sync
```

---

## 方式二：JitPack

适合快速体验，无需注册 Maven Central 账号。

### 步骤 1：添加 JitPack 仓库

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 步骤 2：添加依赖

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Richard102257.kuikly-whatsnew-kit:whatsnew:1.1.0")
            }
        }
    }
}
```

> 💡 `1.1.0` 替换为实际发布的 tag 版本号。

---

## 方式三：源码集成

适合需要深度定制或调试源码的场景。

### 步骤 1：复制源码

将以下目录复制到你的项目中：

```
你的项目/
├── kuikly-whatsnew/
│   ├── core/src/commonMain/     → 核心模块源码
│   ├── compose/src/commonMain/  → Compose DSL 源码
│   └── dsl/src/commonMain/      → Kuikly DSL 源码
```

### 步骤 2：配置 Gradle

在 `settings.gradle.kts` 中：

```kotlin
include(":kuikly-whatsnew-core")
include(":kuikly-whatsnew-compose")
include(":kuikly-whatsnew-dsl")
```

### 步骤 3：添加依赖

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kuikly-whatsnew-core"))
                implementation(project(":kuikly-whatsnew-compose"))
                implementation(project(":kuikly-whatsnew-dsl"))
            }
        }
    }
}
```

---

## 方式四：Git Submodule

适合需要跟踪上游更新的场景。

```bash
# 添加子模块
git submodule add https://github.com/your-org/kuikly-whatsnew-kit.git libs/kuikly-whatsnew-kit

# 初始化
git submodule update --init --recursive
```

然后在 `settings.gradle.kts` 中 include 对应模块。

---

## Compose DSL 完整教程

### 场景 1：手动展示（最简单）

```kotlin
import io.github.Richard102257.whatsnew.compose.*
import io.github.Richard102257.whatsnew.core.*

@Composable
fun MyScreen() {
    // 1. 创建 WhatsNew 数据
    val whatsNew = remember {
        whatsNew {
            version("2.0.0")
            title("What's New")

            feature(
                imageSource = WhatsNew.ImageSource.SystemIcon("star.fill"),
                title = "新功能",
                subtitle = "这是一个很棒的新功能"
            )

            primaryAction {
                title = "继续"
                backgroundColor = 0xFF6200EE
                foregroundColor = 0xFFFFFFFF
            }

            secondaryAction {
                title = "了解更多"
                foregroundColor = 0xFF6200EE
                openURL("https://your-app.com/changelog")
            }
        }
    }

    // 2. 控制展示状态
    var showWhatsNew by remember { mutableStateOf(false) }

    // 3. 页面内容
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { showWhatsNew = true }) {
            Text("查看更新")
        }
    }

    // 4. 展示 WhatsNew
    if (showWhatsNew) {
        WhatsNewDialog(
            whatsNew = whatsNew,
            onDismiss = { showWhatsNew = false }
        )
    }
}
```

### 场景 2：自动展示（推荐）

```kotlin
import io.github.Richard102257.whatsnew.compose.*
import io.github.Richard102257.whatsnew.core.*

// ─── 在 Application 级别初始化 ───
class MyApp : ComposeContainer() {

    override fun willInit() {
        super.willInit()

        // 1. 创建版本存储（持久化）
        val versionStore = createPersistentWhatsNewVersionStore()

        // 2. 定义所有版本更新内容
        val whatsNewCollection = whatsNewCollection {
            whatsNew("1.0.0") {
                title("欢迎使用")
                feature(
                    imageSource = WhatsNew.ImageSource.SystemIcon("star.fill"),
                    title = "跨平台开发",
                    subtitle = "一次编写，多端运行"
                )
                primaryAction {
                    title = "开始使用"
                    backgroundColor = 0xFF6200EE
                    foregroundColor = 0xFFFFFFFF
                }
            }

            whatsNew("2.0.0") {
                title("v2.0 更新")
                feature(
                    imageSource = WhatsNew.ImageSource.SystemIcon("wand.and.stars"),
                    title = "AI 驱动开发"
                )
                feature(
                    imageSource = WhatsNew.ImageSource.SystemIcon("globe"),
                    title = "支持更多平台"
                )
                primaryAction {
                    title = "继续"
                    backgroundColor = 0xFF6200EE
                    foregroundColor = 0xFFFFFFFF
                }
                secondaryAction {
                    title = "了解更多"
                    foregroundColor = 0xFF6200EE
                    openURL("https://your-app.com/changelog")
                }
            }
        }

        // 3. 创建环境
        val environment = WhatsNewEnvironment(
            versionStore = versionStore,
            whatsNewCollection = whatsNewCollection
        )

        // 4. 设置内容
        setContent {
            WhatsNewComposeProvider(environment = environment) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("你的应用内容")
        // WhatsNew 会自动在版本更新时弹出
    }
}
```

### 场景 3：全屏展示

```kotlin
@Composable
fun FullScreenExample() {
    val whatsNew = remember { createSampleWhatsNew() }

    WhatsNewFullScreen(
        whatsNew = whatsNew,
        onDismiss = { /* 处理关闭 */ }
    )
}
```

### 场景 4：自定义布局

```kotlin
val whatsNew = whatsNew {
    version("2.0.0")
    title("What's New")

    feature(
        imageSource = WhatsNew.ImageSource.SystemIcon("flame.fill"),
        title = "新功能"
    )

    primaryAction {
        title = "知道了"
        backgroundColor = 0xFF4CAF50
        foregroundColor = 0xFFFFFFFF
        cornerRadius = 26f  // 圆角
    }

    layout {
        copy(
            contentPadding = WhatsNew.Padding(32f, 64f, 32f, 32f),
            titleBottomSpacing = 48f,
            featureListSpacing = 28f,
            primaryButtonHeight = 56f
        )
    }
}
```

---

## Kuikly DSL 完整教程

### 场景 1：手动展示

```kotlin
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.pager.Pager
import io.github.Richard102257.whatsnew.dsl.*
import io.github.Richard102257.whatsnew.core.*

@Page("myPage")
class MyPage : Pager() {

    private var whatsNewView: WhatsNewDslView? = null

    override fun createRenderView() {
        super.createRenderView()

        // 创建按钮
        val button = DivView().apply {
            attr {
                width(200f)
                height(48f)
                borderRadius(12f)
                backgroundColor(0xFF6200EE)
                justifyContentCenter()
                alignItemsCenter()
            }
            event {
                click { showWhatsNew() }
            }
            addSubView(TextView().apply {
                attr {
                    text("查看更新")
                    color(0xFFFFFFFF)
                    fontSize(16f)
                    fontWeight600()
                }
            })
        }

        getRenderView().addSubView(button)
    }

    private fun showWhatsNew() {
        val whatsNew = whatsNew {
            version("2.0.0")
            title("What's New")

            feature(
                imageSource = WhatsNew.ImageSource.SystemIcon("star.fill"),
                title = "新功能",
                subtitle = "这是一个很棒的新功能"
            )

            primaryAction {
                title = "继续"
                backgroundColor = 0xFF6200EE
                foregroundColor = 0xFFFFFFFF
            }
        }

        whatsNewView = WhatsNewDslView(
            whatsNew = whatsNew,
            onDismiss = {
                // 移除视图
                whatsNewView?.let { getRenderView().removeSubView(it) }
                whatsNewView = null
            },
            onSecondaryAction = { actionType ->
                when (actionType) {
                    is WhatsNew.SecondaryActionType.OpenURL -> {
                        // 打开 URL
                    }
                    is WhatsNew.SecondaryActionType.Custom -> {
                        actionType.handler()
                    }
                }
            }
        )

        getRenderView().addSubView(whatsNewView!!)
    }
}
```

### 场景 2：自动展示

```kotlin
@Page("autoWhatsNewPage")
class AutoWhatsNewPage : Pager() {

    private val environment by lazy {
        WhatsNewEnvironment(
            versionStore = createPersistentWhatsNewVersionStore(),
            whatsNewCollection = whatsNewCollection {
                whatsNew("1.0.0") {
                    title("欢迎使用")
                    feature(
                        imageSource = WhatsNew.ImageSource.SystemIcon("star.fill"),
                        title = "跨平台开发"
                    )
                    primaryAction {
                        title = "开始使用"
                        backgroundColor = 0xFF6200EE
                        foregroundColor = 0xFFFFFFFF
                    }
                }
                whatsNew("2.0.0") {
                    title("v2.0 更新")
                    feature(
                        imageSource = WhatsNew.ImageSource.SystemIcon("wand.and.stars"),
                        title = "AI 驱动"
                    )
                    primaryAction {
                        title = "继续"
                        backgroundColor = 0xFF6200EE
                        foregroundColor = 0xFFFFFFFF
                    }
                }
            }
        )
    }

    override fun createRenderView() {
        super.createRenderView()

        // 你的页面内容
        val content = createPageContent()
        getRenderView().addSubView(content)

        // 自动检查并展示 WhatsNew
        val whatsNew = environment.whatsNew()
        if (whatsNew != null) {
            val view = WhatsNewDslView(
                whatsNew = whatsNew,
                onDismiss = {
                    environment.markAsPresented(whatsNew.version)
                }
            )
            getRenderView().addSubView(view)
        }
    }

    private fun createPageContent(): DivView {
        val container = DivView()
        container.attr {
            flex(1f)
            flexDirectionFlexColumn()
            justifyContentCenter()
            alignItemsCenter()
        }
        // ... 你的页面内容
        return container
    }
}
```

---

## 发布指南

### 发布到 Maven Central

#### 前置条件

1. 注册 [Sonatype JIRA](https://issues.sonatype.org) 账号
2. 创建 GroupId 对应的项目工单（如 `io.github.Richard102257.whatsnew`）
3. 生成 GPG 签名密钥

#### 配置 `~/.gradle/gradle.properties`

```properties
# GPG 签名
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_KEY_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg

# Maven Central 凭据
mavenCentralUsername=YOUR_SONATYPE_USERNAME
mavenCentralPassword=YOUR_SONATYPE_PASSWORD
```

#### 发布命令

```bash
# 发布所有模块
./gradlew :core:publish
./gradlew :compose:publish
./gradlew :dsl:publish

# 或一次性发布
./gradlew publish
```

#### 在 Sonatype 上 Release

1. 登录 [Sonatype Repository Manager](https://s01.oss.sonatype.org/)
2. 进入 Staging Repositories
3. 找到你的 staging repo，点击 Close
4. 验证通过后，点击 Release

### 发布到 JitPack（更简单）

1. 在 GitHub 上创建 Release/Tag（如 `v1.0.0`）
2. 访问 `https://jitpack.io/#your-org/kuikly-whatsnew-kit/v1.0.0`
3. JitPack 自动构建，等待 Build 状态变为 `Get it`
4. 复制生成的依赖配置

---

## 常见问题

### Q1: 支持 HarmonyOS 吗？

支持。Kuikly 本身支持 HarmonyOS，WhatsNewKit 作为 Kuikly 上层库自然支持。只需确保你的 Kuikly 项目已配置 HarmonyOS 编译目标。

### Q2: 如何在 iOS 上使用？

```kotlin
// 在 iOS 端，通过 Kotlin/Native 生成的 Framework 使用
// 无需额外配置，commonMain 代码自动编译为 iOS Framework
```

### Q3: 自定义图标怎么办？

```kotlin
// 使用图片 URL
feature(
    imageSource = WhatsNew.ImageSource.ResourcePath("https://your-cdn.com/icon.png"),
    title = "新功能"
)

// 或使用本地资源
feature(
    imageSource = WhatsNew.ImageSource.ResourcePath("assets://images/star.png"),
    title = "新功能"
)
```

### Q4: 如何测试 WhatsNew 展示？

```kotlin
// 使用 InMemoryWhatsNewVersionStore 进行测试
val testStore = InMemoryWhatsNewVersionStore()
val environment = WhatsNewEnvironment(
    versionStore = testStore,
    whatsNewCollection = testCollection
)

// 每次测试前清空
testStore.clear() // 需要实现 clear 方法
```

### Q5: 从 Swift WhatsNewKit 迁移？

| Swift | Kotlin (Kuikly) |
|-------|----------------|
| `WhatsNew(title:features:)` | `whatsNew { title(); feature() }` |
| `WhatsNew.Feature(image:title:subtitle:)` | `feature(imageSource = ..., title = ..., subtitle = ...)` |
| `WhatsNew.PrimaryAction(title:backgroundColor:)` | `primaryAction { title = ...; backgroundColor = ... }` |
| `.whatsNewSheet()` | `WhatsNewDialog()` 或 `WhatsNewComposeProvider` |
| `UserDefaultsWhatsNewVersionStore` | `createPersistentWhatsNewVersionStore()` |
| `WhatsNewEnvironment` | `WhatsNewEnvironment` |
| `WhatsNewViewController` | `WhatsNewDslView` / `WhatsNewDslPage` |

### Q6: 多个版本如何管理？

```kotlin
val collection = whatsNewCollection {
    whatsNew("1.0.0") { /* v1.0 内容 */ }
    whatsNew("1.1.0") { /* v1.1 内容 */ }
    whatsNew("2.0.0") { /* v2.0 内容 */ }
}

// Environment 会自动匹配当前版本，只展示未看过的最新版本
val environment = WhatsNewEnvironment(
    versionStore = createPersistentWhatsNewVersionStore(),
    whatsNewCollection = collection
)
```

---

## 📞 反馈与支持

- GitHub Issues: https://github.com/your-org/kuikly-whatsnew-kit/issues
- Kuikly 官网: https://kuikly.tds.qq.com
- Kuikly GitHub: https://github.com/Tencent-TDS/KuiklyUI
