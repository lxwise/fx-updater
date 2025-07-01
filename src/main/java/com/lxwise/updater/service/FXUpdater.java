package com.lxwise.updater.service;

import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import com.lxwise.updater.gui.UpdaterDialogController;

/**
 * @author lxwise
 * @create 2024-05
 * @description: updater更新器程序
 * @version: 1.0
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
    }

    /**
     * 创建并初始化FXUpdater类的实例。
     * @param properties 资源文件下的配置文件
     * @param cssUrl 主题样式文件地址
     * @throws IOException
     */
    public FXUpdater(Properties properties, String cssUrl) {
        this(
                properties.getProperty("app.update.configUrl"),
                properties.getProperty("app.update.version"),
                Integer.valueOf(properties.getProperty("app.update.releaseId")),
                Integer.valueOf(properties.getProperty("app.update.licenseVersion")),
                cssUrl
        );
    }


    /**
     * 读取资源文件下的配置文件
     * @param application
     * @return
     * @throws IOException
     */
    private static Properties loadProperties(Class<?> application) throws IOException {
        Properties properties = new Properties();
        properties.load(application.getResourceAsStream("/app.properties"));
        return properties;
    }

    /**
     * 读取资源文件下的主题样式文件
     * @param applicationMain
     * @return
     * @throws IOException
     */
    private static String loadThemeCss(Class<?> applicationMain) {
        URL url = applicationMain.getResource("/theme.css");
        return url != null ? url.toExternalForm() : null;
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
        try {
            AcquireUpdateConfigService acquireService = new AcquireUpdateConfigService(new URL(getUpdateConfigUrl()));
            acquireService.valueProperty().addListener((observable, oldValue, application) -> {
                CheckUpdateService updateService = new CheckUpdateService(application, getReleaseId(), getLicenseVersion());
                updateService.valueProperty().addListener((obs, oldVal, release) -> {
                    Platform.runLater(() -> {
                        UpdaterDialogController.showUpdateDialog(
                                release, getReleaseId(), getVersion(), getLicenseVersion(), getThemeCssUrl(),
                                callback, autoCloseSeconds
                        );
                    });
                });
                updateService.start();
            });
            acquireService.start();
        } catch (Exception e) {
            e.printStackTrace();
            // 可添加日志记录
        }
    }


}
