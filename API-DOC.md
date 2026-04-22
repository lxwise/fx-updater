# FX-Updater API 使用说明文档

> **版本**：2.0 | **技术栈**：Java 21 + JavaFX 23.0.1 + Gson 2.10 + Maven  
> **仓库地址**：https://github.com/lxwise/fx-updater  
> **Maven 坐标**：`io.github.lxwise:fx-updater:1.0.4`

---

## 目录

- [一、项目简介](#一项目简介)
- [二、快速开始](#二快速开始)
- [三、配置说明](#三配置说明)
  - [3.1 app.properties 客户端配置](#31-appproperties-客户端配置)
  - [3.2 app-update-config.json 服务端配置](#32-app-update-configjson-服务端配置)
  - [3.3 theme.css 主题样式](#33-themecss-主题样式)
- [四、核心 API 详解](#四核心-api-详解)
  - [4.1 FXUpdater 类](#41-fxupdater-类)
  - [4.2 UpdateConfig 类（Builder 模式）](#42-updateconfig-类builder-模式)
  - [4.3 UpdateMode 枚举](#43-updatemode-枚举)
- [五、使用示例](#五使用示例)
  - [5.1 一行式极简调用（推荐）](#51-一行式极简调用推荐)
  - [5.2 经典构造方式](#52-经典构造方式)
  - [5.3 Builder 高级配置](#53-builder-高级配置)
  - [5.4 四种更新模式示例](#54-四种更新模式示例)
  - [5.5 纯代码手动配置（不读取配置文件）](#55-纯代码手动配置不读取配置文件)
- [六、功能特性详解](#六功能特性详解)
  - [6.1 四种更新模式](#61-四种更新模式)
  - [6.2 语义化版本比较](#62-语义化版本比较)
  - [6.3 断点续传与重试机制](#63-断点续传与重试机制)
  - [6.4 跨平台支持](#64-跨平台支持)
  - [6.5 自动重启功能](#65-自动重启功能)
  - [6.6 日志记录](#66-日志记录)
  - [6.7 国际化（i18n）](#67-国际化i18n)
- [七、工具类 API](#七工具类-api)
  - [7.1 VersionUtil 版本比较工具](#71-versionutil-版本比较工具)
  - [7.2 HttpUtils HTTP 工具](#72-httputils-http-工具)
  - [7.3 UpdateLogger 日志工具](#73-updatelogger-日志工具)
  - [7.4 AppRestartUtil 应用重启工具](#74-apprestartutil-应用重启工具)
  - [7.5 GuiUtils GUI 工具](#75-guiutils-gui-工具)
- [八、数据模型](#八数据模型)
- [九、向后兼容性说明](#九向后兼容性说明)
- [十、错误处理与异常说明](#十错误处理与异常说明)
- [十一、最佳实践建议](#十一最佳实践建议)
- [十二、项目结构](#十二项目结构)
- [十三、常见问题 FAQ](#十三常见问题-faq)

---

## 一、项目简介

FX-Updater 是一个 **非侵入式** 的 JavaFX 应用自动更新库，为 JavaFX 桌面应用提供完整的自动更新能力。核心特性：

| 特性 | 说明 |
|------|------|
| **极简接入** | 最少仅需 2 项配置 + 1 行代码即可启用自动更新 |
| **多种更新模式** | 交互式 / 静默 / 后台下载 / 仅检查 四种模式 |
| **语义版本比较** | 基于 SemVer 标准比较版本号，无需手动维护 releaseId |
| **断点续传** | 下载中断后自动从断点恢复 |
| **失败重试** | 下载失败自动重试，递增延迟策略 |
| **跨平台** | Windows (x64/x86)、macOS、Linux 全平台支持 |
| **多安装格式** | exe、msi、dmg、pkg、deb、rpm、tar.gz、zip |
| **国际化** | 内置 12 种语言支持 |
| **主题定制** | 通过 CSS 文件自定义更新界面样式 |
| **双格式配置** | 同时支持经典格式和简化格式的 JSON 配置 |

---

## 二、快速开始

### 第一步：添加 Maven 依赖

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>fx-updater</artifactId>
    <version>1.0.4</version>
</dependency>
```

### 第二步：创建配置文件

在项目 `src/main/resources/` 目录下创建 `app.properties`：

```properties
# 仅需2项必填配置即可启用自动更新
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/app-update-config.json
```

### 第三步：一行代码启用更新

```java
@Override
public void start(Stage primaryStage) {
    // ... 初始化你的应用 ...

    // 一行代码检查更新
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### 第四步：部署服务端配置文件

在服务器上部署 `app-update-config.json`（简化格式）：

```json
{
  "name": "你的应用名称",
  "version": "2.0.0",
  "changelog": "版本 2.0.0 更新内容：\n- 新功能A\n- 修复问题B",
  "platforms": {
    "win_x64": { "url": "http://your-server.com/YourApp-2.0.0.exe", "size": 52428800 },
    "mac":     { "url": "http://your-server.com/YourApp-2.0.0.dmg", "size": 61865984 },
    "linux":   { "url": "http://your-server.com/YourApp-2.0.0.deb", "size": 48234496 }
  }
}
```

完成！当用户运行版本 1.0.0 的应用时，会自动检测到 2.0.0 更新并弹出更新提示。

---

## 三、配置说明

### 3.1 app.properties 客户端配置

此文件放在项目的 `src/main/resources/` 根目录下，FXUpdater 会自动读取。

#### 必填配置（仅 2 项）

| 配置项 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `app.update.version` | String | 当前应用版本号（SemVer 格式） | `1.0.0` |
| `app.update.configUrl` | String | 服务端更新配置文件的 URL 地址 | `http://server/update.json` |

#### 可选配置（有智能默认值）

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `app.update.releaseId` | Integer | 从 version 自动推导 | 内部版本数值 ID |
| `app.update.licenseVersion` | Integer | `1` | 许可证版本号 |

#### 完整示例

```properties
# === 必填 ===
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/app-update-config.json

# === 可选（通常不需要填写） ===
# app.update.releaseId = 1000000
# app.update.licenseVersion = 1
```

#### releaseId 自动推导规则

当未配置 `app.update.releaseId` 时，会通过 `VersionUtil.toReleaseId(version)` 自动计算：

```
公式：major × 1,000,000 + minor × 1,000 + patch

示例：
  "1.0.0"   → 1,000,000
  "2.1.3"   → 2,001,003
  "1.5.20"  → 1,005,020
  "10.2.1"  → 10,002,001
```

---

### 3.2 app-update-config.json 服务端配置

此文件部署在你的服务器上，FXUpdater 通过 `configUrl` 下载此文件判断是否有新版本。

FXUpdater 支持 **两种 JSON 格式**，自动检测格式类型。

#### 格式一：简化格式（推荐）

适用于大多数单版本发布场景，配置最少、最易维护：

```json
{
  "name": "Your App Name",
  "version": "2.0.0",
  "changelog": "版本 2.0.0 更新内容：\n- 新增功能A\n- 修复问题B\n- 性能优化",
  "icon": "http://your-server.com/app-icon.png",
  "downloadUrl": "https://your-website.com/download",
  "platforms": {
    "win_x64": {
      "url": "http://your-server.com/YourApp-2.0.0.exe",
      "size": 52428800,
      "sha256": "a1b2c3d4e5f6..."
    },
    "mac": {
      "url": "http://your-server.com/YourApp-2.0.0.dmg",
      "size": 61865984
    },
    "linux": {
      "url": "http://your-server.com/YourApp-2.0.0.deb",
      "size": 48234496
    }
  }
}
```

**简化格式字段说明：**

| 字段 | 必填 | 类型 | 说明 |
|------|------|------|------|
| `name` | 否 | String | 应用名称，显示在更新弹窗标题栏 |
| `version` | **是** | String | 新版本号（SemVer 格式） |
| `changelog` | 否 | String | 更新日志文本（支持 `\n` 换行），或 URL 地址 |
| `icon` | 否 | String | 应用图标 URL |
| `downloadUrl` | 否 | String | 官方下载页 URL（下载失败时的手动下载备选） |
| `licenseVersion` | 否 | Integer | 许可证版本号，默认 `1` |
| `platforms` | **是** | Object | 各平台安装包信息映射 |

**platforms 子对象字段说明：**

| 字段 | 必填 | 类型 | 说明 |
|------|------|------|------|
| `url` | **是** | String | 安装包下载 URL |
| `size` | 否 | Long | 文件大小（字节），用于进度条显示 |
| `sha256` | 否 | String | SHA-256 校验和，用于文件完整性验证 |

**平台标识 (platforms 的 key)：**

| 平台标识 | 说明 | 对应安装格式 |
|----------|------|-------------|
| `win_x64` | Windows 64 位 | `.exe` `.msi` `.zip` |
| `win_x86` | Windows 32 位 | `.exe` `.msi` `.zip` |
| `mac` | macOS | `.dmg` `.pkg` |
| `linux` | Linux | `.deb` `.rpm` `.tar.gz` |
| `other` | 通用平台（任何系统均匹配） | 任意格式 |

#### 格式二：经典格式

适用于需要同时发布多个版本（如不同许可证版本）的复杂场景：

```json
{
  "name": "Your App Name",
  "changelog": "http://your-server.com/changelog.html",
  "icon": "http://your-server.com/app-icon.png",
  "licenses": "http://your-server.com/license.html",
  "releases": [
    {
      "id": 2000000,
      "version": "2.0.0",
      "licenseVersion": 1,
      "releaseDate": "2025-01-15",
      "officialDownloadAddress": "https://your-website.com/download",
      "installationFileInfo": [
        {
          "downloadLink": "http://your-server.com/YourApp-2.0.0.exe",
          "fileSize": 52428800,
          "platform": "win_x64",
          "checksum": "a1b2c3d4e5f6..."
        },
        {
          "downloadLink": "http://your-server.com/YourApp-2.0.0.dmg",
          "fileSize": 61865984,
          "platform": "mac"
        },
        {
          "downloadLink": "http://your-server.com/YourApp-2.0.0.deb",
          "fileSize": 48234496,
          "platform": "linux"
        }
      ]
    }
  ]
}
```

**自动检测规则：**
- JSON 中含有 `"releases"` 数组 → 经典格式
- JSON 中含有 `"version"` + `"platforms"` → 简化格式
- 两者都不满足 → 抛出 `IllegalArgumentException`

---

### 3.3 theme.css 主题样式

在 `src/main/resources/` 根目录下放置 `theme.css` 文件，FXUpdater 会自动加载并应用到更新界面。

#### 可自定义的 CSS 选择器

```css
/* FX-Updater 主题样式 */

/* 主容器布局 */
.vbox {
  -fx-spacing: 20px;
  -fx-padding: 20px;
  -fx-background-color: -color-bg-default;  /* 可替换为 #ffffff 等固定颜色 */
}

/* 文本标签 */
.label {
  -fx-font-family: "Inter";       /* 字体族 */
  -fx-font-size: 13px;            /* 字号 */
  -fx-text-fill: -color-fg-muted; /* 文字颜色 */
}

/* 按钮容器 */
.hbox {
  -fx-alignment: center-right;
  -fx-spacing: 20px;
}

/* 按钮通用样式 */
.button {
  -fx-padding: 5px 20px;
  -fx-background-radius: 5px;
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
}

/* 忽略此版本按钮 */
.button-ignore {
  -fx-background-color: -color-accent-subtle;
  -fx-text-fill: -color-fg-muted;
}

/* 取消/稍后提醒按钮 */
.button-cancel {
  -fx-background-color: -color-danger-emphasis;
  -fx-text-fill: white;
}

/* 立即更新按钮 */
.button-confirm {
  -fx-background-color: -color-success-emphasis;
  -fx-text-fill: white;
}

/* 滚动条样式 */
.scroll-bar { -fx-background-color: transparent; }
.scroll-bar .track { -fx-background-color: #e0e0e0; -fx-background-radius: 4px; }
.scroll-bar .thumb { -fx-background-color: #c0c0c0; -fx-background-radius: 4px; }
.scroll-bar .thumb:hover { -fx-background-color: #a0a0a0; }
.scroll-bar .increment-button,
.scroll-bar .decrement-button { -fx-opacity: 0; }
```

> **提示**：如果不需要自定义主题，可以不创建 `theme.css` 文件，FXUpdater 将使用 JavaFX 默认样式。

---

## 四、核心 API 详解

### 4.1 FXUpdater 类

**包名**：`com.lxwise.updater.service.FXUpdater`

FXUpdater 是整个更新库的核心入口类，负责协调更新检查、下载和安装流程。

#### 构造函数

| 构造函数 | 说明 |
|----------|------|
| `FXUpdater(Class<?> application)` | 自动读取 `/app.properties` 和 `/theme.css` 创建实例 |
| `FXUpdater(String configUrl, String version, Integer releaseId, Integer licenseVersion, String themeCssUrl)` | 手动指定所有参数创建实例 |
| `FXUpdater(Properties properties, String cssUrl)` | 从 Properties 对象创建实例 |
| `FXUpdater(UpdateConfig config)` | 通过 UpdateConfig Builder 创建实例（**包级私有**，由 `UpdateConfig.build()` 内部调用） |

#### 公共方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `checkAppUpdate()` | void | 开始检查更新，无回调、不自动关闭 |
| `checkAppUpdate(Runnable callback)` | void | 带回调的更新检查 |
| `checkAppUpdate(int autoCloseSeconds)` | void | 带自动关闭时间的更新检查 |
| `checkAppUpdate(Runnable callback, int autoCloseSeconds)` | void | **完整版**：带回调 + 自动关闭时间 |

#### Getter 方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getUpdateConfigUrl()` | String | 获取更新配置 URL |
| `getVersion()` | String | 获取当前版本号 |
| `getReleaseId()` | Integer | 获取内部版本 ID |
| `getLicenseVersion()` | int | 获取许可证版本 |
| `getThemeCssUrl()` | String | 获取主题 CSS URL |
| `getUpdateMode()` | UpdateMode | 获取更新模式 |
| `isAutoRestart()` | boolean | 是否自动重启 |

---

### 4.2 UpdateConfig 类（Builder 模式）

**包名**：`com.lxwise.updater.service.UpdateConfig`

UpdateConfig 提供流式 API（Builder 模式）简化配置过程，是推荐的使用方式。

#### 静态工厂方法

| 方法 | 说明 |
|------|------|
| `UpdateConfig.builder()` | 创建空白 Builder，需手动设置所有参数 |
| `UpdateConfig.fromApplication(Class<?>)` | 从应用类加载 `/app.properties` 和 `/theme.css` |
| `UpdateConfig.fromProperties(Properties)` | 从 Properties 对象加载配置 |

#### Builder 链式方法

| 方法 | 参数类型 | 默认值 | 说明 |
|------|----------|--------|------|
| `.configUrl(String)` | String | **必填** | 服务端更新配置 URL |
| `.version(String)` | String | **必填** | 当前应用版本号 |
| `.releaseId(Integer)` | Integer | 自动推导 | 内部版本 ID（可选） |
| `.licenseVersion(Integer)` | Integer | `1` | 许可证版本号（可选） |
| `.themeCss(String)` | String | `null` | 主题 CSS 文件 URL |
| `.updateMode(UpdateMode)` | UpdateMode | `INTERACTIVE` | 更新模式 |
| `.autoCloseSeconds(int)` | int | `-1`（不关闭） | 弹窗自动关闭时间（秒） |
| `.autoRestart(boolean)` | boolean | `false` | 更新后是否自动重启 |
| `.callback(Runnable)` | Runnable | `null` | 更新完成/关闭后的回调 |
| `.connectTimeout(int)` | int | `15000` | HTTP 连接超时（毫秒） |
| `.readTimeout(int)` | int | `30000` | HTTP 读取超时（毫秒） |
| `.maxRetries(int)` | int | `3` | 下载失败最大重试次数 |
| `.checksumVerification(boolean)` | boolean | `true` | 是否启用文件校验 |

#### 终端方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `.build()` | FXUpdater | 构建 FXUpdater 实例（需后续手动调用 `checkAppUpdate`） |
| `.checkUpdate()` | void | **一步到位**：构建实例并立即执行更新检查 |

---

### 4.3 UpdateMode 枚举

**包名**：`com.lxwise.updater.service.UpdateMode`

| 枚举值 | 说明 | 用户交互 |
|--------|------|----------|
| `INTERACTIVE` | 交互式更新（**默认**）：弹出对话框，用户手动选择是否更新 | 弹窗 |
| `SILENT` | 静默更新：后台自动下载并安装，不弹出任何 UI | 无 |
| `BACKGROUND_DOWNLOAD` | 后台下载：后台自动下载，下载完成后弹出安装提示 | 下载完弹窗 |
| `CHECK_ONLY` | 仅检查：仅检查是否有更新，通过回调通知结果 | 无 |

---

## 五、使用示例

### 5.1 一行式极简调用（推荐）

```java
import com.lxwise.updater.service.UpdateConfig;

@Override
public void start(Stage primaryStage) {
    // ... 初始化你的应用界面 ...

    // 一行代码完成更新检查（自动读取 /app.properties 和 /theme.css）
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        // 配置文件读取失败，可忽略或记录日志
        e.printStackTrace();
    }
}
```

### 5.2 经典构造方式

#### 方式 A：通过应用主类自动加载

```java
import com.lxwise.updater.service.FXUpdater;

// 自动读取 /app.properties 和 /theme.css
FXUpdater updater = new FXUpdater(YourApp.class);

// 无回调
updater.checkAppUpdate();

// 带回调
updater.checkAppUpdate(() -> {
    System.out.println("更新检查完成或弹窗已关闭");
});

// 带回调 + 自动关闭（5秒后自动关闭弹窗）
updater.checkAppUpdate(() -> {
    System.out.println("弹窗已关闭");
}, 5);

// 仅自动关闭，无回调
updater.checkAppUpdate(10);
```

#### 方式 B：手动指定所有参数

```java
FXUpdater updater = new FXUpdater(
    "http://your-server.com/update.json",  // configUrl
    "1.0.0",                                // version
    1000000,                                // releaseId
    1,                                      // licenseVersion
    null                                    // themeCssUrl（不使用自定义主题）
);
updater.checkAppUpdate();
```

### 5.3 Builder 高级配置

```java
import com.lxwise.updater.service.UpdateConfig;
import com.lxwise.updater.service.UpdateMode;

// 从配置文件加载 + 自定义选项
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)    // 交互式
    .autoCloseSeconds(8)                   // 8秒自动关闭
    .autoRestart(true)                     // 安装后自动重启
    .connectTimeout(10000)                 // 连接超时 10 秒
    .readTimeout(60000)                    // 读取超时 60 秒
    .maxRetries(5)                         // 最多重试 5 次
    .callback(() -> {
        System.out.println("更新流程结束");
    })
    .checkUpdate();
```

### 5.4 四种更新模式示例

#### 交互式更新（默认）

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)
    .checkUpdate();
```

弹出更新提示对话框，显示版本信息和更新日志。用户可选择「立即更新」、「稍后提醒」或「忽略此版本」。

#### 静默更新

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.SILENT)
    .autoRestart(true)
    .maxRetries(5)
    .callback(() -> System.out.println("静默更新完成"))
    .checkUpdate();
```

后台自动下载并安装，全程无 UI 提示。适用于企业内部应用的强制更新场景。

#### 后台下载

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.BACKGROUND_DOWNLOAD)
    .callback(() -> System.out.println("后台下载完成"))
    .checkUpdate();
```

后台静默下载，下载完成后弹出安装提示。如果下载失败，自动回退到交互式模式让用户重试。

#### 仅检查

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.CHECK_ONLY)
    .callback(() -> {
        // 有更新可用时触发此回调
        System.out.println("检测到新版本！");
        // 可在此处自定义通知逻辑
    })
    .checkUpdate();
```

仅检查是否有新版本，不执行任何下载或安装操作。通过 callback 回调通知检查结果。

### 5.5 纯代码手动配置（不读取配置文件）

```java
// 完全通过代码配置，不依赖 app.properties 文件
UpdateConfig.builder()
    .configUrl("http://your-server.com/update.json")
    .version("1.0.0")
    // releaseId 可不填，从 version 自动推导为 1000000
    // licenseVersion 可不填，默认为 1
    .updateMode(UpdateMode.INTERACTIVE)
    .connectTimeout(15000)
    .readTimeout(30000)
    .maxRetries(3)
    .callback(() -> System.out.println("更新完成"))
    .checkUpdate();
```

### 5.6 使用 build() 获取 FXUpdater 实例

```java
// 如果需要获取 FXUpdater 实例进行更灵活的控制
FXUpdater updater = UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)
    .autoRestart(true)
    .build();

// 后续可多次调用
updater.checkAppUpdate();
updater.checkAppUpdate(() -> { /* 回调 */ });
updater.checkAppUpdate(() -> { /* 回调 */ }, 10);
```

---

## 六、功能特性详解

### 6.1 四种更新模式

```
┌─────────────────────────────────────────────────────────┐
│                    checkAppUpdate()                       │
│                         │                                 │
│              ┌──────────┼──────────┐                     │
│              ▼          ▼          ▼          ▼           │
│         INTERACTIVE   SILENT  BACKGROUND  CHECK_ONLY     │
│              │          │      _DOWNLOAD      │           │
│         弹出更新     后台下载   后台下载     仅检查        │
│         对话框       +安装      完成后      是否有更新     │
│              │          │      弹出安装        │           │
│         用户选择     无UI提示   提示框      回调通知       │
│         是否更新        │          │           │           │
│              │          │          │           │           │
│         下载+安装   下载+安装   用户确认    结束           │
│                                  安装                     │
└─────────────────────────────────────────────────────────┘
```

| 模式 | 下载过程 | 安装过程 | UI 交互 | 适用场景 |
|------|----------|----------|---------|---------|
| INTERACTIVE | 用户确认后开始 | 显示进度条 | 完整 UI | 普通桌面应用 |
| SILENT | 自动后台下载 | 自动静默安装 | 无 | 企业强制更新 |
| BACKGROUND_DOWNLOAD | 自动后台下载 | 下载完弹窗确认 | 部分 UI | 非打扰式体验 |
| CHECK_ONLY | 不下载 | 不安装 | 无 | 自定义更新 UI |

### 6.2 语义化版本比较

FXUpdater 2.0 引入了 `VersionUtil` 工具类，支持标准 SemVer 语义版本比较，**消除了对 releaseId 数值的强依赖**。

**支持的版本号格式：**

```
1.0.0          标准三段式
2.1            两段式（patch 默认为 0）
3              单段式（minor 和 patch 默认为 0）
v1.0.0         带 v 前缀
V2.1.3         带 V 前缀
1.0.0-beta1    带预发布标签
1.0.0-alpha    带预发布标签
1.0.0-rc.1     带预发布标签
```

**比较规则：**

1. 按 `major.minor.patch` 逐段比较数值大小
2. 数值部分相同时比较预发布标签：
   - 有预发布标签 < 无预发布标签（`1.0.0-beta` < `1.0.0`）
   - 两个都有预发布标签时按字典序比较（`alpha` < `beta`）
3. 当无法进行语义比较时，自动回退到 `releaseId` 数值比较

### 6.3 断点续传与重试机制

#### 断点续传

下载过程中如果中断（网络断开、应用关闭等），下次下载时会自动检测已下载的临时文件：

1. 如果已下载文件大小与目标一致 → 直接使用，跳过下载
2. 如果部分下载且服务器支持 `Accept-Ranges: bytes` → 从断点恢复下载
3. 否则 → 删除旧文件，从头开始下载

#### 重试机制

下载失败时自动重试，采用递增延迟策略：

```
第 1 次重试：等待 2 秒
第 2 次重试：等待 4 秒
第 3 次重试：等待 6 秒
...
```

通过 `maxRetries()` 配置最大重试次数（默认 3 次）。

### 6.4 跨平台支持

FXUpdater 自动检测运行平台，匹配对应的安装包：

| 操作系统 | 自动匹配平台 | 支持安装格式 |
|----------|-------------|-------------|
| Windows 64 位 | `win_x64` | `.exe` `.msi` `.zip` |
| Windows 32 位 | `win_x86` | `.exe` `.msi` `.zip` |
| macOS | `mac` | `.dmg` `.pkg` |
| Linux | `linux` | `.deb` `.rpm` `.tar.gz` `.zip` |

特殊平台标识 `other` 可匹配任何操作系统。

#### 各格式安装行为

| 格式 | 安装方式 | 静默模式支持 |
|------|---------|-------------|
| `.exe` | `cmd /c start` 或 `/S` 静默参数 | 是 |
| `.msi` | `msiexec /i` 或 `/quiet` 静默参数 | 是 |
| `.dmg` | 内置 shell 脚本挂载安装 | 否 |
| `.pkg` | `sudo installer -pkg` | 否 |
| `.deb` | `sudo dpkg -i` | 否 |
| `.rpm` | `sudo rpm -i` | 否 |
| `.tar.gz` | `tar -xzf` 解压到 `user.home` | 否 |
| `.zip` | PowerShell `Expand-Archive` / `unzip` | 否 |

### 6.5 自动重启功能

通过 `AppRestartUtil.restartApplication()` 实现跨平台应用重启：

```java
UpdateConfig.fromApplication(YourApp.class)
    .autoRestart(true)  // 启用自动重启
    .checkUpdate();
```

重启原理：
1. 获取当前 JVM 的 `java.home` 路径
2. 收集当前 JVM 启动参数（通过 `RuntimeMXBean`）
3. 检测启动方式（jar 包或 class 方式）
4. 构建新进程命令并启动
5. Windows 上优先使用 `javaw.exe`（无控制台窗口）

手动调用重启：

```java
import com.lxwise.updater.utils.AppRestartUtil;

AppRestartUtil.restartApplication(YourApp.class);
```

### 6.6 日志记录

FXUpdater 使用 `UpdateLogger` 统一管理日志输出：

```java
import com.lxwise.updater.utils.UpdateLogger;
import java.util.logging.*;

// 日志会自动初始化，也可以手动配置

// 设置日志级别
UpdateLogger.setLevel(Level.FINE);  // 开启 DEBUG 级别

// 添加文件日志处理器
FileHandler fileHandler = new FileHandler("fxupdater.log", true);
fileHandler.setFormatter(new SimpleFormatter());
UpdateLogger.addHandler(fileHandler);
```

**日志输出格式：**

```
[2025-01-15 14:30:25.123] [INFO] [FX-Updater] Starting update check (mode: INTERACTIVE, configUrl: http://...)
[2025-01-15 14:30:26.456] [INFO] [FX-Updater] Fetching update config from: http://...
[2025-01-15 14:30:27.789] [INFO] [FX-Updater] Parsed simplified format config: My App (version: 2.0.0)
[2025-01-15 14:30:28.012] [INFO] [FX-Updater] Update found: version 2.0.0 (id: 2000000)
```

**日志级别：**

| 方法 | 对应 java.util.logging 级别 | 说明 |
|------|---------------------------|------|
| `UpdateLogger.debug()` | `FINE` | 调试信息 |
| `UpdateLogger.info()` | `INFO` | 一般信息 |
| `UpdateLogger.warn()` | `WARNING` | 警告信息 |
| `UpdateLogger.error()` | `SEVERE` | 错误信息（支持附带 Throwable） |

### 6.7 国际化（i18n）

FXUpdater 内置 12 种语言支持，自动根据系统 Locale 选择：

| 语言 | 文件名 |
|------|--------|
| 英语（默认） | `updater.properties` |
| 简体中文 | `updater_zh_CN.properties` |
| 繁体中文 | `updater_zh_TW.properties` |
| 日语 | `updater_ja.properties` |
| 韩语 | `updater_ko.properties` |
| 法语 | `updater_fr.properties` |
| 德语 | `updater_de.properties` |
| 西班牙语 | `updater_es.properties` |
| 意大利语 | `updater_it.properties` |
| 葡萄牙语 | `updater_pt.properties` |
| 俄语 | `updater_ru.properties` |
| 阿拉伯语 | `updater_ar.properties` |

---

## 七、工具类 API

### 7.1 VersionUtil 版本比较工具

**包名**：`com.lxwise.updater.utils.VersionUtil`

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `compare(String v1, String v2)` | int | 比较两个版本号：正数=v1更大，负数=v2更大，0=相等 |
| `isNewer(String currentVersion, String newVersion)` | boolean | 判断 newVersion 是否比 currentVersion 更新 |
| `toReleaseId(String version)` | int | 从版本号生成数值ID（major×10^6 + minor×10^3 + patch） |

```java
// 示例
VersionUtil.compare("2.0.0", "1.5.3");        // > 0
VersionUtil.compare("1.0.0-beta", "1.0.0");   // < 0
VersionUtil.compare("v1.0.0", "1.0.0");       // = 0

VersionUtil.isNewer("1.0.0", "2.0.0");        // true
VersionUtil.isNewer("2.0.0", "1.0.0");        // false

VersionUtil.toReleaseId("2.1.3");             // 2001003
VersionUtil.toReleaseId("1.0.0");             // 1000000
```

### 7.2 HttpUtils HTTP 工具

**包名**：`com.lxwise.updater.utils.HttpUtils`

| 方法 | 说明 |
|------|------|
| `openConnection(URL url)` | 使用默认超时打开连接 |
| `openConnection(URL url, int connectTimeout, int readTimeout)` | 使用自定义超时打开连接 |
| `openRangeConnection(URL url, long startByte, int connectTimeout, int readTimeout)` | 打开支持断点续传的连接 |
| `supportsResume(URLConnection connection)` | 检查服务器是否支持断点续传 |
| `executeWithRetry(RetryAction<T> action, int maxRetries)` | 带重试的执行操作 |
| `executeWithRetry(RetryAction<T> action)` | 使用默认重试次数（3次） |
| `calculateSHA256(InputStream inputStream)` | 计算 SHA-256 校验和 |
| `verifyChecksum(String expected, String actual)` | 验证校验和是否匹配 |
| `disconnect(URLConnection connection)` | 安全关闭连接 |

**RetryAction 函数式接口：**

```java
@FunctionalInterface
public interface RetryAction<T> {
    T execute(int attempt) throws Exception;
}
```

```java
// 使用示例
String result = HttpUtils.executeWithRetry(attempt -> {
    System.out.println("第 " + attempt + " 次尝试");
    URLConnection conn = HttpUtils.openConnection(new URL("http://example.com/data"));
    // ... 处理连接 ...
    return "success";
}, 5); // 最多重试 5 次
```

### 7.3 UpdateLogger 日志工具

**包名**：`com.lxwise.updater.utils.UpdateLogger`

| 方法 | 说明 |
|------|------|
| `init()` | 初始化日志系统（首次调用自动初始化，通常不需手动调用） |
| `addHandler(Handler handler)` | 添加自定义日志处理器（如 FileHandler） |
| `setLevel(Level level)` | 设置全局日志级别 |
| `debug(String message)` | 输出 DEBUG 日志 |
| `debug(String format, Object... args)` | 输出格式化 DEBUG 日志 |
| `info(String message)` | 输出 INFO 日志 |
| `info(String format, Object... args)` | 输出格式化 INFO 日志 |
| `warn(String message)` | 输出 WARNING 日志 |
| `warn(String format, Object... args)` | 输出格式化 WARNING 日志 |
| `error(String message)` | 输出 ERROR 日志 |
| `error(String message, Throwable throwable)` | 输出 ERROR 日志（附带异常堆栈） |
| `error(String format, Object... args)` | 输出格式化 ERROR 日志 |

### 7.4 AppRestartUtil 应用重启工具

**包名**：`com.lxwise.updater.utils.AppRestartUtil`

| 方法 | 说明 |
|------|------|
| `restartApplication(Class<?> appMainClass)` | 重启当前 Java 应用，自动检测 jar/class 启动方式 |
| `getCurrentPID()` | 获取当前进程 PID（Java 9+ ProcessHandle 或 RuntimeMXBean 回退） |

### 7.5 GuiUtils GUI 工具

**包名**：`com.lxwise.updater.gui.GuiUtils`

| 方法 | 说明 |
|------|------|
| `setStageIcon(ReleaseInfoModel release, Stage stage)` | 设置窗口图标（自动回退到默认图标） |
| `setupCloseConfirmation(Stage stage, ResourceBundle i18nBundle, Runnable onConfirmClose)` | 设置窗口关闭确认对话框 |
| `buildStageTitle(ReleaseInfoModel release, ResourceBundle i18nBundle)` | 构建窗口标题 |
| `formatFileSize(double fileSizeInBytes)` | 字节数格式化为人类可读格式（如 `15.3 MB`） |

---

## 八、数据模型

### AppInfoModel

应用信息模型，对应服务端 JSON 配置的顶层结构。

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | String | 应用名称 |
| `changelog` | String | 更新日志（文本或 URL） |
| `licenses` | String | 许可证信息 |
| `icon` | String | 应用图标 URL |
| `releases` | List\<ReleaseInfoModel\> | 发布版本列表 |

### ReleaseInfoModel

发布版本信息模型。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Integer | 版本数值 ID |
| `version` | String | 版本号字符串 |
| `licenseVersion` | Integer | 许可证版本号 |
| `releaseDate` | Date | 发布日期 |
| `officialDownloadAddress` | String | 官方下载地址/官网地址 |
| `installationFileInfo` | List\<InstallationFileInfoModel\> | 安装包信息列表 |
| `appInfo` | AppInfoModel | 反向引用父级应用信息 |

### InstallationFileInfoModel

安装文件信息模型。

| 字段 | 类型 | 说明 |
|------|------|------|
| `downloadLink` | String | 安装包下载 URL |
| `fileSize` | Long | 文件大小（字节） |
| `platform` | String | 平台标识（`win_x64`/`mac`/`linux` 等） |
| `checksum` | String | SHA-256 校验和 |

### EPlatformModel 枚举

| 枚举值 | 平台标识字符串 | 说明 |
|--------|--------------|------|
| `win_x64` | `"win_x64"` | Windows 64 位 |
| `win_x86` | `"win_x86"` | Windows 32 位 |
| `mac` | `"mac"` | macOS |
| `linux` | `"linux"` | Linux |
| `other` | `"other"` | 通用平台 |

---

## 九、向后兼容性说明

FXUpdater 2.0 在重构过程中严格保持了向后兼容性：

### 保留的旧版 API

| 旧版 API | 状态 | 说明 |
|----------|------|------|
| `new FXUpdater(Class<?>)` | ✅ 完全兼容 | 自动读取 `/app.properties` |
| `new FXUpdater(String, String, Integer, Integer, String)` | ✅ 完全兼容 | 5 参数构造函数 |
| `new FXUpdater(Properties, String)` | ✅ 完全兼容 | Properties 构造函数 |
| `checkAppUpdate()` | ✅ 完全兼容 | 无参版本 |
| `checkAppUpdate(Runnable)` | ✅ 完全兼容 | 带回调版本 |
| `checkAppUpdate(int)` | ✅ 完全兼容 | 带自动关闭版本 |
| `checkAppUpdate(Runnable, int)` | ✅ 完全兼容 | 完整版本 |

### 配置文件兼容性

| 配置格式 | 状态 | 说明 |
|----------|------|------|
| 4 项 `app.properties`（含 releaseId、licenseVersion） | ✅ 完全兼容 | 可选项有则读取 |
| 2 项 `app.properties`（仅 version、configUrl） | ✅ 新增支持 | releaseId 自动推导 |
| 经典 JSON 格式（含 releases 数组） | ✅ 完全兼容 | 自动检测 |
| 简化 JSON 格式（version + platforms） | ✅ 新增支持 | 自动检测 |

### 升级指南

**从 1.x 升级到 2.0：**

1. **无需修改任何现有代码** — 所有旧版构造函数和方法签名完全保留
2. **无需修改任何配置文件** — 旧版 4 项配置和经典 JSON 格式继续支持
3. **可选简化配置** — 如果想简化，可删除 `app.properties` 中的 `releaseId` 和 `licenseVersion`
4. **可选使用新 API** — 推荐使用 `UpdateConfig.fromApplication().checkUpdate()` 新写法

---

## 十、错误处理与异常说明

### 异常类型

| 异常 | 抛出位置 | 原因 | 处理建议 |
|------|---------|------|---------|
| `IOException` | `FXUpdater(Class<?>)`、`UpdateConfig.fromApplication()` | `/app.properties` 文件不存在或读取失败 | 检查配置文件是否在 classpath 根目录 |
| `IllegalStateException` | `UpdateConfig.build()` | `configUrl` 或 `version` 未设置 | 确保必填参数已设置 |
| `IllegalArgumentException` | `AcquireUpdateConfigService` | JSON 格式无效（缺少必要字段） | 检查服务端 JSON 配置 |
| `NoUpdateException` | `CheckUpdateService` | 当前已是最新版本，无可用更新 | 正常情况，不需要处理 |
| `IllegalArgumentException` | `InstallFileDownloadService` | 当前平台无对应安装包 | 在 JSON 配置中添加当前平台的安装包信息 |
| `IllegalArgumentException` | `ExecuteInstallerService` | 不支持的安装包格式 | 使用支持的格式（exe/msi/dmg/pkg/deb/rpm/tar.gz/zip） |

### 错误处理最佳实践

```java
// 方式1：使用 try-catch 捕获配置错误
try {
    UpdateConfig.fromApplication(YourApp.class)
        .callback(() -> {
            // 即使更新失败或无更新，callback 也会被调用
            System.out.println("更新流程结束");
        })
        .checkUpdate();
} catch (IOException e) {
    // 配置文件读取失败，记录日志但不影响应用正常运行
    System.err.println("Update check skipped: " + e.getMessage());
}

// 方式2：更新检查失败时的回调处理
// FXUpdater 内部已处理所有异常，通过回调通知结果
// 即使网络不可用或服务器宕机，callback 也会被安全调用
```

### 内部异常处理机制

FXUpdater 对所有异步操作都内置了完善的异常处理：

- **配置获取失败** → 调用 callback，记录错误日志
- **版本检查失败/无更新** → 调用 callback，记录 info 日志
- **下载失败** → 自动重试 N 次，全部失败后调用 callback
- **安装失败** → SILENT 模式调用 callback；交互模式在 UI 中显示错误信息
- **后台下载失败** → 自动回退到交互式模式让用户手动重试

---

## 十一、最佳实践建议

### 1. 推荐的调用时机

```java
@Override
public void start(Stage primaryStage) {
    // 先展示应用主界面
    primaryStage.show();

    // 应用启动后异步检查更新（不阻塞用户操作）
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        // 更新检查失败不应影响应用正常使用
    }
}
```

### 2. 推荐的配置方案

**app.properties（客户端）：**
```properties
# 仅需 2 项必填
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/update.json
```

**app-update-config.json（服务端，使用简化格式）：**
```json
{
  "name": "Your App",
  "version": "2.0.0",
  "changelog": "- 新功能A\n- 修复问题B",
  "platforms": {
    "win_x64": { "url": "http://server/App-2.0.0.exe", "size": 52428800 },
    "mac":     { "url": "http://server/App-2.0.0.dmg", "size": 61865984 }
  }
}
```

### 3. 版本号管理建议

- 使用标准 SemVer 格式：`MAJOR.MINOR.PATCH`
- 发布新版本时只需更新 `app.properties` 中的 `version` 和服务端 JSON
- 不需要手动维护 `releaseId`，由 VersionUtil 自动推导
- 客户端 version 小于服务端 version 时自动触发更新

### 4. 服务端部署建议

- 将 `app-update-config.json` 部署在 CDN 或静态文件服务器上
- 更新 JSON 文件的 `version` 字段和对应平台的 `url` 即可发布新版本
- 建议提供 `size` 字段以显示准确的下载进度
- 建议提供 `sha256` 字段以验证下载文件的完整性
- 建议提供 `downloadUrl` 作为自动下载失败时的手动下载备选方案

### 5. 日志配置建议

```java
// 生产环境：仅记录 WARNING 及以上
UpdateLogger.setLevel(Level.WARNING);

// 开发/调试环境：开启所有日志
UpdateLogger.setLevel(Level.ALL);

// 输出到文件
try {
    FileHandler fh = new FileHandler("fxupdater_%g.log", 10 * 1024 * 1024, 3, true);
    fh.setFormatter(new SimpleFormatter());
    UpdateLogger.addHandler(fh);
} catch (IOException e) {
    e.printStackTrace();
}
```

### 6. 不同场景的推荐模式

| 场景 | 推荐模式 | 配置建议 |
|------|---------|---------|
| 普通桌面应用 | `INTERACTIVE` | 默认即可 |
| 企业内部工具 | `SILENT` | `autoRestart(true)` |
| 游戏/大型应用 | `BACKGROUND_DOWNLOAD` | `maxRetries(5)` |
| 自定义 UI | `CHECK_ONLY` | 在 callback 中实现自定义逻辑 |
| 启动时快速检查 | `INTERACTIVE` | `autoCloseSeconds(10)` |

---

## 十二、项目结构

```
fx-updater/
├── src/main/java/com/lxwise/updater/
│   ├── gui/                                # GUI 层
│   │   ├── UpdaterDialogController.java    # 更新对话框控制器
│   │   ├── UpdaterProgressController.java  # 下载进度控制器
│   │   └── GuiUtils.java                  # GUI 公共工具
│   ├── model/                              # 数据模型
│   │   ├── AppInfoModel.java              # 应用信息
│   │   ├── ReleaseInfoModel.java          # 版本发布信息
│   │   ├── InstallationFileInfoModel.java # 安装包信息
│   │   └── EPlatformModel.java            # 平台枚举
│   ├── service/                            # 服务层（核心逻辑）
│   │   ├── FXUpdater.java                 # ★ 更新器入口
│   │   ├── UpdateConfig.java              # ★ Builder 配置器
│   │   ├── UpdateMode.java                # 更新模式枚举
│   │   ├── AcquireUpdateConfigService.java # 获取远程配置
│   │   ├── CheckUpdateService.java        # 版本比较检查
│   │   ├── InstallFileDownloadService.java # 文件下载
│   │   └── ExecuteInstallerService.java   # 执行安装
│   └── utils/                              # 工具类
│       ├── VersionUtil.java               # 语义版本比较
│       ├── HttpUtils.java                 # HTTP 连接工具
│       ├── UpdateLogger.java              # 统一日志
│       ├── AppRestartUtil.java            # 应用重启
│       ├── InstallationScriptUtil.java    # 安装脚本工具
│       └── NoUpdateException.java         # 无更新异常
├── src/main/resources/
│   ├── com/lxwise/updater/
│   │   ├── gui/
│   │   │   ├── UpdaterDialog.fxml         # 更新对话框布局
│   │   │   └── UpdaterProgress.fxml       # 进度窗口布局
│   │   ├── i18n/
│   │   │   ├── updater.properties         # 英语（默认）
│   │   │   ├── updater_zh_CN.properties   # 简体中文
│   │   │   └── ... (12种语言)
│   │   └── utils/
│   │       └── installdmg.sh              # macOS DMG 安装脚本
│   └── images/
│       └── fx-updater-logo.png            # 默认图标
└── src/test/                               # 测试目录
    ├── java/com/lxwise/updater/
    │   ├── SimpleUpdateDemo.java           # Demo1：经典用法
    │   ├── AdvancedUpdateDemo.java         # Demo2：Builder 高级用法
    │   ├── SimpleUpdateStart.java          # Demo1 启动入口
    │   └── AdvancedUpdateStart.java        # Demo2 启动入口
    └── resources/
        ├── app.properties                  # 测试配置
        ├── app-update-config.json          # 测试 JSON
        └── theme.css                       # 测试主题
```

---

## 十三、常见问题 FAQ

### Q1：最少需要几个文件才能启用自动更新？

**客户端**：1 个文件 `app.properties`（2 行配置）+ 1 行 Java 代码  
**服务端**：1 个 JSON 文件 + 安装包文件

### Q2：releaseId 还需要手动维护吗？

不需要。2.0 版本引入语义版本比较后，`releaseId` 可从版本号自动推导（`toReleaseId("2.1.3")` → `2001003`），配置文件中不再需要填写。

### Q3：如何同时支持 Windows 和 macOS？

在 JSON 配置中同时提供两个平台的安装包即可：

```json
"platforms": {
  "win_x64": { "url": "http://.../App.exe", "size": 52428800 },
  "mac":     { "url": "http://.../App.dmg", "size": 61865984 }
}
```

FXUpdater 会自动检测运行平台并下载对应安装包。

### Q4：如何自定义更新界面？

有两种方式：
1. **CSS 主题**：在 `src/main/resources/theme.css` 中自定义样式
2. **CHECK_ONLY 模式**：使用仅检查模式，在回调中实现完全自定义的更新 UI

### Q5：下载中断后会丢失已下载内容吗？

不会。FXUpdater 支持断点续传，如果服务器支持 `Accept-Ranges`，会自动从断点恢复下载。

### Q6：更新检查失败会影响应用正常运行吗？

不会。FXUpdater 的所有网络操作都是异步执行的，内部对所有异常都做了处理。即使网络不可用、服务器宕机或配置错误，都不会影响应用的正常运行。

### Q7：如何在没有 app.properties 的情况下使用？

使用纯 Builder 方式：

```java
UpdateConfig.builder()
    .configUrl("http://your-server.com/update.json")
    .version("1.0.0")
    .checkUpdate();
```

### Q8：changelog 支持什么格式？

`changelog` 字段支持两种方式：
1. **文本内容**：直接写入更新日志文本，支持 `\n` 换行
2. **URL 地址**：提供一个 URL，FXUpdater 会异步加载该 URL 的内容并显示

### Q9：如何处理多个许可证版本？

使用经典 JSON 格式，在 `releases` 数组中定义多个版本，每个版本设置不同的 `licenseVersion`：

```json
{
  "releases": [
    { "version": "2.0.0", "licenseVersion": 1, "installationFileInfo": [...] },
    { "version": "3.0.0", "licenseVersion": 2, "installationFileInfo": [...] }
  ]
}
```

FXUpdater 会根据客户端的 `licenseVersion` 自动匹配对应的最新版本。

### Q10：版本号比较支持预发布标签吗？

支持。`1.0.0-beta` < `1.0.0-rc` < `1.0.0`。预发布版本始终低于同版本号的正式版本。
