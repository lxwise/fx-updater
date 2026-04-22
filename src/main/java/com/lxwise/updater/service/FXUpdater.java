package com.lxwise.updater.service;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import com.lxwise.updater.gui.UpdaterDialogController;
import com.lxwise.updater.gui.UpdaterProgressController;
import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.UpdateLogger;
import com.lxwise.updater.utils.VersionUtil;

/**
 * @author lxwise
 * @create 2024-05
 * @description: updater更新器程序，支持多种更新模式（交互/静默/后台下载/仅检查）
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class FXUpdater {

    /**
     * 更新配置文件地址（字符串形式）
     */
    private final String updateConfigUrl;
    /**
     * 版本号
     */
    private final String version;
    /**
     * 发布版本ID
     */
    private final Integer releaseId;
    /**
     * 版本对应的licence版本号
     */
    private final Integer licenseVersion;
    /**
     * 主题样式文件地址（字符串形式）
     */
    private final String themeCssUrl;

    // ===== 增强配置项（带默认值，向后兼容） =====
    private UpdateMode updateMode = UpdateMode.INTERACTIVE;
    private boolean autoRestart = false;
    private int connectTimeout = 15000;
    private int readTimeout = 30000;
    private int maxRetries = 3;

    /**
     * 创建并初始化FXUpdater类的实例。
     * @param application 应用程序的主类,通过主类读取资源文件下的app-version-info.properties文件
     *                    application.getResourceAsStream("/app-version-info.properties")
     * @throws IOException 读取资源文件失败
     */
    public FXUpdater(Class<?> application) throws IOException {
        this(loadProperties(application), loadThemeCss(application));
    }

    /**
     * 创建并初始化FXUpdater类的实例。
     * @param updateConfigUrl 更新配置文件地址
     * @param version 版本号
     * @param releaseId 发布版本ID
     * @param licenseVersion 版本对应的licence版本号
     * @param themeCssUrl 主题样式文件地址
     */
    public FXUpdater(String updateConfigUrl, String version, Integer releaseId, Integer licenseVersion, String themeCssUrl) {
        this.updateConfigUrl = updateConfigUrl;
        this.releaseId = releaseId;
        this.version = version;
        this.licenseVersion = licenseVersion;
        this.themeCssUrl = themeCssUrl;
        UpdateLogger.init();
    }

    /**
     * 创建并初始化FXUpdater类的实例。
     * @param properties 资源文件下的配置文件（releaseId和licenseVersion可选）
     * @param cssUrl 主题样式文件地址
     */
    public FXUpdater(Properties properties, String cssUrl) {
        this(
                properties.getProperty("app.update.configUrl"),
                properties.getProperty("app.update.version"),
                resolveReleaseId(properties),
                resolveLicenseVersion(properties),
                cssUrl
        );
    }

    /**
     * 通过 UpdateConfig 构建器创建实例（推荐的新方式）
     * @param config 更新配置
     */
    FXUpdater(UpdateConfig config) {
        this(config.getUpdateConfigUrl(), config.getVersion(), config.getReleaseId(),
                config.getLicenseVersion(), config.getThemeCssUrl());
        this.updateMode = config.getUpdateMode();
        this.autoRestart = config.isAutoRestart();
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.maxRetries = config.getMaxRetries();
    }


    /**
     * 读取资源文件下的配置文件
     * @param application
     * @return
     * @throws IOException
     */
    private static Properties loadProperties(Class<?> application) throws IOException {
        InputStream is = application.getResourceAsStream("/app.properties");
        if (is == null) {
            throw new IOException("Resource /app.properties not found in classpath");
        }
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    /**
     * 读取资源文件下的主题样式文件
     * @param applicationMain
     * @return
     */
    private static String loadThemeCss(Class<?> applicationMain) {
        URL url = applicationMain.getResource("/theme.css");
        return url != null ? url.toExternalForm() : null;
    }

    /**
     * 从Properties中解析releaseId，不存在则从version自动推导
     */
    private static Integer resolveReleaseId(Properties properties) {
        String releaseIdStr = properties.getProperty("app.update.releaseId");
        if (releaseIdStr != null && !releaseIdStr.isBlank()) {
            return Integer.valueOf(releaseIdStr.trim());
        }
        String version = properties.getProperty("app.update.version");
        return VersionUtil.toReleaseId(version);
    }

    /**
     * 从Properties中解析licenseVersion，不存在则默认1
     */
    private static Integer resolveLicenseVersion(Properties properties) {
        String licenseVersionStr = properties.getProperty("app.update.licenseVersion");
        if (licenseVersionStr != null && !licenseVersionStr.isBlank()) {
            return Integer.valueOf(licenseVersionStr.trim());
        }
        return 1;
    }

    public String getUpdateConfigUrl() {
        return updateConfigUrl;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public String getVersion() {
        return version;
    }

    public int getLicenseVersion() {
        return licenseVersion;
    }

    public String getThemeCssUrl() {
        return themeCssUrl;
    }

    public UpdateMode getUpdateMode() {
        return updateMode;
    }

    public boolean isAutoRestart() {
        return autoRestart;
    }

    /**
     * 开始检查更新并提示用户安装任务。
     * 默认不带回调和定时关闭
     */
    public void checkAppUpdate() {
        checkAppUpdate(null, -1);
    }

    /**
     * 带回调的开始检查更新并提示用户安装任务。
     * @param callback 带回调
     */
    public void checkAppUpdate(Runnable callback) {
        checkAppUpdate(callback, -1);
    }
    /**
     * 带回调的开始检查更新并提示用户安装任务。
     * @param autoCloseSeconds 不带回调,带自动关闭
     */
    public void checkAppUpdate(int autoCloseSeconds) {
        checkAppUpdate(null, autoCloseSeconds);
    }

    /**
     * 支持回调 + 自动关闭时间的方法（单位：秒，<=0 表示不自动关闭）
     * @param callback
     * @param autoCloseSeconds
     */
    public void checkAppUpdate(Runnable callback, int autoCloseSeconds) {
        UpdateLogger.info("Starting update check (mode: %s, configUrl: %s)", updateMode, updateConfigUrl);

        try {
            AcquireUpdateConfigService acquireService = new AcquireUpdateConfigService(
                    new URL(getUpdateConfigUrl()), connectTimeout, readTimeout);
            acquireService.valueProperty().addListener((observable, oldValue, application) -> {
                CheckUpdateService updateService = new CheckUpdateService(
                        application, getReleaseId(), getLicenseVersion(), getVersion());
                updateService.valueProperty().addListener((obs, oldVal, release) -> {
                    UpdateLogger.info("Update found: version %s (id: %d)", release.getVersion(), release.getId());
                    handleUpdateFound(release, callback, autoCloseSeconds);
                });
                updateService.setOnFailed(event -> {
                    UpdateLogger.info("No update available or check failed");
                    if (callback != null) callback.run();
                });
                updateService.start();
            });
            acquireService.setOnFailed(event -> {
                Throwable ex = acquireService.getException();
                UpdateLogger.error("Failed to acquire update config", ex);
                if (callback != null) callback.run();
            });
            acquireService.start();
        } catch (Exception e) {
            UpdateLogger.error("Failed to start update check", e);
            if (callback != null) callback.run();
        }
    }

    /**
     * 根据更新模式处理发现的更新
     */
    private void handleUpdateFound(ReleaseInfoModel release, Runnable callback, int autoCloseSeconds) {
        switch (updateMode) {
            case INTERACTIVE -> Platform.runLater(() ->
                    UpdaterDialogController.showUpdateDialog(
                            release, getReleaseId(), getVersion(), getLicenseVersion(), getThemeCssUrl(),
                            callback, autoCloseSeconds
                    )
            );

            case SILENT -> executeSilentUpdate(release, callback);

            case BACKGROUND_DOWNLOAD -> executeBackgroundDownload(release, callback);

            case CHECK_ONLY -> {
                UpdateLogger.info("CHECK_ONLY mode: update available - version %s", release.getVersion());
                if (callback != null) {
                    Platform.runLater(callback);
                }
            }
        }
    }

    /**
     * 静默更新：后台自动下载并安装，不弹出任何UI
     */
    private void executeSilentUpdate(ReleaseInfoModel release, Runnable callback) {
        UpdateLogger.info("Starting silent update to version %s", release.getVersion());

        InstallFileDownloadService downloadService = new InstallFileDownloadService(
                release, maxRetries, connectTimeout, readTimeout);

        downloadService.setOnSucceeded(event -> {
            UpdateLogger.info("Silent download completed, starting installation");
            ExecuteInstallerService installService = new ExecuteInstallerService(downloadService.getValue(), true);
            installService.setOnSucceeded(evt -> {
                UpdateLogger.info("Silent installation completed");
                if (callback != null) callback.run();
            });
            installService.setOnFailed(evt -> {
                UpdateLogger.error("Silent installation failed", installService.getException());
                if (callback != null) callback.run();
            });
            installService.start();
        });

        downloadService.setOnFailed(event -> {
            UpdateLogger.error("Silent download failed", downloadService.getException());
            if (callback != null) callback.run();
        });

        downloadService.start();
    }

    /**
     * 后台下载模式：后台自动下载，下载完成后弹出安装提示
     */
    private void executeBackgroundDownload(ReleaseInfoModel release, Runnable callback) {
        UpdateLogger.info("Starting background download for version %s", release.getVersion());

        InstallFileDownloadService downloadService = new InstallFileDownloadService(
                release, maxRetries, connectTimeout, readTimeout);

        downloadService.setOnSucceeded(event -> {
            UpdateLogger.info("Background download completed, showing install dialog");
            Platform.runLater(() ->
                    UpdaterProgressController.performUpdate(release, getThemeCssUrl())
            );
        });

        downloadService.setOnFailed(event -> {
            UpdateLogger.error("Background download failed", downloadService.getException());
            // 回退到交互模式
            Platform.runLater(() ->
                    UpdaterDialogController.showUpdateDialog(
                            release, getReleaseId(), getVersion(), getLicenseVersion(), getThemeCssUrl(),
                            callback, -1
                    )
            );
        });

        downloadService.start();
    }
}
