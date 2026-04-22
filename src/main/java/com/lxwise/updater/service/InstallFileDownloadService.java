package com.lxwise.updater.service;

import com.lxwise.updater.model.EPlatformModel;
import com.lxwise.updater.model.InstallationFileInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.HttpUtils;
import com.lxwise.updater.utils.UpdateLogger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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
 * @description: 安装文件下载服务类,负责根据当前平台下载指定安装包到本地，支持断点续传和重试
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class InstallFileDownloadService extends Service<Path> {

    private final ReleaseInfoModel releaseInfoModel;
    private int maxRetries = 3;
    private int connectTimeout = 15000;
    private int readTimeout = 60000;

    /**
     * 构造函数，接收ReleaseInfoModel对象（向后兼容）
     *
     * @param releaseInfoModel 包含发布信息的模型
     */
    public InstallFileDownloadService(ReleaseInfoModel releaseInfoModel) {
        if (releaseInfoModel == null) {
            throw new IllegalArgumentException("download fails, please try again");
        }
        this.releaseInfoModel = releaseInfoModel;
    }

    /**
     * 构造函数（增强版），支持配置重试和超时
     *
     * @param releaseInfoModel 包含发布信息的模型
     * @param maxRetries 最大重试次数
     * @param connectTimeout 连接超时（毫秒）
     * @param readTimeout 读取超时（毫秒）
     */
    public InstallFileDownloadService(ReleaseInfoModel releaseInfoModel, int maxRetries, int connectTimeout, int readTimeout) {
        this(releaseInfoModel);
        this.maxRetries = Math.max(1, maxRetries);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
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

                // 获取下载链接
                String downloadLinkStr = needDownloadFile.getDownloadLink();
                URL downloadUrl = new URL(downloadLinkStr);

                UpdateLogger.info("Starting download from: %s", downloadLinkStr);

                // 使用重试机制执行下载
                return HttpUtils.executeWithRetry(attempt -> {
                    UpdateLogger.info("Download attempt %d/%d", attempt, maxRetries);
                    return doDownload(downloadUrl, needDownloadFile);
                }, maxRetries);
            }

            /**
             * 执行实际的下载逻辑，支持断点续传
             */
            private Path doDownload(URL downloadUrl, InstallationFileInfoModel needDownloadFile) throws Exception {
                URLConnection connection = HttpUtils.openConnection(downloadUrl, connectTimeout, readTimeout);
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

                // 检查是否可以断点续传
                long existingSize = 0;
                if (Files.exists(downloadFile)) {
                    existingSize = Files.size(downloadFile);
                    // 如果已下载的文件大小与目标一致，直接返回
                    if (existingSize == fileSize && fileSize > 0) {
                        UpdateLogger.info("File already downloaded: %s", downloadFile);
                        updateProgress(fileSize, fileSize);
                        return downloadFile;
                    }
                    // 尝试断点续传
                    if (existingSize > 0 && existingSize < fileSize && HttpUtils.supportsResume(connection)) {
                        HttpUtils.disconnect(connection);
                        connection = HttpUtils.openRangeConnection(downloadUrl, existingSize, connectTimeout, readTimeout);
                        connection.connect();
                        UpdateLogger.info("Resuming download from byte %d", existingSize);
                    } else {
                        existingSize = 0; // 不支持续传，从头开始
                    }
                }

                // 开始下载文件
                StandardOpenOption[] openOptions = existingSize > 0
                        ? new StandardOpenOption[]{StandardOpenOption.APPEND}
                        : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

                try (OutputStream fos = Files.newOutputStream(downloadFile, openOptions);
                     BufferedInputStream is = new BufferedInputStream(connection.getInputStream())) {

                    byte[] buffer = new byte[16384]; // 增大缓冲区提升性能
                    int bytesRead;
                    long totalDownloaded = existingSize;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        if (isCancelled()) {
                            UpdateLogger.info("Download cancelled by user");
                            return downloadFile;
                        }
                        fos.write(buffer, 0, bytesRead);
                        totalDownloaded += bytesRead;
                        updateProgress(totalDownloaded, fileSize);
                    }
                } finally {
                    HttpUtils.disconnect(connection);
                }

                UpdateLogger.info("Download completed: %s (size: %d bytes)", downloadFile, Files.size(downloadFile));
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
    private String extractFileName(URLConnection connection, InstallationFileInfoModel fileInfo) throws MalformedURLException {
        String fileName = connection.getHeaderField("Content-Disposition");
        if (fileName != null && fileName.contains("=")) {
            fileName = fileName.split("=")[1].replace("\"", "");
        } else {
            String urlPath = new URL(fileInfo.getDownloadLink()).getPath();
            fileName = Paths.get(urlPath).getFileName().toString();
        }

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("The file name could not be determined");
        }
        return fileName;
    }
}
