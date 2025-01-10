package com.lxwise.updater.service;

import com.lxwise.updater.utils.InstallationScriptUtil;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.nio.file.Path;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 执行安装程序任务
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ExecuteInstallerService extends Service<Void> {
    // 存储安装程序的路径
    private final Path installer;
    public ExecuteInstallerService(Path installer) {
        this.installer = installer;
    }

    @Override
    protected Task<Void> createTask() {
        // 创建并返回一个异步任务，处理安装程序执行逻辑
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 拆分安装程序文件名，按点分隔，获取文件扩展名
                String[] nameComponents = installer.getFileName().toString().split("\\.");

                // 如果文件名没有扩展名，抛出异常
                if (nameComponents.length < 2) {
                    throw new IllegalArgumentException("Files without extensions are not supported yet");
                }

                // 获取文件扩展名并转换为小写
                String extension = nameComponents[nameComponents.length - 1].toLowerCase();

                // 根据文件扩展名执行对应的安装逻辑
                switch (extension) {
                    case "dmg": // DMG 文件（macOS）
                        handleDMGInstallation();
                        break;
                    case "exe": // EXE 文件（Windows）
                        handleEXEInstallation();
                        break;
                    case "msi": // MSI 文件（Windows）
                        handleMSIInstallation();
                        break;
                    case "rpm": // RPM 文件（Linux）
                        handleRPMInstallation();
                        break;
                    case "deb": // DEB 文件（Linux）
                        handleDEBInstallation();
                        break;
                    case "pkg": // PKG 文件（macOS）
                        handlePKGInstallation();
                        break;
                    default:
                        // 如果不支持该扩展名，抛出异常
                        throw new IllegalArgumentException(String.format("Installers with extension %s are not supported", extension));
                }

                // 安装完成后退出 JavaFX 平台
                Platform.exit();
                return null;
            }
        };
    }

    // 处理 DMG 文件安装逻辑（macOS）
    private void handleDMGInstallation() throws Exception {
        // 复制安装脚本到临时目录
        Path tmpScript = InstallationScriptUtil.copyScript("installdmg.sh");
        // 使用 ProcessBuilder 执行安装脚本
        new ProcessBuilder(
                "/bin/sh",
                tmpScript.toAbsolutePath().toString(),
                installer.toAbsolutePath().toString(),
                String.format("%d", InstallationScriptUtil.getPID())
        ).start();
    }

    // 处理 EXE 文件安装逻辑（Windows）
    private void handleEXEInstallation() throws Exception {
        // 构建启动 EXE 文件的命令，指定为后台执行
        String command = String.format("cmd /c start \"\" /b /high /min /wait \"%s\"", installer.toAbsolutePath());
        // 使用 ProcessBuilder 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
        // 设置进程输入输出流以抑制窗口弹出
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        // 启动进程
        processBuilder.start();
    }

    // 处理 MSI 文件安装逻辑（Windows）
    private void handleMSIInstallation() throws Exception {
        // 使用 msiexec 安装 MSI 文件，显示引导安装界面
        new ProcessBuilder("msiexec", "/i", installer.toAbsolutePath().toString(), "/norestart").start();
        // 静默安装（如需要，可启用）
//        new ProcessBuilder("msiexec", "/i", installer.toAbsolutePath().toString(), "/quiet", "/norestart").start();
    }

    // 处理 RPM 文件安装逻辑（Linux）
    private void handleRPMInstallation() throws Exception {
        // 使用 rpm 命令安装 RPM 包
        new ProcessBuilder("sudo", "rpm", "-i", "--quiet", installer.toAbsolutePath().toString()).start();
    }

    // 处理 DEB 文件安装逻辑（Linux）
    private void handleDEBInstallation() throws Exception {
        // 使用 dpkg 命令安装 DEB 包
        new ProcessBuilder("sudo", "dpkg", "-i", installer.toAbsolutePath().toString()).start();
    }

    // 处理 PKG 文件安装逻辑（macOS）
    private void handlePKGInstallation() throws Exception {
        // 使用 installer 命令安装 PKG 包到系统根目录
        new ProcessBuilder("sudo", "installer", "-pkg", installer.toAbsolutePath().toString(), "-target", "/").start();
    }
}
