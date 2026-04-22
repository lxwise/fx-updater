package com.lxwise.updater.service;

import com.lxwise.updater.utils.VersionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新配置构建器，提供流式API简化配置过程
 *               简化后仅需 version + configUrl 两项必填配置
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class UpdateConfig {

    private String updateConfigUrl;
    private String version;
    private Integer releaseId;          // 可选，不填则从version自动推导
    private Integer licenseVersion;     // 可选，默认1
    private String themeCssUrl;
    private UpdateMode updateMode = UpdateMode.INTERACTIVE;
    private int autoCloseSeconds = -1;
    private boolean autoRestart = false;
    private Runnable callback;
    private int connectTimeout = 15000;
    private int readTimeout = 30000;
    private int maxRetries = 3;
    private boolean checksumVerification = true;

    private UpdateConfig() { }

    /**
     * 创建新的配置构建器
     * @return 配置构建器实例
     */
    public static UpdateConfig builder() {
        return new UpdateConfig();
    }

    /**
     * 从应用主类的资源文件中加载配置
     * 读取 /app.properties 和 /theme.css
     * <p>
     * 简化后 app.properties 仅需2项必填配置：
     * <pre>
     * app.update.version = 1.0.0
     * app.update.configUrl = http://your-server/update.json
     * </pre>
     * 可选配置（有智能默认值）：
     * <pre>
     * app.update.releaseId = 10000        # 不填则从version自动推导
     * app.update.licenseVersion = 1       # 不填则默认为1
     * </pre>
     *
     * @param applicationClass 应用程序的主类
     * @return 配置构建器实例
     * @throws IOException 读取资源文件失败
     */
    public static UpdateConfig fromApplication(Class<?> applicationClass) throws IOException {
        InputStream is = applicationClass.getResourceAsStream("/app.properties");
        if (is == null) {
            throw new IOException("Resource /app.properties not found in classpath");
        }
        Properties properties = new Properties();
        properties.load(is);

        URL cssUrl = applicationClass.getResource("/theme.css");
        return fromProperties(properties)
                .themeCss(cssUrl != null ? cssUrl.toExternalForm() : null);
    }

    /**
     * 从Properties对象加载配置
     * releaseId和licenseVersion为可选项，不填则使用智能默认值
     * @param properties 配置属性
     * @return 配置构建器实例
     */
    public static UpdateConfig fromProperties(Properties properties) {
        String version = properties.getProperty("app.update.version");
        String releaseIdStr = properties.getProperty("app.update.releaseId");
        String licenseVersionStr = properties.getProperty("app.update.licenseVersion");

        UpdateConfig config = builder()
                .configUrl(properties.getProperty("app.update.configUrl"))
                .version(version);

        // releaseId: 如果配置了就用，否则从version自动推导
        if (releaseIdStr != null && !releaseIdStr.isBlank()) {
            config.releaseId(Integer.valueOf(releaseIdStr.trim()));
        }
        // licenseVersion: 如果配置了就用，否则默认1
        if (licenseVersionStr != null && !licenseVersionStr.isBlank()) {
            config.licenseVersion(Integer.valueOf(licenseVersionStr.trim()));
        }
        return config;
    }

    public UpdateConfig configUrl(String updateConfigUrl) {
        this.updateConfigUrl = updateConfigUrl;
        return this;
    }

    public UpdateConfig version(String version) {
        this.version = version;
        return this;
    }

    public UpdateConfig releaseId(Integer releaseId) {
        this.releaseId = releaseId;
        return this;
    }

    public UpdateConfig licenseVersion(Integer licenseVersion) {
        this.licenseVersion = licenseVersion;
        return this;
    }

    public UpdateConfig themeCss(String themeCssUrl) {
        this.themeCssUrl = themeCssUrl;
        return this;
    }

    /**
     * 设置更新模式
     * @param mode 更新模式（默认 INTERACTIVE）
     */
    public UpdateConfig updateMode(UpdateMode mode) {
        this.updateMode = mode;
        return this;
    }

    /**
     * 设置弹窗自动关闭时间（秒）
     * @param seconds 自动关闭秒数，<=0 表示不自动关闭
     */
    public UpdateConfig autoCloseSeconds(int seconds) {
        this.autoCloseSeconds = seconds;
        return this;
    }

    /**
     * 设置更新完成后是否自动重启应用
     * @param autoRestart 是否自动重启
     */
    public UpdateConfig autoRestart(boolean autoRestart) {
        this.autoRestart = autoRestart;
        return this;
    }

    /**
     * 设置更新完成/关闭后的回调函数
     * @param callback 回调
     */
    public UpdateConfig callback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 设置HTTP连接超时（毫秒）
     * @param connectTimeout 连接超时，默认15000ms
     */
    public UpdateConfig connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置HTTP读取超时（毫秒）
     * @param readTimeout 读取超时，默认30000ms
     */
    public UpdateConfig readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 设置下载失败时最大重试次数
     * @param maxRetries 最大重试次数，默认3
     */
    public UpdateConfig maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * 设置是否启用文件校验和验证
     * @param enabled 是否启用，默认true
     */
    public UpdateConfig checksumVerification(boolean enabled) {
        this.checksumVerification = enabled;
        return this;
    }

    /**
     * 使用当前配置构建FXUpdater实例
     * 仅 configUrl 和 version 为必填，其他项有智能默认值
     * @return FXUpdater实例
     */
    public FXUpdater build() {
        if (updateConfigUrl == null || updateConfigUrl.isBlank()) {
            throw new IllegalStateException("updateConfigUrl is required");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("version is required");
        }
        // 智能默认值：releaseId 从 version 自动推导
        if (releaseId == null) {
            releaseId = VersionUtil.toReleaseId(version);
        }
        // 智能默认值：licenseVersion 默认为 1
        if (licenseVersion == null) {
            licenseVersion = 1;
        }
        return new FXUpdater(this);
    }

    /**
     * 一行式便捷方法：构建FXUpdater并立即执行更新检查。
     * 使用配置中设置的callback和autoCloseSeconds。
     * <pre>
     * // 最简用法（仅一行）:
     * UpdateConfig.fromApplication(App.class).checkUpdate();
     *
     * // 高级用法:
     * UpdateConfig.fromApplication(App.class)
     *     .updateMode(UpdateMode.SILENT)
     *     .autoRestart(true)
     *     .checkUpdate();
     * </pre>
     */
    public void checkUpdate() {
        build().checkAppUpdate(callback, autoCloseSeconds);
    }

    // ========== Getters ==========

    public String getUpdateConfigUrl() { return updateConfigUrl; }
    public String getVersion() { return version; }
    public Integer getReleaseId() { return releaseId; }
    public Integer getLicenseVersion() { return licenseVersion; }
    public String getThemeCssUrl() { return themeCssUrl; }
    public UpdateMode getUpdateMode() { return updateMode; }
    public int getAutoCloseSeconds() { return autoCloseSeconds; }
    public boolean isAutoRestart() { return autoRestart; }
    public Runnable getCallback() { return callback; }
    public int getConnectTimeout() { return connectTimeout; }
    public int getReadTimeout() { return readTimeout; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isChecksumVerification() { return checksumVerification; }
}
