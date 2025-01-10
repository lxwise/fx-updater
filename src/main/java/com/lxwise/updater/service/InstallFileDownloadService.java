package com.lxwise.updater.service;

import com.lxwise.updater.model.EPlatformModel;
import com.lxwise.updater.model.InstallationFileInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 安装文件下载服务类,负责根据当前平台下载指定安装包到本地
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class InstallFileDownloadService extends Service<Path> {

    private final ReleaseInfoModel releaseInfoModel;

    /**
     * 构造函数，接收ReleaseInfoModel对象
     *
     * @param releaseInfoModel 包含发布信息的模型
     */
    public InstallFileDownloadService(ReleaseInfoModel releaseInfoModel) {
        if (releaseInfoModel == null) {
            throw new IllegalArgumentException("download fails, please try again");
        }
        this.releaseInfoModel = releaseInfoModel;
    }

    @Override
    protected Task<Path> createTask() {
        // 创建并返回一个异步任务，处理安装程序下载逻辑
        return new Task<>() {
            @Override
            protected Path call() throws Exception {
                // 查找当前平台对应的安装文件
                InstallationFileInfoModel needDownloadFile = releaseInfoModel.getInstallationFileInfo()
                        .stream()
                        .filter(infoModel -> checkCurrentPlatform(infoModel.getPlatform()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Failed to download the installation package, and there is no installation package that supports the system"));


                // 获取下载链接并建立连接
                URL downloadLink = needDownloadFile.getDownloadLink();
                URLConnection connection = downloadLink.openConnection();
                connection.connect();

                // 获取文件大小
                long fileSize = connection.getContentLengthLong();
                if (fileSize == -1) {
                    fileSize = needDownloadFile.getFileSize();
                }

                updateProgress(0, fileSize);

                // 确定文件名
                String fileName = extractFileName(connection, needDownloadFile);
                Path downloadFile = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

                // 开始下载文件
                try (OutputStream fos = Files.newOutputStream(downloadFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                     BufferedInputStream is = new BufferedInputStream(connection.getInputStream())) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalDownloaded = 0;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalDownloaded += bytesRead;
                        updateProgress(totalDownloaded, fileSize);
                    }
                }

                return downloadFile;
            }
        };
    }

    /**
     * 检查当前平台是否匹配指定平台
     *
     * @param platform 安装文件支持的平台标识
     * @return 如果匹配返回true，否则返回false
     */
    private boolean checkCurrentPlatform(String platform) {
        if (Objects.isNull(platform) || platform.isBlank()) {
            throw new IllegalStateException("Failed to download the installation package, and there is no installation package that supports the system");
        }

        if(EPlatformModel.other.getOs().equals(platform)){
            return true;
        }

        String currentPlatform = System.getProperty("os.name").toLowerCase();

        if (currentPlatform.startsWith("mac")) {
            return EPlatformModel.mac.name().equals(platform);
        } else if (currentPlatform.startsWith("windows")) {
            if (System.getProperty("os.arch").contains("64")) {
                return EPlatformModel.win_x64.getOs().equals(platform);
            } else {
                return EPlatformModel.win_x86.getOs().equals(platform);
            }
        } else if (currentPlatform.startsWith("linux")) {
            return EPlatformModel.linux.getOs().equals(platform);
        } else {
            throw new IllegalStateException("Failed to download the installation package, and there is no installation package that supports the system");
        }
    }

    /**
     * 提取文件名
     *
     * @param connection URL连接对象
     * @param fileInfo   安装文件信息
     * @return 提取的文件名
     */
    private String extractFileName(URLConnection connection, InstallationFileInfoModel fileInfo) {
        String fileName = connection.getHeaderField("Content-Disposition");
        if (fileName != null && fileName.contains("=")) {
            fileName = fileName.split("=")[1].replace("\"", "");
        } else {
            String urlPath = fileInfo.getDownloadLink().getPath();
            fileName = Paths.get(urlPath).getFileName().toString();
        }

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("The file name could not be determined");
        }
        return fileName;
    }
}
