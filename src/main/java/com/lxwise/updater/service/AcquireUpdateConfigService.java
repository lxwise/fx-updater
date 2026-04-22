package com.lxwise.updater.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lxwise.updater.model.AppInfoModel;
import com.lxwise.updater.model.InstallationFileInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.HttpUtils;
import com.lxwise.updater.utils.UpdateLogger;
import com.lxwise.updater.utils.VersionUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 获取更新配置任务，支持经典格式和简化格式两种JSON配置
 *
 * <p><b>经典格式</b>（含 releases 数组）：</p>
 * <pre>
 * {
 *   "name": "My App",
 *   "changelog": "http://server/changelog.html",
 *   "icon": "http://server/icon.png",
 *   "releases": [
 *     {
 *       "id": "20000",
 *       "version": "2.0.0",
 *       "licenseVersion": "1",
 *       "installationFileInfo": [
 *         { "downloadLink": "http://...", "fileSize": 97677, "platform": "win_x64" }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p><b>简化格式</b>（单版本 + platforms 映射）：</p>
 * <pre>
 * {
 *   "name": "My App",
 *   "version": "2.0.0",
 *   "changelog": "更新内容...",
 *   "platforms": {
 *     "win_x64": { "url": "http://...", "size": 97677 },
 *     "mac":     { "url": "http://...", "size": 97677 }
 *   }
 * }
 * </pre>
 *
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class AcquireUpdateConfigService extends Service<AppInfoModel> {

    private final URL updateConfigUrl;
    private final int connectTimeout;
    private final int readTimeout;

    public AcquireUpdateConfigService(URL updateConfigUrl) {
        this(updateConfigUrl, 15000, 30000);
    }

    public AcquireUpdateConfigService(URL updateConfigUrl, int connectTimeout, int readTimeout) {
        this.updateConfigUrl = updateConfigUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    protected Task<AppInfoModel> createTask() {
        return new Task<>() {
            @Override
            protected AppInfoModel call() throws Exception {
                UpdateLogger.info("Fetching update config from: %s", updateConfigUrl);

                URLConnection connection = HttpUtils.openConnection(updateConfigUrl, connectTimeout, readTimeout);
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    // 先解析为 JsonObject 判断格式
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                    AppInfoModel application;
                    if (root.has("releases")) {
                        // 经典格式：直接用Gson反序列化
                        application = new Gson().fromJson(root, AppInfoModel.class);
                        UpdateLogger.info("Parsed classic format config: %s (%d releases)",
                                application.getName(), application.getReleases().size());
                    } else if (root.has("version") && root.has("platforms")) {
                        // 简化格式：手动转换为AppInfoModel
                        application = parseSimplifiedFormat(root);
                        UpdateLogger.info("Parsed simplified format config: %s (version: %s)",
                                application.getName(), root.get("version").getAsString());
                    } else {
                        throw new IllegalArgumentException(
                                "Invalid update config format: must contain either 'releases' array or 'version' + 'platforms'");
                    }

                    // 设置反向引用
                    for (ReleaseInfoModel release : application.getReleases()) {
                        release.setAppInfo(application);
                    }
                    return application;
                } catch (Exception e) {
                    UpdateLogger.error("Failed to fetch/parse update config", e);
                    throw e;
                } finally {
                    HttpUtils.disconnect(connection);
                }
            }
        };
    }

    /**
     * 将简化格式JSON转换为AppInfoModel
     *
     * 简化格式示例：
     * {
     *   "name": "My App",
     *   "version": "2.0.0",
     *   "changelog": "- Bug fixes\n- New features",
     *   "icon": "http://server/icon.png",
     *   "downloadUrl": "https://www.example.com/",
     *   "platforms": {
     *     "win_x64": { "url": "http://.../App.exe", "size": 97677, "sha256": "abc..." },
     *     "mac":     { "url": "http://.../App.dmg", "size": 97677 }
     *   }
     * }
     */
    private static AppInfoModel parseSimplifiedFormat(JsonObject root) {
        AppInfoModel app = new AppInfoModel();

        // 基本信息
        app.setName(getStringOrNull(root, "name"));
        app.setChangelog(getStringOrNull(root, "changelog"));
        app.setIcon(getStringOrNull(root, "icon"));
        app.setLicenses(getStringOrNull(root, "licenses"));

        // 构建 release
        ReleaseInfoModel release = new ReleaseInfoModel();
        String version = root.get("version").getAsString();
        release.setVersion(version);
        release.setId(VersionUtil.toReleaseId(version));

        // licenseVersion: 可选，默认1
        if (root.has("licenseVersion")) {
            release.setLicenseVersion(root.get("licenseVersion").getAsInt());
        } else {
            release.setLicenseVersion(1);
        }

        // releaseDate: 可选
        if (root.has("releaseDate")) {
            // 保留字符串形式，Gson反序列化时会处理
        }

        // officialDownloadAddress / downloadUrl
        if (root.has("downloadUrl")) {
            release.setOfficialDownloadAddress(root.get("downloadUrl").getAsString());
        } else if (root.has("officialDownloadAddress")) {
            release.setOfficialDownloadAddress(root.get("officialDownloadAddress").getAsString());
        }

        // platforms → installationFileInfo 列表
        List<InstallationFileInfoModel> fileInfoList = new ArrayList<>();
        JsonObject platforms = root.getAsJsonObject("platforms");
        for (Map.Entry<String, JsonElement> entry : platforms.entrySet()) {
            String platform = entry.getKey();
            JsonObject platformObj = entry.getValue().getAsJsonObject();

            InstallationFileInfoModel fileInfo = new InstallationFileInfoModel();
            fileInfo.setPlatform(platform);
            fileInfo.setDownloadLink(platformObj.get("url").getAsString());
            if (platformObj.has("size")) {
                fileInfo.setFileSize(platformObj.get("size").getAsLong());
            }
            if (platformObj.has("sha256")) {
                fileInfo.setChecksum(platformObj.get("sha256").getAsString());
            }
            fileInfoList.add(fileInfo);
        }
        release.setInstallationFileInfo(fileInfoList);

        app.setReleases(List.of(release));
        return app;
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }
}
