package com.lxwise.updater.service;

import com.lxwise.updater.model.AppInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.NoUpdateException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 检测更新任务
 * @version: 1.0
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

    public CheckUpdateService(AppInfoModel application, Integer releaseId, Integer licenseVersion) {
        this.application = application;
        this.releaseId = releaseId;
        this.licenseVersion = licenseVersion;
    }

    @Override
    protected Task<ReleaseInfoModel> createTask() {
        // 创建并返回一个异步任务，用于执行更新检查逻辑
        return new Task<>() {
            @Override
            protected ReleaseInfoModel call() throws Exception {
                // 检查应用的发布版本列表是否为空，如果为空，抛出 NoUpdateException 表示无更新
                if (application.getReleases().isEmpty()) {
                    throw new NoUpdateException();
                }

                // 初始化一个映射表，用于存储每个 licenseVersion 的最新版本信息
                Map<Integer, ReleaseInfoModel> releaseMap = new HashMap<>();
                // 用于记录全局的最新版本信息
                ReleaseInfoModel latestVersion = null;

                // 遍历应用的所有发布版本，更新版本映射表和全局最新版本
                for (ReleaseInfoModel release : application.getReleases()) {
                    // 获取当前发布版本的 licenseVersion
                    int licenseVersion = release.getLicenseVersion();

                    // 更新版本映射表，如果当前 licenseVersion 的版本不存在或新版本的 ID 更大，则替换
                    releaseMap.merge(licenseVersion, release,
                            (existing, candidate) -> candidate.getId() > existing.getId() ? candidate : existing);

                    // 更新全局最新版本信息，如果当前 latestVersion 为 null 或新版本的 ID 更大，则更新
                    if (latestVersion == null || release.getId() > latestVersion.getId()) {
                        latestVersion = release;
                    }
                }

                // 获取当前 licenseVersion 对应的最新版本信息
                ReleaseInfoModel newestForThisLicense = releaseMap.get(licenseVersion);

                // 如果当前 licenseVersion 的最新版本存在，且其 ID 大于当前 releaseId，则返回该版本
                if (newestForThisLicense != null && newestForThisLicense.getId() > releaseId) {
                    return newestForThisLicense;
                }

                // 如果全局最新版本存在，且其 ID 大于当前 releaseId，则返回全局最新版本
                if (latestVersion != null && latestVersion.getId() > releaseId) {
                    return latestVersion;
                }

                // 如果没有找到符合条件的更新版本，则抛出 NoUpdateException 表示无更新
                throw new NoUpdateException();
            }
        };
    }

}
