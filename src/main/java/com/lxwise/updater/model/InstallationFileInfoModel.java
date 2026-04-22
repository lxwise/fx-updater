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
    private String downloadLink;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 平台
     */
    private String platform;
    /**
     * 文件校验和（SHA-256），用于验证下载文件完整性
     */
    private String checksum;

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
