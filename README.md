# ThirdHub-Downloader

第三方科技 · ThirdHub-Downloader —— 官方应用下载器（Android）

## 功能

- **账号登录**：使用 ThirdHub 账号登录（Supabase 认证）
- **产品列表**：展示 ThirdHub 全系列产品
- **一键下载**：下载 Android APK / Windows EXE / PWA
- **自动安装**：下载完成后自动拉起安装器

## 产品列表

| 产品 | 类型 | 说明 |
|------|------|------|
| ThirdHub Android | APK | 原生安卓客户端（轻壳版） |
| ThirdHub 完全体 | APK | Flutter 完全体客户端 |
| ThirdHub Windows | EXE | Windows 桌面客户端 |
| OmniHub 只看漫画 | APK | 第二代漫画稳定版 |
| ThirdHub PWA | Web | 网页版 |

## 技术栈

- Kotlin + Jetpack Compose
- OkHttp + Coroutines
- Supabase Auth
- Coil 图片加载

## 构建

```bash
./gradlew assembleDebug
```

## 仓库地址

https://github.com/Smalluniverseheng/ThirdHub-Downloader
