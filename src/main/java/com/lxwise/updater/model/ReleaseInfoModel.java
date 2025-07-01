package com.lxwise.updater.model;

import java.util.Date;
import java.util.List;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 发布版本信息
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ReleaseInfoModel {
    /**
     * 版本id
     */
    private Integer id;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本对应的licence版本号
     */
    private Integer licenseVersion;
    /**
     * 发布时间
     */
    private Date releaseDate;

    /**
     * 应用信息
     */
    private AppInfoModel appInfo;
    /**
     * 手动下载地址/官网地址
     */
    private String officialDownloadAddress;
    /**
     * 安装包信息
     */
    private List<InstallationFileInfoModel> installationFileInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getLicenseVersion() {
        return licenseVersion;
    }

    public void setLicenseVersion(Integer licenseVersion) {
        this.licenseVersion = licenseVersion;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public AppInfoModel getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfoModel appInfo) {
        this.appInfo = appInfo;
    }

    public List<InstallationFileInfoModel> getInstallationFileInfo() {
        return installationFileInfo;
    }

    public void setInstallationFileInfo(List<InstallationFileInfoModel> installationFileInfo) {
        this.installationFileInfo = installationFileInfo;
    }

    public String getOfficialDownloadAddress() {
        return officialDownloadAddress;
    }

    public void setOfficialDownloadAddress(String officialDownloadAddress) {
        this.officialDownloadAddress = officialDownloadAddress;
    }
}
