package com.lxwise.updater.model;

import java.net.URL;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 安装文件信息
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class InstallationFileInfoModel {
    /**
     * 下载链接
     */
    private URL downloadLink;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 平台
     */
    private String platform;

    public URL getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(URL downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
