## FXUpdater

<p align="center">
  <a href="https://github.com/lxwise/fx-updater/">
    <img src="./doc/fx-updater.png" alt="FXUpdater" ">
  </a>
</p>

<p align="center">
FXUpdater is an automatic update launcher for JavaFX applications. It provides an integrated solution to solve the update and upgrade problems of applications based on the JavaFX framework and packaged as independent desktop applications.
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

## Overview

Are you struggling to update your application after development? Are you frustrated by the complexity of using various external update frameworks? Congratulations, FXUpdater was born to solve these problems for you. FXUpdater is a pure Java-based auto-update launcher for JavaFX applications. It is free of third-party frameworks and completely non-intrusive to your existing application. With just a few simple configurations, you can use it out of the box.

FXUpdater offers an all-in-one solution for updating and upgrading JavaFX-based applications packaged as standalone desktop programs. It provides a beautiful GUI for simple and clear operations and includes mechanisms for updates across all platforms (Windows, macOS, Linux). If you dislike the default style, you can easily customize the updater’s appearance with a few lines of code.

## Project address

**Gitee ：** [https://gitee.com/lxwise/fx-updater](https://gitee.com/lxwise/fx-updater)

**Github ：** [https://github.com/lxwise/fx-updater](https://github.com/lxwise/fx-updater)



## Star

P.S.: Although many users, like the author, enjoy free access to the source code and move on after downloading, we encourage you to give this project a **Star**. Your **Star** will help others discover this project and attract like-minded contributors to improve it. Please click the **Star** button and feel free to submit PRs to enhance the project together.

## Principle

When the application starts, it creates an `FXUpdater` instance and calls the `checkAppUpdate` method. The `checkAppUpdate` method runs in the background and searches for an `app.properties` file in the application’s resource directory to read version configuration details and look for updates. If an update is found, the updater confirms its availability through a simple algorithm: if a higher version number exists, an update is available.

If an update is detected, a dialog will notify the user, showing the update log and options to ignore or install the update. If the user chooses to install, the updater automatically downloads the corresponding installer for the current platform and displays the download progress in the GUI. Once the download completes, the user can proceed with installation. The updater executes the installation command, and the application restarts after the update completes. No manual intervention is required during the installation process.

<img src="assets/fxupdater_01-17364983999291.gif" alt="fxupdater_01" style="zoom: 50%;" />

## Installation and Usage

### 1.Dependency Installation

You can use Maven to [download](https://repo1.maven.org/maven2/io/github/lxwise/fx-updater/) or install:

- Maven:

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>fx-updater</artifactId>
    <version>1.0.1</version>
</dependency>
```

- Gradle:

```Groovy
dependencies {
    implementation group: 'io.github.lxwise', name: 'fx-updater', version: '1.0.1'
}
```

### 2.Code Usage

1.Auto-load through the main program:

```java
FXUpdater updater = new FXUpdater(App.class);
updater.checkAppUpdate();
```

2.Load with custom configuration:

```java
FXUpdater updater = updater = new FXUpdater(new URL("http://localhost:8080/updater/updateConfig.json"), "1.0.0", 1, 1, new URL("http://localhost:8080/updater/themeCssUrl.css"));
updater.checkAppUpdate();
```

### 3.Configure Version Files

#### 3.1 Current Application Information File

Create an `app.properties` file in the resource directory of your application with the following configuration:

```properties
app.update.releaseId = 10000
app.update.licenseVersion = 1
app.update.version = 1.0.0
app.update.configUrl = http://192.168.12.50:81/downloads/app-update-config.json
```

- `app.update.releaseId`: Internal version number (must be numeric). Increment this value for each version to determine the presence of updates.
- `app.update.licenseVersion`: License version.
- `app.update.version`: Current application version.
- `app.update.configUrl`: URL of the update configuration file.

#### 3.2 Version Configuration File

Create an `app-update-config` file and host it on a downloadable server like Nginx or Tomcat. Here is an example configuration:

```json
{
    "name": "UpdateTest-FX",
    "licenses": "http://192.168.12.50:81/downloads/LICENSE",
    "changelog": "http://192.168.12.50:81/downloads/changelog.html",
	"Icon":null,
    "releases": [
      {
        "id": "20000",
        "version": "2.0.0",
        "releaseDate": "2024-03-07",
        "licenseVersion": "1",
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

**Parameter Description:**

- `name`: Application name.。
- `licenses`:  License, can be empty.
- `changelog`:  change log.
- `Icon`:  Application icon, can be empty. If empty, the default application icon will be used.
- `releases`: Release information.
    - `id` Version ID, used to determine whether there is an updated version.
    - `version` Current application version.
    - `releaseDate` Release date, must follow standard formatting.
    - `licenseVersion` Version of the license.
    - `installationFileInfo` Installation file information; a release version can contain installation files for multiple platforms.
        - `downloadLink`  Download link for the installation file.
        - `fileSize` File size in bytes.
        - `platform` Platform supported by the installation package: mac, win_x64, win_x86, linux, other.

#### 3.3 Update Log File

When updating versions, changes should be accompanied by an explanation. Create a new `changelog.html` file. For an elegant display, an HTML file is used.

`changelog.html` configuration:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Changelog</title>
</head>
<body>
    <h1>Changelog</h1>
    <h2>Version 2.0.0 (2024-03-07)</h2>
    <ul>
        <li>Added support for macOS platform.</li>
        <li>Improved performance and fixed minor issues.</li>
        <li>Updated user interface for better usability.</li>
    </ul>
    <h2>Version 1.0.0 (2024-01-07)</h2>
    <ul>
        <li>Released the initial version of the UpdateTest-FX application.</li>
        <li>Supported Windows platform.</li>
    </ul>
</body>
</html>

```



#### 3.4 License File

The license file here refers to common licenses such as `Apache 2.0`, `MIT`, `GPL`, etc.

The license template provided below is based on the MIT license. The filename is typically `LICENSE` or `LICENSE.txt`.

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

### Notes

1. **Filename**: `LICENSE` is the standard name for open-source projects but can also be `LICENSE.txt`.
2. **Copyright Year**: Replace with the current year (e.g., 2024).
3. **Copyright Holder**: Replace with your name or company name.
4. **License Type**: The above content is for an MIT license. If other licenses (e.g., Apache 2.0, GPL) are required, use the corresponding templates.

#### 3.5 Nginx File Server Configuration

Add a `server` module and create a global cache configuration in the `http` module.

```shell
	# Global cache area
	proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=download_cache:10m inactive=1d max_size=1g;	

	server {
    listen 81;
    server_name localhost;

    # Download directory
	location /downloads/ {
		root /var/www/app_updates/;
		autoindex on;
		add_header Cache-Control "public, max-age=31536000, immutable";
		add_header Last-Modified $date_gmt;
		add_header ETag $binary_remote_addr;
		expires 1h;
		if_modified_since exact;

		# Nginx cache
		proxy_cache download_cache;
		proxy_cache_valid 200 1h;
		proxy_cache_use_stale error timeout updating;
		add_header X-Cache-Status $upstream_cache_status;

		# Default Content-Disposition header
		add_header Content-Disposition "attachment";

		# Disable download behavior for changelog files
		location ~ /downloads/changelog(\.html|\.txt)?$ {
			add_header Content-Disposition "";  # Clear attachment
			add_header Content-Type "text/html"; # Or "text/plain" based on file type
		}
	}


  # Shortcut for the latest version
    location /latest/ {
        root /var/www/app_updates/;
        autoindex off;
        add_header Content-Disposition "attachment";
    }
}
```

## Custom UI

Create a `theme.css` file and place it in the resource directory at the same level as the `app-update-config` file. The program will automatically load the style file when creating the `FXUpdater`. This style file only applies to the current update program and will not affect your application.

Example:

```css
/* VBox styles */
.vbox {
  -fx-spacing: 20px;
  -fx-padding: 20px;
  -fx-background-color: -color-bg-default;
}

/* Label styles */
.label {
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
  -fx-text-fill: -color-fg-muted;
}

/* WebView styles */
.web-view {
  -fx-background-color: transparent;
}

/* HBox styles */
.hbox {
  -fx-alignment: center-right;
  -fx-spacing: 20px;
}

/* Button common styles */
.button {
  -fx-padding: 5px 20px;
  -fx-background-radius: 5px;
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
}

/* Ignore button styles */
.button-ignore {
  -fx-background-color: -color-accent-subtle;
  -fx-text-fill: -color-fg-muted;
}

/* Cancel button styles */
.button-cancel {
  -fx-background-color: -color-danger-emphasis;
  -fx-text-fill: white;
}

/* Confirm button styles */
.button-confirm {
  -fx-background-color: -color-success-emphasis;
  -fx-text-fill: white;
}

/* Scroll bar background */
.scroll-bar {
  -fx-background-color: transparent;
  -fx-padding: 0;
}

/* Scroll track styles */
.scroll-bar .track {
  -fx-background-color: #e0e0e0;
  -fx-background-radius: 4px;
}

/* Scroll thumb styles */
.scroll-bar .thumb {
  -fx-background-color: #e3e3e3;
  -fx-background-radius: 4px;
  -fx-padding: 2px;
}

/* Thumb hover effect */
.scroll-bar .thumb:hover {
  -fx-background-color: #e3e3e3;
}

/* Hide arrow buttons (optional) */
.scroll-bar .increment-button,
.scroll-bar .decrement-button {
  -fx-opacity: 0;
}

```

With the above configuration, congratulations! Your software now supports automatic updates.

Finally, I hope my project brings you help and value. If you have any suggestions or feedback, feel free to contact me. Let’s share knowledge and grow together!