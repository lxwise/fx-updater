## FXUpdater

<p align="center">
  <a href="https://github.com/lxwise/fx-updater/">
    <img src="./doc/fx-updater.png" alt="FXUpdater" ">
  </a>
</p>

<p align="center">
FXUpdater是一个JavaFX 应用程序的自动更新启动器。提供了一个一体化的方案来解决基于JavaFX 框架并打包为独立桌面应用程序的应用程序的更新升级问题。
</p>

<p align="center">
   <a target="_blank" href="https://github.com/lxwise/fx-updater">
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/build-maven-green"/>
      <img src="https://img.shields.io/badge/java-9%2B-%23F27E3F"/>
   </a>
</p>



[**中文文档**](https://github.com/lxwise/fx-updater/blob/master/README.md)

[**English DOC**](https://github.com/lxwise/fx-updater/blob/master/README.en.md)

## 概述

还在为写好应用不知道如何做更新而发愁吗？还在为研究外面各种更新框架的而不知道怎么使用而烦恼吗？那恭喜你FXUpdater就是解决您的烦恼而生。FXUpdater是一个纯java写的JavaFX 应用程序的自动更新启动器。无第三方框架，对当前应用程序完全无侵入，只需几个简单的配置即可开箱即用。FXUpdater提供了一个一体化的方案来解决基于JavaFX 框架并打包为独立桌面应用程序的应用程序的更新升级问题。它既具有漂亮的 GUI界面，提供简洁明了的操作，又具有全平台(Windows、MAC OS、Linux)执行更新所需的机制。如果您不喜欢默认的样式，只需简单的几行代码您就可以自定义更新程序的样式。

## 项目地址

**Gitee 地址：** [https://gitee.com/lxwise/fx-updater](https://gitee.com/lxwise/fx-updater)

**Github 地址：** [https://github.com/lxwise/fx-updater](https://github.com/lxwise/fx-updater)



## Star

ps: 虽然我知道，大部分人和作者菌一样喜欢白嫖，都是看了直接下载源代码后就潇洒的离开。但我还是想请各位喜欢本项目的小伙伴：**Star**，**Star**，**Star**。只有你们的**Star**本项目才能有更多的人看到，才有更多志同道合的小伙伴一起加入完善本项目。请小伙伴们动动您可爱的小手，给本项目一个**Star**。**同时也欢迎大家提交pr，一起改进项目** 。

## 原理

应用启动时先创建`FXUpdater`更新器并调用`checkAppUpdate`方法,调用 `checkAppUpdate` 方法时，后台线程会先去查找当前应用资源目录下的`app.properties`应用信息文件,读取里面的版本配置文件并查找更新。如果找到相关更新,再调用后台程序确认更新是否可用,这里使用的确定更新是否可用的算法超级简单：即如果存在编号较大的版本，则有可用的更新。如果找到更新，则会显示一个更新对话框，通知用户有可用更新，显示更新日志等信息。用户可以选择忽略或安装更新。如果用户选择安装更新，框架将根据找到的更新，根据当前平台自动下载对应的安装程序，同时会在GUI界面实时告知用户当前下载进度。当安装程序下载成功，通知用户可以进行安装，用户点击安装后程序将自动执行命令启动应用安装，更新结束时，应用程序将重新启动。安装过程中不需要用户干预。

<img src="/doc/fxupdater_01.gif" alt="fxupdater_01" style="zoom: 50%;" />

## 安装和使用

### 1.依赖安装

您可以使用 Maven [下载](https://repo1.maven.org/maven2/io/github/lxwise/fx-updater/)或安装：

- Maven:

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>fx-updater</artifactId>
    <version>1.0.3</version>
</dependency>
```

- Gradle:

```Groovy
dependencies {
    implementation group: 'io.github.lxwise', name: 'fx-updater', version: '1.0.3'
}
```

### 2.代码使用

1.通过主程序自动加载

```java
FXUpdater updater = new FXUpdater(App.class);
updater.checkAppUpdate();
```

2.自定义配置文件加载

```java
FXUpdater updater = updater = new FXUpdater(new URL("http://localhost:8080/updater/updateConfig.json"), "1.0.0", 1, 1, new URL("http://localhost:8080/updater/themeCssUrl.css"));
//默认不带回调函数和自动关闭
updater.checkAppUpdate();
//带回调函数和不带自动关闭
updater.checkAppUpdate(() -> {
        System.out.println("更新提示关闭或完成后执行回调");
            });
//带回调函数和带自动关闭
updater.checkAppUpdate(() -> {
        System.out.println("窗口关闭，执行自动回调");
            }, 5);
```

### 3.配置版本文件

#### 3.1 当前应用信息文件

在您的应用的资源目录下新建一个`app.properties`应用信息文件,配置如下:

```properties
app.update.releaseId = 10000
app.update.licenseVersion = 1
app.update.version = 1.0.0
app.update.configUrl = http://192.168.12.50:81/downloads/app-update-config.json
```

- `app.update.releaseId`:  内部版本号,必须是数字,此数字用于确定更新是否存在，因此必须为每个版本增加此数字。
- `app.update.licenseVersion`: 许可证版本。
- `app.update.version`: 当前应用程序版本。
- `app.update.configUrl`: 可用更新的配置文件所在地址。

#### 3.2 版本配置文件

新建一个`app-update-config`可用更新的配置文件,并放到可供下载的服务器,如Nginx或Tomcat。作者菌这里使用的是Nginx,后面会贴上Nginx作为下载服务器的配置，供各位小伙伴参考。`app-update-config`配置如下：

```json
{
    "name": "UpdateTest-FX App",
    "licenses": "http://192.168.12.50:81/downloads/LICENSE",
    "changelog": "http://192.168.12.50:81/downloads/changelog.html",
    "icon":"http://192.168.12.50:81/downloads/fx-updater.png",
    "releases": [
      {
        "id": "20000",
        "version": "2.0.0",
        "releaseDate": "2024-03-07",
        "licenseVersion": "1",
        "officialDownloadAddress": "https://www.lstar.icu/",
        "installationFileInfo": [
          {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-2.0.0.exe",
            "fileSize": 97677,
            "platform": "win_x64"
          },
          {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-2.0.0.dmg",
            "fileSize": 97677,
            "platform": "mac"
          },
          {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-2.0.0.rpm",
            "fileSize": 97677,
            "platform": "linux"
          }
        ]
      },
      {
        "id": "10000",
        "version": "1.0.0",
        "releaseDate": "2024-01-05",
        "licenseVersion": "1",
        "officialDownloadAddress": "https://www.lstar.icu/",
        "installationFileInfo": [
          {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-1.0.0.exe",
            "fileSize": 63995,
            "platform": "win_x64"
          },
          {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-1.0.0.dmg",
            "fileSize": 63995,
            "platform": "mac"
          },
            {
            "downloadLink": "http://192.168.12.50:81/downloads/UpdateTest-FX-1.0.0.rpm",
            "fileSize": 97677,
            "platform": "linux"
          }
        ]
      }
    ]
  }


```

**参数说明:**

- `name`: 应用名称。
- `licenses`:  许可证,可为空。
- `changelog`:  更新日志。
- `icon`:  应用图标,可为空,为空时使用默认应用图标。
- `releases`: 发布信息。
    - `id` 版本id,用于确定是否有更新的版本。
    - `version` 当前应用程序版本。
    - `releaseDate` 是发布日期,必须使用标准格式。
    - `licenseVersion` 许可证的版本。
    - `officialDownloadAddress` 手动下载地址/官网地址。用于自动下载失败后的兜底下载。
    - `installationFileInfo`  安装文件信息,一个发布版本中可包含多个平台的安装文件。
        - `downloadLink`  安装文件下载地址
        - `fileSize` 文件大小,字节为单位
        - `platform` 安装包对应的平台支持:mac、win_x64、win_x86、linux、other。

#### 3.3 更新日志文件

版本更新时伴随着改动说明，新建一个`changelog.txt`文件。

`changelog.txt`配置如下：

```txt
更新日志

版本 2.0.0 (2025-01-07)
- 新增对 macOS 平台的支持。
- 提升性能并修复了一些小问题。
- 更新用户界面，提升易用性。

版本 1.0.0 (2024-01-07)
- 发布 UpdateTest-FX 应用程序的初始版本。
- 支持 Windows 平台。

```



#### 3.4 许可证文件

这里的许可证许可证即我们常见的,如`Apache 2.0`、`MIT `、`GPL`、等

这里给的许可证模板是基于 MIT 许可证模板,文件名通常使用 `LICENSE` 或 `LICENSE.txt`。

```plaintext
MIT License

Copyright (c) 2024 [Your Name or Company Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### 说明

1. **文件名**: `LICENSE` 是开源项目的标准命名，也可以使用 `LICENSE.txt`。
2. **版权年份**: 替换为当前年份（如 2024）。
3. **版权人**: 替换为你的名字或公司名称。
4. **许可证类型**: 以上内容为 MIT 许可证，如果需要其他许可证（如 Apache 2.0、GPL），可以使用对应的模板。

#### 3.5 Nginx文件服务器配置

新加一个`server`模块,并在`http`模块中新加全局缓存配置

```shell
	# 全局缓存区域
	proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=download_cache:10m inactive=1d max_size=1g;	

	server {
    listen 81;
    server_name localhost;

    # 下载目录
	location /downloads/ {
	#如果使用其他目录请改为 alias /opt/myapp/app_updates/;
		root /var/www/app_updates/;
		autoindex on;
		add_header Cache-Control "public, max-age=31536000, immutable";
		add_header Last-Modified $date_gmt;
		add_header ETag $binary_remote_addr;
		expires 1h;
		if_modified_since exact;

		# Nginx 缓存
		proxy_cache download_cache;
		proxy_cache_valid 200 1h;
		proxy_cache_use_stale error timeout updating;
		add_header X-Cache-Status $upstream_cache_status;

		# 默认添加 Content-Disposition
		add_header Content-Disposition "attachment";

		# 取消 changelog 文件的下载行为
		location ~ /downloads/changelog(\.html|\.txt)?$ {
			add_header Content-Disposition ""; # 清空 attachment
			add_header Content-Type "text/html"; # 或 "text/plain" 根据文件类型设置
		}
	}


    # 最新版本快捷路径
    location /latest/ {
        root /var/www/app_updates/;
        autoindex off;
        add_header Content-Disposition "attachment";
    }
}
```

## 自定义UI

新建一个`theme.css`的css文件并放在资源目录下和`app-update-config`文件同级。创建`FXUpdater`时程序会自动加载样式文件,这个样式文件只针对于当前更新程序,对您的应用程序不会造成任何影响。

例如：

```css
/* VBox 样式 */
.vbox {
  -fx-spacing: 20px;
  -fx-padding: 20px;
  -fx-background-color: -color-bg-default;
}

/* Label样式 */
.label {
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
  -fx-text-fill: -color-fg-muted;
}

/* WebView样式 */
.web-view {
  -fx-background-color: transparent;
}

/* HBox样式 */
.hbox {
  -fx-alignment: center-right;
  -fx-spacing: 20px;
}

/* 按钮常见样式 */
.button {
  -fx-padding: 5px 20px;
  -fx-background-radius: 5px;
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
     }

/* 忽略按钮样式 */
.button-ignore {
  -fx-background-color: -color-accent-subtle;
  -fx-text-fill: -color-fg-muted;
}

/* 取消按钮样式 */
.button-cancel {
  -fx-background-color: -color-danger-emphasis;
  -fx-text-fill: white;
}

/* 确认按钮样式 */
.button-confirm {
  -fx-background-color: -color-success-emphasis;
  -fx-text-fill: white;
}

/* 设置滚动条的背景 */
.scroll-bar {
  -fx-background-color: transparent; /* 背景透明 */
  -fx-padding: 0;
}

/* 自定义滚动槽（轨道）的样式 */
.scroll-bar .track {
  -fx-background-color: #e0e0e0; /* 浅灰色 */
  -fx-background-radius: 4px; /* 圆角 */
}

/* 自定义滚动滑块（thumb）的样式 */
.scroll-bar .thumb {
  -fx-background-color: #e3e3e3;
  -fx-background-radius: 4px; /* 圆角 */
  -fx-padding: 2px;
}

/* 滑块的悬停效果 */
.scroll-bar .thumb:hover {
  -fx-background-color: #e3e3e3;
}

/* 设置箭头的透明度（可选） */
.scroll-bar .increment-button,
.scroll-bar .decrement-button {
  -fx-opacity: 0; /* 隐藏箭头 */
}

```

通过上面的配置，那么恭喜您，您的软件具备自动更新功能啦。

最后，我希望我的项目能够为你带来帮助与收获。如果你有任何建议或意见，欢迎随时联系我。让我们一起分享知识，共同成长！