# EasyScan

一个基于Kotlin Multiplatform和Compose Multiplatform构建的跨平台二维码扫描应用，支持Android和Desktop平台。

## 功能特性

- 🔍 二维码扫描功能
- 📱 支持Android和Desktop平台
- 🌐 支持自定义服务器端点配置
- 🎨 Material Design 3界面设计

## 项目结构

* `/composeApp` 包含跨平台共享代码
  - `commonMain` - 所有平台的通用代码
  - `androidMain` - Android平台专用代码
  - `desktopMain` - Desktop平台专用代码
  - `commonTest` - 通用测试代码

## 开发环境要求

- JDK 11 或更高版本
- Android Studio 或 IntelliJ IDEA
- Android SDK (用于Android平台)

## 🔧 配置服务器端点

关于服务端配置，请移步 [EasyScanServer](https://github.com/LiYulin-s/easyscan-server) 仓库。

本应用使用BuildKonfig来管理配置，你可以通过以下方式配置服务器端点：

### 使用本地配置文件

1. 在项目根目录 `local.properties` 修改配置：
```properties
easyscan.url=http://your-server-address:port
```

## 🚀 运行应用

### Android
```bash
./gradlew :composeApp:installDebug
```


## 📦 构建发布版本

### Android APK
```bash
./gradlew :composeApp:assembleRelease
```

## 🙏 致谢

本项目使用了以下优秀的开源项目和技术：

### 核心技术栈
- **[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)** - 跨平台开发框架
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** - 声明式UI框架
- **[Material Design 3](https://m3.material.io/)** - Google Material Design设计系统

### 主要依赖库
- **[EasyQRScan](https://github.com/kalinjul/EasyQRScan)** - 二维码扫描核心库
- **[Ktor](https://ktor.io/)** - HTTP客户端和网络通信
- **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)** - JSON序列化支持
- **[AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore)** - 数据存储解决方案
- **[BuildKonfig](https://github.com/yshrsmz/BuildKonfig)** - Kotlin Multiplatform配置管理

### 构建工具和插件
- **[Gradle](https://gradle.org/)** - 构建自动化工具
- **[Android Gradle Plugin](https://developer.android.com/studio/build)** - Android构建支持
- **[Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler)** - Compose编译器
- **[Compose Hot Reload](https://github.com/JetBrains/compose-hot-reload)** - 热重载支持

### 特别感谢
- **JetBrains** 提供的强大开发工具和Kotlin生态系统
- **Google** 的Android开发平台和Material Design
- **开源社区** 中所有贡献者的无私奉献

## 🤝 贡献

欢迎提交Issue和Pull Request来帮助改进这个项目。

## 📄 许可证

本项目采用 AGPLv3 许可证 - 详见 [LICENSE](LICENSE) 文件。

---

了解更多关于 [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) 的信息。
