package com.lxwise.updater.service;

import com.lxwise.updater.utils.InstallationScriptUtil;
import com.lxwise.updater.utils.UpdateLogger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.nio.file.Path;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 执行安装程序任务，支持多种安装包格式和静默安装模式
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class ExecuteInstallerService extends Service<Void> {
    // 存储安装程序的路径
    private final Path installer;
    // 是否以静默模式安装
    private final boolean silentMode;

    /**
     * 构造函数（向后兼容）
     */
    public ExecuteInstallerService(Path installer) {
        this(installer, false);
    }

    /**
     * 构造函数（增强版）
     * @param installer 安装程序路径
     * @param silentMode 是否静默安装
     */
    public ExecuteInstallerService(Path installer, boolean silentMode) {
        this.installer = installer;
        this.silentMode = silentMode;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String fileName = installer.getFileName().toString();
                int lastDot = fileName.lastIndexOf('.');

                if (lastDot < 0) {
                    throw new IllegalArgumentException("Files without extensions are not supported yet");
                }

                String extension = fileName.substring(lastDot + 1).toLowerCase();
                UpdateLogger.info("Executing installer: %s (extension: %s, silent: %s)", fileName, extension, silentMode);

                switch (extension) {
                    case "dmg" -> handleDMGInstallation();
                    case "exe" -> handleEXEInstallation();
                    case "msi" -> handleMSIInstallation();
                    case "rpm" -> handleRPMInstallation();
                    case "deb" -> handleDEBInstallation();
                    case "pkg" -> handlePKGInstallation();
                    case "gz"  -> handleTarGzInstallation(fileName);
                    case "zip" -> handleZipInstallation();
                    default -> throw new IllegalArgumentException(
                            String.format("Installers with extension %s are not supported", extension));
                }

                UpdateLogger.info("Installer process started successfully");
                Platform.exit();
                return null;
            }
        };
    }

    // 处理 DMG 文件安装逻辑（macOS）
    private void handleDMGInstallation() throws Exception {
        Path tmpScript = InstallationScriptUtil.copyScript("installdmg.sh");
        new ProcessBuilder(
                "/bin/sh",
                tmpScript.toAbsolutePath().toString(),
                installer.toAbsolutePath().toString(),
                String.format("%d", InstallationScriptUtil.getPID())
        ).start();
    }

    // 处理 EXE 文件安装逻辑（Windows）
    private void handleEXEInstallation() throws Exception {
        if (silentMode) {
            // 静默安装模式，常见的静默参数: /S, /silent, /VERYSILENT
            ProcessBuilder pb = new ProcessBuilder(installer.toAbsolutePath().toString(), "/S");
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.start();
            UpdateLogger.info("EXE silent install started");
        } else {
            String command = String.format("cmd /c start \"\" /b /high /min /wait \"%s\"", installer.toAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();
        }
    }

    // 处理 MSI 文件安装逻辑（Windows）
    private void handleMSIInstallation() throws Exception {
        if (silentMode) {
            new ProcessBuilder("msiexec", "/i", installer.toAbsolutePath().toString(), "/quiet", "/norestart").start();
            UpdateLogger.info("MSI silent install started");
        } else {
            new ProcessBuilder("msiexec", "/i", installer.toAbsolutePath().toString(), "/norestart").start();
        }
    }

    // 处理 RPM 文件安装逻辑（Linux）
    private void handleRPMInstallation() throws Exception {
        new ProcessBuilder("sudo", "rpm", "-i", "--quiet", installer.toAbsolutePath().toString()).start();
    }

    // 处理 DEB 文件安装逻辑（Linux）
    private void handleDEBInstallation() throws Exception {
        new ProcessBuilder("sudo", "dpkg", "-i", installer.toAbsolutePath().toString()).start();
    }

    // 处理 PKG 文件安装逻辑（macOS）
    private void handlePKGInstallation() throws Exception {
        new ProcessBuilder("sudo", "installer", "-pkg", installer.toAbsolutePath().toString(), "-target", "/").start();
    }

    // 处理 tar.gz 文件解压安装逻辑（Linux）
    private void handleTarGzInstallation(String fileName) throws Exception {
        if (!fileName.endsWith(".tar.gz")) {
            throw new IllegalArgumentException("Only .tar.gz archives are supported for .gz extension");
        }
        String targetDir = System.getProperty("user.home");
        new ProcessBuilder("tar", "-xzf", installer.toAbsolutePath().toString(), "-C", targetDir).start();
        UpdateLogger.info("tar.gz extracted to: %s", targetDir);
    }

    // 处理 zip 文件解压安装逻辑（跨平台）
    private void handleZipInstallation() throws Exception {
        String targetDir = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("win")) {
            new ProcessBuilder("powershell", "-Command",
                    String.format("Expand-Archive -Path '%s' -DestinationPath '%s' -Force",
                            installer.toAbsolutePath(), targetDir)).start();
        } else {
            new ProcessBuilder("unzip", "-o", installer.toAbsolutePath().toString(), "-d", targetDir).start();
        }
        UpdateLogger.info("zip extracted to: %s", targetDir);
    }
}
