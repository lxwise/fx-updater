package com.lxwise.updater.service;

import com.lxwise.updater.model.AppInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.NoUpdateException;
import com.lxwise.updater.utils.UpdateLogger;
import com.lxwise.updater.utils.VersionUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 检测更新任务，同时支持releaseId数值比较和语义版本号比较
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class CheckUpdateService extends Service<ReleaseInfoModel> {
    /**
     * 应用信息
     */
    private final AppInfoModel application;
    /**
     * 发布版本ID
     */
    private final Integer releaseId;
    /**
     * 版本对应的licence版本号
     */
    private final Integer licenseVersion;
    /**
     * 当前版本号（用于语义版本比较）
     */
    private final String currentVersion;

    /**
     * 构造函数（向后兼容）
     */
    public CheckUpdateService(AppInfoModel application, Integer releaseId, Integer licenseVersion) {
        this(application, releaseId, licenseVersion, null);
    }

    /**
     * 构造函数（增强版，支持语义版本比较）
     * @param application 应用信息
     * @param releaseId 当前发布ID
     * @param licenseVersion 当前许可证版本
     * @param currentVersion 当前版本号字符串（用于语义比较，可为null）
     */
    public CheckUpdateService(AppInfoModel application, Integer releaseId, Integer licenseVersion, String currentVersion) {
        this.application = application;
        this.releaseId = releaseId;
        this.licenseVersion = licenseVersion;
        this.currentVersion = currentVersion;
    }

    @Override
    protected Task<ReleaseInfoModel> createTask() {
        return new Task<>() {
            @Override
            protected ReleaseInfoModel call() throws Exception {
                if (application.getReleases().isEmpty()) {
                    throw new NoUpdateException();
                }

                // 初始化版本映射表
                Map<Integer, ReleaseInfoModel> releaseMap = new HashMap<>();
                ReleaseInfoModel latestVersion = null;

                for (ReleaseInfoModel release : application.getReleases()) {
                    int lv = release.getLicenseVersion();

                    // 更新版本映射表
                    releaseMap.merge(lv, release, (existing, candidate) -> {
                        // 优先使用语义版本比较，回退到releaseId比较
                        if (candidate.getVersion() != null && existing.getVersion() != null) {
                            return VersionUtil.compare(candidate.getVersion(), existing.getVersion()) > 0
                                    ? candidate : existing;
                        }
                        return candidate.getId() > existing.getId() ? candidate : existing;
                    });

                    // 更新全局最新版本
                    if (latestVersion == null) {
                        latestVersion = release;
                    } else if (release.getVersion() != null && latestVersion.getVersion() != null) {
                        if (VersionUtil.compare(release.getVersion(), latestVersion.getVersion()) > 0) {
                            latestVersion = release;
                        }
                    } else if (release.getId() > latestVersion.getId()) {
                        latestVersion = release;
                    }
                }

                // 检查当前 licenseVersion 对应的最新版本
                ReleaseInfoModel newestForThisLicense = releaseMap.get(licenseVersion);
                if (newestForThisLicense != null && isNewerRelease(newestForThisLicense)) {
                    UpdateLogger.info("Update found for current license: version %s", newestForThisLicense.getVersion());
                    return newestForThisLicense;
                }

                // 检查全局最新版本
                if (latestVersion != null && isNewerRelease(latestVersion)) {
                    UpdateLogger.info("Global update found: version %s", latestVersion.getVersion());
                    return latestVersion;
                }

                throw new NoUpdateException();
            }

            /**
             * 判断给定版本是否比当前版本新
             * 优先使用语义版本比较，回退到releaseId数值比较
             */
            private boolean isNewerRelease(ReleaseInfoModel release) {
                // 优先使用语义版本比较
                if (currentVersion != null && release.getVersion() != null) {
                    return VersionUtil.isNewer(currentVersion, release.getVersion());
                }
                // 回退到 releaseId 比较
                return release.getId() > releaseId;
            }
        };
    }
}
