package com.lxwise.updater.model;

import java.util.List;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 应用信息
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class AppInfoModel {
    /**
     * 应用名称
     */
    private String name;
    /**
     * 更新日志
     */
    private String changelog;
    /**
     * 许可证
     */
    private String licenses;
    /**
     * 应用图标
     */
    private String icon;
    /**
     * 发布信息
     */
    private List<ReleaseInfoModel> releases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public String getLicenses() {
        return licenses;
    }

    public void setLicenses(String licenses) {
        this.licenses = licenses;
    }

    public List<ReleaseInfoModel> getReleases() {
        return releases;
    }

    public void setReleases(List<ReleaseInfoModel> releases) {
        this.releases = releases;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
