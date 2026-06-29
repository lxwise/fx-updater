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

> **Version**: 2.0 | **Tech Stack**: Java 21 + JavaFX 23.0.1 + Gson 2.10 + Maven  
> **Maven Coordinates**: `io.github.lxwise:fx-updater:2.0.1`

[**中文文档**](https://github.com/lxwise/fx-updater/blob/master/README.md)

[**English DOC**](https://github.com/lxwise/fx-updater/blob/master/README.en.md)

## Table of Contents

- [Overview](#overview)
- [Project Address](#project-address)
- [Star](#star)
- [Principle](#principle)
- [Key Features](#key-features)
- [Quick Start](#quick-start)
- [Installation and Usage](#installation-and-usage)
  - [1. Dependency Installation](#1-dependency-installation)
  - [2. Code Usage](#2-code-usage)
  - [3. Configure Version Files](#3-configure-version-files)
- [Core API Reference](#core-api-reference)
  - [FXUpdater Class](#fxupdater-class)
  - [UpdateConfig Class (Builder Pattern)](#updateconfig-class-builder-pattern)
  - [UpdateMode Enum](#updatemode-enum)
- [Usage Examples](#usage-examples)
  - [One-Line Minimal Call (Recommended)](#one-line-minimal-call-recommended)
  - [Classic Construction](#classic-construction)
  - [Builder Advanced Configuration](#builder-advanced-configuration)
  - [Four Update Mode Examples](#four-update-mode-examples)
  - [Pure Code Configuration (No Config File)](#pure-code-configuration-no-config-file)
  - [Using build() to Get FXUpdater Instance](#using-build-to-get-fxupdater-instance)
- [Feature Details](#feature-details)
  - [Four Update Modes](#four-update-modes)
  - [Semantic Version Comparison](#semantic-version-comparison)
  - [Resume Download & Retry Mechanism](#resume-download--retry-mechanism)
  - [Cross-Platform Support](#cross-platform-support)
  - [Auto Restart](#auto-restart)
  - [Logging](#logging)
  - [Internationalization (i18n)](#internationalization-i18n)
- [Utility API](#utility-api)
- [Data Models](#data-models)
- [Custom UI](#custom-ui)
- [Backward Compatibility](#backward-compatibility)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [Project Structure](#project-structure)
- [FAQ](#faq)

---

## Overview

Are you struggling to update your application after development? Are you frustrated by the complexity of using various external update frameworks? Congratulations, FXUpdater was born to solve these problems for you. FXUpdater is a pure Java-based auto-update launcher for JavaFX applications. It is free of third-party frameworks and completely non-intrusive to your existing application. With just a few simple configurations, you can use it out of the box.

FXUpdater offers an all-in-one solution for updating and upgrading JavaFX-based applications packaged as standalone desktop programs. It provides a beautiful GUI for simple and clear operations and includes mechanisms for updates across all platforms (Windows, macOS, Linux). If you dislike the default style, you can easily customize the updater's appearance with a few lines of code.

## Project Address

**Gitee:** [https://gitee.com/lxwise/fx-updater](https://gitee.com/lxwise/fx-updater)

**Github:** [https://github.com/lxwise/fx-updater](https://github.com/lxwise/fx-updater)

## Star

P.S.: Although many users, like the author, enjoy free access to the source code and move on after downloading, we encourage you to give this project a **Star**. Your **Star** will help others discover this project and attract like-minded contributors to improve it. Please click the **Star** button and feel free to submit PRs to enhance the project together.

## Principle

When the application starts, it creates an `FXUpdater` instance and calls the `checkAppUpdate` method. The `checkAppUpdate` method runs in the background and searches for an `app.properties` file in the application's resource directory to read version configuration details and look for updates. If an update is found, the updater confirms its availability through a simple algorithm: if a higher version number exists, an update is available.

If an update is detected, a dialog will notify the user, showing the update log and options to ignore or install the update. If the user chooses to install, the updater automatically downloads the corresponding installer for the current platform and displays the download progress in the GUI. Once the download completes, the user can proceed with installation. The updater executes the installation command, and the application restarts after the update completes. No manual intervention is required during the installation process.

<img src="/doc/fxupdater_01.gif" alt="fxupdater_01" style="zoom: 50%;" />

## Key Features

| Feature | Description |
|---------|-------------|
| **Minimal Integration** | Only 2 config items + 1 line of code to enable auto-update |
| **Multiple Update Modes** | Interactive / Silent / Background Download / Check Only |
| **Semantic Versioning** | SemVer-based version comparison, no manual releaseId maintenance |
| **Resume Download** | Automatically resumes from breakpoint after interruption |
| **Failure Retry** | Auto-retry with incremental delay strategy |
| **Cross-Platform** | Windows (x64/x86), macOS, Linux full support |
| **Multiple Installer Formats** | exe, msi, dmg, pkg, deb, rpm, tar.gz, zip |
| **Internationalization** | Built-in 12 language support |
| **Theme Customization** | Customize update UI via CSS file |
| **Dual Format Config** | Supports both classic and simplified JSON formats |

---

## Quick Start

### Step 1: Add Maven Dependency

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>fx-updater</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Step 2: Create Configuration File

Create `app.properties` in your project's `src/main/resources/` directory:

```properties
# Only 2 required config items to enable auto-update
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/app-update-config.json
```

### Step 3: Enable Update with One Line of Code

```java
@Override
public void start(Stage primaryStage) {
    // ... initialize your application ...

    // One line to check for updates
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### Step 4: Deploy Server-Side Configuration File

Deploy `app-update-config.json` on your server (simplified format):

```json
{
  "name": "Your App Name",
  "version": "2.0.0",
  "changelog": "Version 2.0.0 updates:\n- New Feature A\n- Bug Fix B",
  "platforms": {
    "win_x64": { "url": "http://your-server.com/YourApp-2.0.0.exe", "size": 52428800 },
    "mac":     { "url": "http://your-server.com/YourApp-2.0.0.dmg", "size": 61865984 },
    "linux":   { "url": "http://your-server.com/YourApp-2.0.0.deb", "size": 48234496 }
  }
}
```

Done! When users run version 1.0.0 of the app, it will automatically detect the 2.0.0 update and show an update prompt.

---

## Installation and Usage

### 1. Dependency Installation

You can use Maven to [download](https://repo1.maven.org/maven2/io/github/lxwise/fx-updater/) or install:

- Maven:

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>fx-updater</artifactId>
    <version>2.0.1</version>
</dependency>
```

- Gradle:

```Groovy
dependencies {
    implementation group: 'io.github.lxwise', name: 'fx-updater', version: '2.0.1'
}
```

### 2. Code Usage

1. Auto-load through the main program:

```java
FXUpdater updater = new FXUpdater(App.class);
updater.checkAppUpdate();
```

2. Load with custom configuration:

```java
FXUpdater updater = new FXUpdater(
    "http://localhost:8080/updater/updateConfig.json",  // configUrl
    "1.0.0",                                           // version
    1,                                                  // releaseId
    1,                                                  // licenseVersion
    null                                                // themeCssUrl (no custom theme)
);
// Without callback and auto-close
updater.checkAppUpdate();
// With callback, without auto-close
updater.checkAppUpdate(() -> {
    System.out.println("Update dialog closed or completed");
});
// With callback and auto-close
updater.checkAppUpdate(() -> {
    System.out.println("Window closed, executing callback");
}, 5);
```

### 3. Configure Version Files

#### 3.1 Current Application Information File (app.properties)

Place this file in the `src/main/resources/` root directory. FXUpdater reads it automatically.

**Required Configuration (only 2 items):**

| Config Item | Type | Description | Example |
|-------------|------|-------------|---------|
| `app.update.version` | String | Current app version (SemVer format) | `1.0.0` |
| `app.update.configUrl` | String | URL of the server-side update config file | `http://server/update.json` |

**Optional Configuration (with smart defaults):**

| Config Item | Type | Default | Description |
|-------------|------|---------|-------------|
| `app.update.releaseId` | Integer | Auto-derived from version | Internal version numeric ID |
| `app.update.licenseVersion` | Integer | `1` | License version number |

**Complete Example:**

```properties
# === Required ===
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/app-update-config.json

# === Optional (usually not needed) ===
# app.update.releaseId = 1000000
# app.update.licenseVersion = 1
```

**releaseId Auto-Derivation Rule:**

When `app.update.releaseId` is not configured, it is automatically calculated via `VersionUtil.toReleaseId(version)`:

```
Formula: major × 1,000,000 + minor × 1,000 + patch

Examples:
  "1.0.0"   → 1,000,000
  "2.1.3"   → 2,001,003
  "1.5.20"  → 1,005,020
  "10.2.1"  → 10,002,001
```

#### 3.2 Version Configuration File (app-update-config.json)

Deploy this file on your server. FXUpdater downloads it via `configUrl` to determine if a new version is available. FXUpdater supports **two JSON formats** with automatic format detection.

**Format 1: Simplified Format (Recommended)**

Suitable for most single-version release scenarios, minimal configuration and easiest to maintain:

```json
{
  "name": "Your App Name",
  "version": "2.0.0",
  "changelog": "Version 2.0.0 updates:\n- New Feature A\n- Bug Fix B\n- Performance improvements",
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

**Simplified Format Field Description:**

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `name` | No | String | App name, displayed in update dialog title bar |
| `version` | **Yes** | String | New version number (SemVer format) |
| `changelog` | No | String | Changelog text (supports `\n` line breaks), or URL |
| `icon` | No | String | App icon URL |
| `downloadUrl` | No | String | Official download page URL (fallback for manual download) |
| `licenseVersion` | No | Integer | License version number, default `1` |
| `platforms` | **Yes** | Object | Platform installer info mapping |

**platforms Sub-Object Fields:**

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `url` | **Yes** | String | Installer download URL |
| `size` | No | Long | File size (bytes), used for progress bar |
| `sha256` | No | String | SHA-256 checksum for file integrity verification |

**Platform Identifiers (platforms keys):**

| Platform ID | Description | Installer Formats |
|-------------|-------------|-------------------|
| `win_x64` | Windows 64-bit | `.exe` `.msi` `.zip` |
| `win_x86` | Windows 32-bit | `.exe` `.msi` `.zip` |
| `mac` | macOS | `.dmg` `.pkg` |
| `linux` | Linux | `.deb` `.rpm` `.tar.gz` |
| `other` | Universal (matches any OS) | Any format |

**Format 2: Classic Format**

Suitable for complex scenarios requiring multiple versions (e.g., different license versions):

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

**Classic Format Parameter Description:**

- `name`: Application name.
- `licenses`: License, can be empty.
- `changelog`: Changelog.
- `icon`: Application icon, can be empty. If empty, the default icon will be used.
- `releases`: Release information.
    - `id` Version ID, used to determine if an updated version exists.
    - `version` Current application version.
    - `releaseDate` Release date, must follow standard formatting.
    - `licenseVersion` License version.
    - `officialDownloadAddress` Manual download / official website address. Used as fallback when auto-download fails.
    - `installationFileInfo` Installation file info; a release can contain installers for multiple platforms.
        - `downloadLink` Installer download URL.
        - `fileSize` File size in bytes.
        - `platform` Platform support: mac, win_x64, win_x86, linux, other.

**Auto-Detection Rules:**
- JSON contains `"releases"` array → Classic format
- JSON contains `"version"` + `"platforms"` → Simplified format
- Neither condition met → Throws `IllegalArgumentException`

#### 3.3 Update Log File

Create a `changelog.txt` file to document version changes:

```txt
Changelog

Version 2.0.0 (2024-03-07)
- Added support for macOS platform.
- Improved performance and fixed minor issues.
- Updated user interface for better usability.

Version 1.0.0 (2024-01-07)
- Released the initial version of the UpdateTest-FX application.
- Supported Windows platform.
```

#### 3.4 License File

The license file refers to common licenses such as `Apache 2.0`, `MIT`, `GPL`, etc. The template below is based on the MIT license. The filename is typically `LICENSE` or `LICENSE.txt`.

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

**Notes:**
1. **Filename**: `LICENSE` is the standard name for open-source projects but can also be `LICENSE.txt`.
2. **Copyright Year**: Replace with the current year (e.g., 2024).
3. **Copyright Holder**: Replace with your name or company name.
4. **License Type**: The above content is for an MIT license. If other licenses (e.g., Apache 2.0, GPL) are required, use the corresponding templates.

#### 3.5 Nginx File Server Configuration

Add a `server` module and create a global cache configuration in the `http` module:

```shell
	# Global cache area
	proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=download_cache:10m inactive=1d max_size=1g;	

	server {
    listen 81;
    server_name localhost;

    # Download directory
	location /downloads/ {
	#if use other directory please use alias /opt/myapp/app_updates/;
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
			add_header Content-Disposition "";
			add_header Content-Type "text/html";
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

---

## Core API Reference

### FXUpdater Class

**Package**: `com.lxwise.updater.service.FXUpdater`

FXUpdater is the core entry class of the update library, responsible for coordinating update checks, downloads, and installation.

#### Constructors

| Constructor | Description |
|-------------|-------------|
| `FXUpdater(Class<?> application)` | Auto-reads `/app.properties` and `/theme.css` to create instance |
| `FXUpdater(String configUrl, String version, Integer releaseId, Integer licenseVersion, String themeCssUrl)` | Manually specify all parameters |
| `FXUpdater(Properties properties, String cssUrl)` | Create from Properties object |
| `FXUpdater(UpdateConfig config)` | Create via UpdateConfig Builder (**package-private**, called internally by `UpdateConfig.build()`) |

#### Public Methods

| Method | Return | Description |
|--------|--------|-------------|
| `checkAppUpdate()` | void | Start update check, no callback, no auto-close |
| `checkAppUpdate(Runnable callback)` | void | Update check with callback |
| `checkAppUpdate(int autoCloseSeconds)` | void | Update check with auto-close timer |
| `checkAppUpdate(Runnable callback, int autoCloseSeconds)` | void | **Full version**: callback + auto-close |

#### Getter Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getUpdateConfigUrl()` | String | Get update config URL |
| `getVersion()` | String | Get current version |
| `getReleaseId()` | Integer | Get internal version ID |
| `getLicenseVersion()` | int | Get license version |
| `getThemeCssUrl()` | String | Get theme CSS URL |
| `getUpdateMode()` | UpdateMode | Get update mode |
| `isAutoRestart()` | boolean | Whether auto-restart is enabled |

---

### UpdateConfig Class (Builder Pattern)

**Package**: `com.lxwise.updater.service.UpdateConfig`

UpdateConfig provides a fluent API (Builder pattern) to simplify configuration. This is the recommended approach.

#### Static Factory Methods

| Method | Description |
|--------|-------------|
| `UpdateConfig.builder()` | Create a blank Builder, requires manual parameter setup |
| `UpdateConfig.fromApplication(Class<?>)` | Load from `/app.properties` and `/theme.css` |
| `UpdateConfig.fromProperties(Properties)` | Load from Properties object |

#### Builder Chain Methods

| Method | Param Type | Default | Description |
|--------|-----------|---------|-------------|
| `.configUrl(String)` | String | **Required** | Server update config URL |
| `.version(String)` | String | **Required** | Current app version |
| `.releaseId(Integer)` | Integer | Auto-derived | Internal version ID (optional) |
| `.licenseVersion(Integer)` | Integer | `1` | License version (optional) |
| `.themeCss(String)` | String | `null` | Theme CSS file URL |
| `.updateMode(UpdateMode)` | UpdateMode | `INTERACTIVE` | Update mode |
| `.autoCloseSeconds(int)` | int | `-1` (no close) | Dialog auto-close time (seconds) |
| `.autoRestart(boolean)` | boolean | `false` | Auto-restart after update |
| `.callback(Runnable)` | Runnable | `null` | Callback after update/close |
| `.connectTimeout(int)` | int | `15000` | HTTP connect timeout (ms) |
| `.readTimeout(int)` | int | `30000` | HTTP read timeout (ms) |
| `.maxRetries(int)` | int | `3` | Max download retry count |
| `.checksumVerification(boolean)` | boolean | `true` | Enable file checksum verification |

#### Terminal Methods

| Method | Return | Description |
|--------|--------|-------------|
| `.build()` | FXUpdater | Build FXUpdater instance (requires manual `checkAppUpdate` call) |
| `.checkUpdate()` | void | **All-in-one**: Build instance and immediately check for updates |

---

### UpdateMode Enum

**Package**: `com.lxwise.updater.service.UpdateMode`

| Value | Description | User Interaction |
|-------|-------------|------------------|
| `INTERACTIVE` | Interactive update (**default**): Shows dialog, user chooses whether to update | Dialog |
| `SILENT` | Silent update: Auto-downloads and installs in background, no UI | None |
| `BACKGROUND_DOWNLOAD` | Background download: Auto-downloads, shows install prompt when done | Prompt after download |
| `CHECK_ONLY` | Check only: Only checks for updates, notifies via callback | None |

---

## Usage Examples

### One-Line Minimal Call (Recommended)

```java
import com.lxwise.updater.service.UpdateConfig;

@Override
public void start(Stage primaryStage) {
    // ... initialize your application UI ...

    // One line to check for updates (auto-reads /app.properties and /theme.css)
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        // Config file read failure, can be ignored or logged
        e.printStackTrace();
    }
}
```

### Classic Construction

#### Method A: Auto-Load via Application Main Class

```java
import com.lxwise.updater.service.FXUpdater;

// Auto-reads /app.properties and /theme.css
FXUpdater updater = new FXUpdater(YourApp.class);

// No callback
updater.checkAppUpdate();

// With callback
updater.checkAppUpdate(() -> {
    System.out.println("Update check completed or dialog closed");
});

// With callback + auto-close (dialog auto-closes after 5 seconds)
updater.checkAppUpdate(() -> {
    System.out.println("Dialog closed");
}, 5);

// Auto-close only, no callback
updater.checkAppUpdate(10);
```

#### Method B: Manually Specify All Parameters

```java
FXUpdater updater = new FXUpdater(
    "http://your-server.com/update.json",  // configUrl
    "1.0.0",                                // version
    1000000,                                // releaseId
    1,                                      // licenseVersion
    null                                    // themeCssUrl (no custom theme)
);
updater.checkAppUpdate();
```

### Builder Advanced Configuration

```java
import com.lxwise.updater.service.UpdateConfig;
import com.lxwise.updater.service.UpdateMode;

// Load from config file + custom options
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)    // Interactive
    .autoCloseSeconds(8)                   // 8-second auto-close
    .autoRestart(true)                     // Auto-restart after install
    .connectTimeout(10000)                 // Connect timeout 10s
    .readTimeout(60000)                    // Read timeout 60s
    .maxRetries(5)                         // Max 5 retries
    .callback(() -> {
        System.out.println("Update process completed");
    })
    .checkUpdate();
```

### Four Update Mode Examples

#### Interactive Update (Default)

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)
    .checkUpdate();
```

Shows an update dialog with version info and changelog. User can choose "Update Now", "Remind Later", or "Skip This Version".

#### Silent Update

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.SILENT)
    .autoRestart(true)
    .maxRetries(5)
    .callback(() -> System.out.println("Silent update completed"))
    .checkUpdate();
```

Auto-downloads and installs in background with no UI. Suitable for enterprise mandatory updates.

#### Background Download

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.BACKGROUND_DOWNLOAD)
    .callback(() -> System.out.println("Background download completed"))
    .checkUpdate();
```

Silent background download, shows install prompt when done. Falls back to interactive mode on failure.

#### Check Only

```java
UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.CHECK_ONLY)
    .callback(() -> {
        // Triggered when an update is available
        System.out.println("New version detected!");
        // Custom notification logic here
    })
    .checkUpdate();
```

Only checks for new versions without downloading or installing. Notifies via callback.

### Pure Code Configuration (No Config File)

```java
// Fully code-based configuration, no app.properties dependency
UpdateConfig.builder()
    .configUrl("http://your-server.com/update.json")
    .version("1.0.0")
    // releaseId optional, auto-derived from version as 1000000
    // licenseVersion optional, defaults to 1
    .updateMode(UpdateMode.INTERACTIVE)
    .connectTimeout(15000)
    .readTimeout(30000)
    .maxRetries(3)
    .callback(() -> System.out.println("Update completed"))
    .checkUpdate();
```

### Using build() to Get FXUpdater Instance

```java
// Get FXUpdater instance for more flexible control
FXUpdater updater = UpdateConfig.fromApplication(YourApp.class)
    .updateMode(UpdateMode.INTERACTIVE)
    .autoRestart(true)
    .build();

// Can be called multiple times later
updater.checkAppUpdate();
updater.checkAppUpdate(() -> { /* callback */ });
updater.checkAppUpdate(() -> { /* callback */ }, 10);
```

---

## Feature Details

### Four Update Modes

```
┌──────────────────────────────────────────────────────────────┐
│                      checkAppUpdate()                         │
│                           │                                   │
│                ┌──────────┼──────────┐                       │
│                ▼          ▼          ▼          ▼             │
│           INTERACTIVE   SILENT  BACKGROUND  CHECK_ONLY       │
│                │          │      _DOWNLOAD      │             │
│           Show update  Background  Background   Check for     │
│           dialog       download    download     updates       │
│                │        +install    then prompt   │            │
│           User decides   No UI     after done  Callback       │
│           whether to      │          │          notification  │
│           update          │          │            │            │
│                │          │          │            │            │
│           Download     Download   User confirms  End          │
│           +Install     +Install   Install                     │
└──────────────────────────────────────────────────────────────┘
```

| Mode | Download | Installation | UI | Use Case |
|------|----------|-------------|-----|----------|
| INTERACTIVE | After user confirms | Shows progress bar | Full UI | Desktop apps |
| SILENT | Auto background | Auto silent install | None | Enterprise forced updates |
| BACKGROUND_DOWNLOAD | Auto background | Prompt after download | Partial UI | Non-intrusive experience |
| CHECK_ONLY | No download | No install | None | Custom update UI |

### Semantic Version Comparison

FXUpdater 2.0 introduces the `VersionUtil` utility class supporting standard SemVer semantic version comparison, **eliminating the dependency on releaseId numeric values**.

**Supported Version Formats:**

```
1.0.0          Standard three-segment
2.1            Two-segment (patch defaults to 0)
3              Single-segment (minor and patch default to 0)
v1.0.0         With v prefix
V2.1.3         With V prefix
1.0.0-beta1    With pre-release tag
1.0.0-alpha    With pre-release tag
1.0.0-rc.1     With pre-release tag
```

**Comparison Rules:**

1. Compare `major.minor.patch` segments numerically
2. When numeric parts are equal, compare pre-release tags:
   - With pre-release tag < without pre-release tag (`1.0.0-beta` < `1.0.0`)
   - Both with pre-release tags: compare lexicographically (`alpha` < `beta`)
3. When semantic comparison is not possible, falls back to `releaseId` numeric comparison

### Resume Download & Retry Mechanism

#### Resume Download

If download is interrupted (network disconnection, app closure, etc.), the next download automatically detects the existing temp file:

1. If downloaded file size matches target → Use directly, skip download
2. If partially downloaded and server supports `Accept-Ranges: bytes` → Resume from breakpoint
3. Otherwise → Delete old file, start fresh

#### Retry Mechanism

Auto-retries on download failure with incremental delay:

```
1st retry: Wait 2 seconds
2nd retry: Wait 4 seconds
3rd retry: Wait 6 seconds
...
```

Configure max retries via `maxRetries()` (default: 3).

### Cross-Platform Support

FXUpdater auto-detects the runtime platform and matches the corresponding installer:

| OS | Platform ID | Supported Formats |
|----|-------------|-------------------|
| Windows 64-bit | `win_x64` | `.exe` `.msi` `.zip` |
| Windows 32-bit | `win_x86` | `.exe` `.msi` `.zip` |
| macOS | `mac` | `.dmg` `.pkg` |
| Linux | `linux` | `.deb` `.rpm` `.tar.gz` `.zip` |

Special platform ID `other` matches any operating system.

#### Installation Behavior by Format

| Format | Installation Method | Silent Mode Support |
|--------|-------------------|---------------------|
| `.exe` | `cmd /c start` or `/S` silent flag | Yes |
| `.msi` | `msiexec /i` or `/quiet` flag | Yes |
| `.dmg` | Built-in shell script mount install | No |
| `.pkg` | `sudo installer -pkg` | No |
| `.deb` | `sudo dpkg -i` | No |
| `.rpm` | `sudo rpm -i` | No |
| `.tar.gz` | `tar -xzf` extract to `user.home` | No |
| `.zip` | PowerShell `Expand-Archive` / `unzip` | No |

### Auto Restart

Cross-platform application restart via `AppRestartUtil.restartApplication()`:

```java
UpdateConfig.fromApplication(YourApp.class)
    .autoRestart(true)  // Enable auto-restart
    .checkUpdate();
```

Restart mechanism:
1. Gets current JVM `java.home` path
2. Collects JVM launch arguments (via `RuntimeMXBean`)
3. Detects launch mode (jar or class)
4. Builds new process command and launches
5. On Windows, prefers `javaw.exe` (no console window)

Manual restart:

```java
import com.lxwise.updater.utils.AppRestartUtil;

AppRestartUtil.restartApplication(YourApp.class);
```

### Logging

FXUpdater uses `UpdateLogger` for unified log management:

```java
import com.lxwise.updater.utils.UpdateLogger;
import java.util.logging.*;

// Logs auto-initialize; manual configuration also available

// Set log level
UpdateLogger.setLevel(Level.FINE);  // Enable DEBUG level

// Add file log handler
FileHandler fileHandler = new FileHandler("fxupdater.log", true);
fileHandler.setFormatter(new SimpleFormatter());
UpdateLogger.addHandler(fileHandler);
```

**Log Output Format:**

```
[2025-01-15 14:30:25.123] [INFO] [FX-Updater] Starting update check (mode: INTERACTIVE, configUrl: http://...)
[2025-01-15 14:30:26.456] [INFO] [FX-Updater] Fetching update config from: http://...
[2025-01-15 14:30:27.789] [INFO] [FX-Updater] Parsed simplified format config: My App (version: 2.0.0)
[2025-01-15 14:30:28.012] [INFO] [FX-Updater] Update found: version 2.0.0 (id: 2000000)
```

**Log Levels:**

| Method | java.util.logging Level | Description |
|--------|------------------------|-------------|
| `UpdateLogger.debug()` | `FINE` | Debug information |
| `UpdateLogger.info()` | `INFO` | General information |
| `UpdateLogger.warn()` | `WARNING` | Warning messages |
| `UpdateLogger.error()` | `SEVERE` | Error messages (supports Throwable) |

### Internationalization (i18n)

FXUpdater includes built-in support for 12 languages, automatically selected based on system Locale:

| Language | Filename |
|----------|----------|
| English (default) | `updater.properties` |
| Simplified Chinese | `updater_zh_CN.properties` |
| Traditional Chinese | `updater_zh_TW.properties` |
| Japanese | `updater_ja.properties` |
| Korean | `updater_ko.properties` |
| French | `updater_fr.properties` |
| German | `updater_de.properties` |
| Spanish | `updater_es.properties` |
| Italian | `updater_it.properties` |
| Portuguese | `updater_pt.properties` |
| Russian | `updater_ru.properties` |
| Arabic | `updater_ar.properties` |

---

## Utility API

### VersionUtil Version Comparison Utility

**Package**: `com.lxwise.updater.utils.VersionUtil`

| Method | Return | Description |
|--------|--------|-------------|
| `compare(String v1, String v2)` | int | Compare two versions: positive=v1 larger, negative=v2 larger, 0=equal |
| `isNewer(String currentVersion, String newVersion)` | boolean | Check if newVersion is newer than currentVersion |
| `toReleaseId(String version)` | int | Generate numeric ID from version (major×10^6 + minor×10^3 + patch) |

```java
// Examples
VersionUtil.compare("2.0.0", "1.5.3");        // > 0
VersionUtil.compare("1.0.0-beta", "1.0.0");   // < 0
VersionUtil.compare("v1.0.0", "1.0.0");       // = 0

VersionUtil.isNewer("1.0.0", "2.0.0");        // true
VersionUtil.isNewer("2.0.0", "1.0.0");        // false

VersionUtil.toReleaseId("2.1.3");             // 2001003
VersionUtil.toReleaseId("1.0.0");             // 1000000
```

### HttpUtils HTTP Utility

**Package**: `com.lxwise.updater.utils.HttpUtils`

| Method | Description |
|--------|-------------|
| `openConnection(URL url)` | Open connection with default timeouts |
| `openConnection(URL url, int connectTimeout, int readTimeout)` | Open connection with custom timeouts |
| `openRangeConnection(URL url, long startByte, int connectTimeout, int readTimeout)` | Open connection supporting resume |
| `supportsResume(URLConnection connection)` | Check if server supports resume |
| `executeWithRetry(RetryAction<T> action, int maxRetries)` | Execute with retry |
| `executeWithRetry(RetryAction<T> action)` | Execute with default retries (3) |
| `calculateSHA256(InputStream inputStream)` | Calculate SHA-256 checksum |
| `verifyChecksum(String expected, String actual)` | Verify checksum match |
| `disconnect(URLConnection connection)` | Safely close connection |

**RetryAction Functional Interface:**

```java
@FunctionalInterface
public interface RetryAction<T> {
    T execute(int attempt) throws Exception;
}
```

```java
// Usage example
String result = HttpUtils.executeWithRetry(attempt -> {
    System.out.println("Attempt " + attempt);
    URLConnection conn = HttpUtils.openConnection(new URL("http://example.com/data"));
    // ... process connection ...
    return "success";
}, 5); // Max 5 retries
```

### UpdateLogger Logging Utility

**Package**: `com.lxwise.updater.utils.UpdateLogger`

| Method | Description |
|--------|-------------|
| `init()` | Initialize logging system (auto-initialized on first call) |
| `addHandler(Handler handler)` | Add custom log handler (e.g., FileHandler) |
| `setLevel(Level level)` | Set global log level |
| `debug(String message)` | Output DEBUG log |
| `debug(String format, Object... args)` | Output formatted DEBUG log |
| `info(String message)` | Output INFO log |
| `info(String format, Object... args)` | Output formatted INFO log |
| `warn(String message)` | Output WARNING log |
| `warn(String format, Object... args)` | Output formatted WARNING log |
| `error(String message)` | Output ERROR log |
| `error(String message, Throwable throwable)` | Output ERROR log with exception stack trace |
| `error(String format, Object... args)` | Output formatted ERROR log |

### AppRestartUtil Application Restart Utility

**Package**: `com.lxwise.updater.utils.AppRestartUtil`

| Method | Description |
|--------|-------------|
| `restartApplication(Class<?> appMainClass)` | Restart current Java app, auto-detects jar/class launch mode |
| `getCurrentPID()` | Get current process PID (Java 9+ ProcessHandle or RuntimeMXBean fallback) |

### GuiUtils GUI Utility

**Package**: `com.lxwise.updater.gui.GuiUtils`

| Method | Description |
|--------|-------------|
| `setStageIcon(ReleaseInfoModel release, Stage stage)` | Set window icon (auto-fallback to default icon) |
| `setupCloseConfirmation(Stage stage, ResourceBundle i18nBundle, Runnable onConfirmClose)` | Set up window close confirmation dialog |
| `buildStageTitle(ReleaseInfoModel release, ResourceBundle i18nBundle)` | Build window title |
| `formatFileSize(double fileSizeInBytes)` | Format bytes to human-readable format (e.g., `15.3 MB`) |

---

## Data Models

### AppInfoModel

Application information model, corresponding to the top-level structure of the server JSON config.

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Application name |
| `changelog` | String | Changelog (text or URL) |
| `licenses` | String | License information |
| `icon` | String | App icon URL |
| `releases` | List\<ReleaseInfoModel\> | Release version list |

### ReleaseInfoModel

Release version information model.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Version numeric ID |
| `version` | String | Version string |
| `licenseVersion` | Integer | License version |
| `releaseDate` | Date | Release date |
| `officialDownloadAddress` | String | Official download/website URL |
| `installationFileInfo` | List\<InstallationFileInfoModel\> | Installer info list |
| `appInfo` | AppInfoModel | Back-reference to parent app info |

### InstallationFileInfoModel

Installation file information model.

| Field | Type | Description |
|-------|------|-------------|
| `downloadLink` | String | Installer download URL |
| `fileSize` | Long | File size (bytes) |
| `platform` | String | Platform ID (`win_x64`/`mac`/`linux` etc.) |
| `checksum` | String | SHA-256 checksum |

### EPlatformModel Enum

| Value | Platform String | Description |
|-------|----------------|-------------|
| `win_x64` | `"win_x64"` | Windows 64-bit |
| `win_x86` | `"win_x86"` | Windows 32-bit |
| `mac` | `"mac"` | macOS |
| `linux` | `"linux"` | Linux |
| `other` | `"other"` | Universal platform |

---

## Custom UI

Create a `theme.css` file and place it in the resource directory at the same level as the `app-update-config` file. The program will automatically load the style file when creating the `FXUpdater`. This style file only applies to the updater and will not affect your application.

#### Customizable CSS Selectors

```css
/* FX-Updater Theme Styles */

/* Main container layout */
.vbox {
  -fx-spacing: 20px;
  -fx-padding: 20px;
  -fx-background-color: -color-bg-default;  /* Can be replaced with #ffffff etc. */
}

/* Text labels */
.label {
  -fx-font-family: "Inter";
  -fx-font-size: 13px;
  -fx-text-fill: -color-fg-muted;
}

/* Button container */
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

/* Skip this version button */
.button-ignore {
  -fx-background-color: -color-accent-subtle;
  -fx-text-fill: -color-fg-muted;
}

/* Cancel / Remind later button */
.button-cancel {
  -fx-background-color: -color-danger-emphasis;
  -fx-text-fill: white;
}

/* Update now button */
.button-confirm {
  -fx-background-color: -color-success-emphasis;
  -fx-text-fill: white;
}

/* Scroll bar styles */
.scroll-bar { -fx-background-color: transparent; }
.scroll-bar .track { -fx-background-color: #e0e0e0; -fx-background-radius: 4px; }
.scroll-bar .thumb { -fx-background-color: #c0c0c0; -fx-background-radius: 4px; }
.scroll-bar .thumb:hover { -fx-background-color: #a0a0a0; }
.scroll-bar .increment-button,
.scroll-bar .decrement-button { -fx-opacity: 0; }
```

> **Tip**: If you don't need custom theming, you can skip creating `theme.css` — FXUpdater will use JavaFX default styles.

With the above configuration, congratulations! Your software now supports automatic updates.

---

## Backward Compatibility

FXUpdater 2.0 maintains strict backward compatibility during the refactoring process:

### Preserved Legacy API

| Legacy API | Status | Description |
|------------|--------|-------------|
| `new FXUpdater(Class<?>)` | Fully compatible | Auto-reads `/app.properties` |
| `new FXUpdater(String, String, Integer, Integer, String)` | Fully compatible | 5-parameter constructor |
| `new FXUpdater(Properties, String)` | Fully compatible | Properties constructor |
| `checkAppUpdate()` | Fully compatible | No-arg version |
| `checkAppUpdate(Runnable)` | Fully compatible | Callback version |
| `checkAppUpdate(int)` | Fully compatible | Auto-close version |
| `checkAppUpdate(Runnable, int)` | Fully compatible | Full version |

### Configuration File Compatibility

| Config Format | Status | Description |
|---------------|--------|-------------|
| 4-item `app.properties` (with releaseId, licenseVersion) | Fully compatible | Optional items read if present |
| 2-item `app.properties` (version, configUrl only) | Newly supported | releaseId auto-derived |
| Classic JSON format (with releases array) | Fully compatible | Auto-detected |
| Simplified JSON format (version + platforms) | Newly supported | Auto-detected |

### Upgrade Guide

**Upgrading from 1.x to 2.0:**

1. **No code changes required** — All legacy constructors and method signatures are preserved
2. **No config file changes required** — Legacy 4-item config and classic JSON format continue to work
3. **Optional config simplification** — You can remove `releaseId` and `licenseVersion` from `app.properties`
4. **Optional new API adoption** — Recommended: `UpdateConfig.fromApplication().checkUpdate()`

---

## Error Handling

### Exception Types

| Exception | Thrown At | Cause | Solution |
|-----------|-----------|-------|----------|
| `IOException` | `FXUpdater(Class<?>)`, `UpdateConfig.fromApplication()` | `/app.properties` not found or read failure | Check config file is in classpath root |
| `IllegalStateException` | `UpdateConfig.build()` | `configUrl` or `version` not set | Ensure required params are set |
| `IllegalArgumentException` | `AcquireUpdateConfigService` | Invalid JSON format (missing required fields) | Check server JSON config |
| `NoUpdateException` | `CheckUpdateService` | Already latest version, no update available | Normal, no handling needed |
| `IllegalArgumentException` | `InstallFileDownloadService` | No installer for current platform | Add current platform installer to JSON config |
| `IllegalArgumentException` | `ExecuteInstallerService` | Unsupported installer format | Use supported formats (exe/msi/dmg/pkg/deb/rpm/tar.gz/zip) |

### Error Handling Best Practices

```java
// Method 1: Use try-catch to catch config errors
try {
    UpdateConfig.fromApplication(YourApp.class)
        .callback(() -> {
            // Callback is always called, even on failure or no update
            System.out.println("Update process completed");
        })
        .checkUpdate();
} catch (IOException e) {
    // Config file read failure, log but don't affect app operation
    System.err.println("Update check skipped: " + e.getMessage());
}

// Method 2: Callback handling for update check failures
// FXUpdater handles all exceptions internally, notifying via callback
// Even if network is unavailable or server is down, callback is safely called
```

### Internal Exception Handling Mechanism

FXUpdater includes comprehensive exception handling for all async operations:

- **Config fetch failure** → Calls callback, logs error
- **Version check failure / no update** → Calls callback, logs info
- **Download failure** → Auto-retries N times, calls callback after all fail
- **Install failure** → SILENT mode calls callback; Interactive mode shows error in UI
- **Background download failure** → Auto-falls back to interactive mode for manual retry

---

## Best Practices

### 1. Recommended Call Timing

```java
@Override
public void start(Stage primaryStage) {
    // Show main application UI first
    primaryStage.show();

    // Check for updates asynchronously after app launch (non-blocking)
    try {
        UpdateConfig.fromApplication(YourApp.class).checkUpdate();
    } catch (IOException e) {
        // Update check failure should not affect normal app usage
    }
}
```

### 2. Recommended Configuration

**app.properties (client):**
```properties
# Only 2 required items
app.update.version = 1.0.0
app.update.configUrl = http://your-server.com/update.json
```

**app-update-config.json (server, simplified format):**
```json
{
  "name": "Your App",
  "version": "2.0.0",
  "changelog": "- New Feature A\n- Bug Fix B",
  "platforms": {
    "win_x64": { "url": "http://server/App-2.0.0.exe", "size": 52428800 },
    "mac":     { "url": "http://server/App-2.0.0.dmg", "size": 61865984 }
  }
}
```

### 3. Version Number Management

- Use standard SemVer format: `MAJOR.MINOR.PATCH`
- When releasing new versions, only update `version` in `app.properties` and server JSON
- No need to manually maintain `releaseId` — auto-derived by VersionUtil
- Update is triggered when client version < server version

### 4. Server Deployment Recommendations

- Deploy `app-update-config.json` on CDN or static file server
- Update the `version` field and corresponding platform `url` to release new versions
- Provide `size` field for accurate download progress
- Provide `sha256` field for download integrity verification
- Provide `downloadUrl` as fallback for manual download

### 5. Logging Configuration

```java
// Production: Only WARNING and above
UpdateLogger.setLevel(Level.WARNING);

// Development/Debug: Enable all logs
UpdateLogger.setLevel(Level.ALL);

// Output to file
try {
    FileHandler fh = new FileHandler("fxupdater_%g.log", 10 * 1024 * 1024, 3, true);
    fh.setFormatter(new SimpleFormatter());
    UpdateLogger.addHandler(fh);
} catch (IOException e) {
    e.printStackTrace();
}
```

### 6. Recommended Modes by Scenario

| Scenario | Recommended Mode | Config Suggestion |
|----------|-----------------|-------------------|
| Desktop app | `INTERACTIVE` | Defaults |
| Enterprise tool | `SILENT` | `autoRestart(true)` |
| Game / Large app | `BACKGROUND_DOWNLOAD` | `maxRetries(5)` |
| Custom UI | `CHECK_ONLY` | Implement custom logic in callback |
| Quick check at startup | `INTERACTIVE` | `autoCloseSeconds(10)` |

---

## Project Structure

```
fx-updater/
├── src/main/java/com/lxwise/updater/
│   ├── gui/                                # GUI Layer
│   │   ├── UpdaterDialogController.java    # Update dialog controller
│   │   ├── UpdaterProgressController.java  # Download progress controller
│   │   └── GuiUtils.java                  # GUI utilities
│   ├── model/                              # Data Models
│   │   ├── AppInfoModel.java              # App information
│   │   ├── ReleaseInfoModel.java          # Release information
│   │   ├── InstallationFileInfoModel.java # Installer information
│   │   └── EPlatformModel.java            # Platform enum
│   ├── service/                            # Service Layer (Core Logic)
│   │   ├── FXUpdater.java                 # ★ Updater entry point
│   │   ├── UpdateConfig.java              # ★ Builder configurator
│   │   ├── UpdateMode.java                # Update mode enum
│   │   ├── AcquireUpdateConfigService.java # Fetch remote config
│   │   ├── CheckUpdateService.java        # Version comparison
│   │   ├── InstallFileDownloadService.java # File download
│   │   └── ExecuteInstallerService.java   # Execute installation
│   └── utils/                              # Utilities
│       ├── VersionUtil.java               # Semantic version comparison
│       ├── HttpUtils.java                 # HTTP connection utility
│       ├── UpdateLogger.java              # Unified logging
│       ├── AppRestartUtil.java            # App restart
│       ├── InstallationScriptUtil.java    # Installation scripts
│       └── NoUpdateException.java         # No-update exception
├── src/main/resources/
│   ├── com/lxwise/updater/
│   │   ├── gui/
│   │   │   ├── UpdaterDialog.fxml         # Update dialog layout
│   │   │   └── UpdaterProgress.fxml       # Progress window layout
│   │   ├── i18n/
│   │   │   ├── updater.properties         # English (default)
│   │   │   ├── updater_zh_CN.properties   # Simplified Chinese
│   │   │   └── ... (12 languages)
│   │   └── utils/
│   │       └── installdmg.sh              # macOS DMG install script
│   └── images/
│       └── fx-updater-logo.png            # Default icon
└── src/test/                               # Test directory
    ├── java/com/lxwise/updater/
    │   ├── SimpleUpdateDemo.java           # Demo1: Classic usage
    │   ├── AdvancedUpdateDemo.java         # Demo2: Builder advanced
    │   ├── SimpleUpdateStart.java          # Demo1 entry point
    │   └── AdvancedUpdateStart.java        # Demo2 entry point
    └── resources/
        ├── app.properties                  # Test config
        ├── app-update-config.json          # Test JSON
        └── theme.css                       # Test theme
```

---

## FAQ

### Q1: What's the minimum number of files needed to enable auto-update?

**Client**: 1 file `app.properties` (2 lines of config) + 1 line of Java code  
**Server**: 1 JSON file + installer files

### Q2: Do I still need to manually maintain releaseId?

No. Since version 2.0 introduced semantic version comparison, `releaseId` can be auto-derived from the version number (`toReleaseId("2.1.3")` → `2001003`). It's no longer needed in config files.

### Q3: How to support both Windows and macOS?

Simply provide installers for both platforms in the JSON config:

```json
"platforms": {
  "win_x64": { "url": "http://.../App.exe", "size": 52428800 },
  "mac":     { "url": "http://.../App.dmg", "size": 61865984 }
}
```

FXUpdater will auto-detect the runtime platform and download the corresponding installer.

### Q4: How to customize the update UI?

Two approaches:
1. **CSS Theme**: Customize styles in `src/main/resources/theme.css`
2. **CHECK_ONLY Mode**: Use check-only mode and implement fully custom update UI in the callback

### Q5: Will interrupted downloads lose downloaded content?

No. FXUpdater supports resume downloads. If the server supports `Accept-Ranges`, it will automatically resume from the breakpoint.

### Q6: Will update check failures affect normal app operation?

No. All FXUpdater network operations are asynchronous with comprehensive internal exception handling. Even if the network is unavailable, server is down, or config is incorrect, normal app operation is unaffected.

### Q7: How to use without app.properties?

Use the pure Builder approach:

```java
UpdateConfig.builder()
    .configUrl("http://your-server.com/update.json")
    .version("1.0.0")
    .checkUpdate();
```

### Q8: What formats does changelog support?

The `changelog` field supports two approaches:
1. **Text content**: Write changelog text directly, supports `\n` line breaks
2. **URL address**: Provide a URL, FXUpdater will asynchronously load and display the content

### Q9: How to handle multiple license versions?

Use classic JSON format, define multiple versions in the `releases` array with different `licenseVersion`:

```json
{
  "releases": [
    { "version": "2.0.0", "licenseVersion": 1, "installationFileInfo": [...] },
    { "version": "3.0.0", "licenseVersion": 2, "installationFileInfo": [...] }
  ]
}
```

FXUpdater will auto-match the latest version based on the client's `licenseVersion`.

### Q10: Does version comparison support pre-release tags?

Yes. `1.0.0-beta` < `1.0.0-rc` < `1.0.0`. Pre-release versions are always lower than the same version's release version.

---

Finally, I hope my project brings you help and value. If you have any suggestions or feedback, feel free to contact me. Let's share knowledge and grow together!