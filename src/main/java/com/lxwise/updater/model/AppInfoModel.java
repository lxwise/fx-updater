package com.lxwise.updater.model;

import java.net.URL;
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
    private URL changelog;
    /**
     * 许可证
     */
    private URL licenses;
    /**
     * 应用图标
     */
    private URL icon;
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

    public URL getChangelog() {
        return changelog;
    }

    public void setChangelog(URL changelog) {
        this.changelog = changelog;
    }

    public URL getLicenses() {
        return licenses;
    }

    public void setLicenses(URL licenses) {
        this.licenses = licenses;
    }

    public List<ReleaseInfoModel> getReleases() {
        return releases;
    }

    public void setReleases(List<ReleaseInfoModel> releases) {
        this.releases = releases;
    }

    public URL getIcon() {
        return icon;
    }

    public void setIcon(URL icon) {
        this.icon = icon;
    }
}
