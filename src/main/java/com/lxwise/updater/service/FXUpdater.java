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
     * 更新配置文件地址
     */
    private final URL updateConfigUrl;
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
     * 主题样式文件地址
     */
    private final URL themeCssUrl;

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
    public FXUpdater(URL updateConfigUrl, String version, Integer releaseId, Integer licenseVersion, URL themeCssUrl) {
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
    public FXUpdater(Properties properties, URL cssUrl) throws IOException{
        this(new URL(properties.getProperty("app.update.configUrl")),
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
    private static URL loadThemeCss(Class<?> applicationMain) throws IOException  {
        return applicationMain.getResource("/theme.css");
    }

    public URL getUpdateConfigUrl() {
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

    public URL getThemeCssUrl() {
        return themeCssUrl;
    }

    /**
     * 开始检查更新并提示用户安装任务。
     */
    public void checkAppUpdate() {
        AcquireUpdateConfigService acquireService = new AcquireUpdateConfigService(getUpdateConfigUrl());
        acquireService.valueProperty().addListener((observable, oldValue, application) -> {
            CheckUpdateService updateService = new CheckUpdateService(application, getReleaseId(), getLicenseVersion());
            updateService.valueProperty().addListener((obs, oldVal, release) -> {
                Platform.runLater(() -> UpdaterDialogController.showUpdateDialog(release, getReleaseId(), getVersion(), getLicenseVersion(), themeCssUrl));
            });
            updateService.start();
        });

        acquireService.start();
    }
}
