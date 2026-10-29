# Publishing Guide

Kuikly WhatsNewKit 采用 **三平台独立发布** 策略，每个平台侧模块独立打包分发。

## 发布架构

```
┌─────────────────────────────────────────────────────────┐
│                    发布产物汇总                          │
├──────────────┬──────────────┬──────────────┬────────────┤
│ 模块         │ 产物格式     │ 发布渠道     │ 用户引入   │
├──────────────┼──────────────┼──────────────┼────────────┤
│ whatsnew     │ AAR          │ JitPack /    │ Gradle     │
│ (KMP 跨端)   │ iOS Framework│ Maven Central│ pod        │
│              │ Ohos SO      │              │ ohpm       │
├──────────────┼──────────────┼──────────────┼────────────┤
│whatsnewAndroid│ AAR         │ JitPack /    │ Gradle     │
│(Android 平台) │              │ Maven Central│            │
├──────────────┼──────────────┼──────────────┼────────────┤
│whatsnewIOS   │ CocoaPods    │ CocoaPods    │ Podfile    │
│(iOS 平台)    │ podspec      │ Trunk        │            │
├──────────────┼──────────────┼──────────────┼────────────┤
│whatsnewOhos  │ AAR (ohos)   │ JitPack /    │ Gradle     │
│(鸿蒙平台)    │              │ Maven Central│            │
└──────────────┴──────────────┴──────────────┴────────────┘
```

---

## 一、Android 平台发布

### 产物
- `whatsnew` → AAR（KMP 跨端模块编译产物）
- `whatsnewAndroid` → AAR（Android 平台侧原生实现）

### 方式 A：JitPack（最简单）

```bash
# 1. 推送代码
git add .
git commit -m "Release v1.0.0"
git tag v1.0.0
git push origin main --tags

# 2. 触发 JitPack 构建
# 访问 https://jitpack.io/#your-org/kuikly-whatsnew-kit/v1.0.0
```

用户引入：
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.your-org.kuikly-whatsnew-kit:whatsnew:1.0.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.github.your-org.kuikly-whatsnew-kit:whatsnewAndroid:1.0.0")
            }
        }
    }
}
```

### 方式 B：Maven Central

```bash
# 发布到 Maven Central
./gradlew :whatsnewAndroid:publish

# 然后在 Sonatype 管理台 Close → Release
```

---

## 二、iOS 平台发布

iOS 有 **两个** CocoaPods 产物，用途不同：

### 产物 A：whatsnew（KMP 跨端 Framework）

由 `whatsnew/build.gradle.kts` 中的 `cocoapods {}` 配置生成。

```bash
# 发布到 CocoaPods Trunk
./gradlew :whatsnew:podPublish
```

### 产物 B：whatsnewIOS（iOS 平台侧原生实现）

由 `whatsnewIOS.podspec` 定义，源码在 `whatsnewIOS/Classes/`。

```bash
# 验证 podspec
pod spec lint whatsnewIOS.podspec --allow-warnings

# 发布到 CocoaPods Trunk
pod trunk push whatsnewIOS.podspec --allow-warnings

# 或发布到私有仓库
pod repo push your-private-repo whatsnewIOS.podspec --allow-warnings
```

用户引入：
```ruby
# Podfile
pod 'WhatsNew', '~> 1.0.0'          # KMP 跨端 Framework
pod 'whatsnewIOS', '~> 1.0.0'       # iOS 平台侧原生实现
```

### podspec 关键配置说明

```ruby
spec.dependency 'OpenKuiklyIOSRender', '~> 2.0'  # 依赖 Kuikly iOS Render
spec.source_files = 'whatsnewIOS/Classes/**/*'     # 源码路径
spec.static_framework = true                        # 静态框架
```

---

## 三、HarmonyOS 平台发布

### 产物
- `whatsnew` → SO（KMP 跨端模块编译产物，通过 `build.ohos.gradle.kts` 构建）
- `whatsnewOhos` → AAR（鸿蒙平台侧原生实现）

### 方式 A：JitPack

```bash
git tag v1.0.0
git push origin v1.0.0
# 访问 https://jitpack.io/#your-org/kuikly-whatsnew-kit/v1.0.0
```

### 方式 B：Maven Central

```bash
./gradlew :whatsnewOhos:publish
```

### 方式 C：ohpm（鸿蒙包管理器）

```bash
# 生成 ohpm 包
ohpm build whatsnewOhos/

# 发布到 ohpm 仓库
ohpm publish whatsnewOhos/
```

---

## 四、版本管理

所有模块版本号统一，鸿蒙版本加 `_ohos` 后缀避免 Maven 元数据冲突：

```
whatsnew:          1.0.0
whatsnewAndroid:   1.0.0
whatsnewIOS:       1.0.0
whatsnewOhos:      1.0.0_ohos
```

发布新版本：

```bash
# 1. 更新所有模块版本号
# 2. 提交并打标签
git add .
git commit -m "Release v1.1.0"
git tag v1.1.0
git push origin main --tags

# 3. iOS 额外步骤
pod trunk push whatsnewIOS.podspec --allow-warnings
```

---

## 五、CI/CD 自动发布

已配置 GitHub Actions (`.github/workflows/publish.yml`)，推送 tag 后自动触发。

需要的 Secrets：
- `MAVEN_USERNAME` — Sonatype 用户名
- `MAVEN_PASSWORD` — Sonatype 密码
- `GPG_KEY` — Base64 编码的 GPG 私钥
- `GPG_PASSWORD` — GPG 密钥密码
- `COCOAPODS_TRUNK_TOKEN` — CocoaPods Trunk Token
