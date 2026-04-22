package com.lxwise.updater.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 应用重启工具类，支持跨平台的应用重启功能
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class AppRestartUtil {

    private AppRestartUtil() { }

    /**
     * 重启当前Java应用程序
     * <p>
     * 通过获取当前JVM启动参数和主类信息，构建新的进程来重启应用。
     * 支持以jar包方式和class方式运行的应用。
     * </p>
     *
     * @param appMainClass 应用程序主类（用于以class方式启动时）
     * @throws Exception 重启失败
     */
    public static void restartApplication(Class<?> appMainClass) throws Exception {
        UpdateLogger.info("Attempting to restart application...");

        String javaBin = getJavaBinary();
        List<String> command = new ArrayList<>();
        command.add(javaBin);

        // 添加JVM参数
        List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        command.addAll(vmArgs);

        // 判断是通过jar启动还是class启动
        String classPath = System.getProperty("java.class.path");
        String sunCommand = System.getProperty("sun.java.command");

        if (sunCommand != null && sunCommand.endsWith(".jar")) {
            // jar方式启动
            command.add("-jar");
            command.add(sunCommand.split(" ")[0]);
        } else if (classPath != null && !classPath.isBlank()) {
            // class方式启动
            command.add("-cp");
            command.add(classPath);
            if (appMainClass != null) {
                command.add(appMainClass.getName());
            } else if (sunCommand != null) {
                command.add(sunCommand.split(" ")[0]);
            }
        }

        UpdateLogger.info("Restart command: %s", String.join(" ", command));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO();
        builder.directory(new File(System.getProperty("user.dir")));
        builder.start();

        UpdateLogger.info("New process started, current process will exit.");
    }

    /**
     * 获取Java可执行文件路径
     * @return java/javaw 可执行文件的完整路径
     */
    private static String getJavaBinary() {
        String javaHome = System.getProperty("java.home");
        String separator = File.separator;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows上优先使用javaw（无控制台窗口）
            File javaw = new File(javaHome + separator + "bin" + separator + "javaw.exe");
            if (javaw.exists()) {
                return javaw.getAbsolutePath();
            }
            return javaHome + separator + "bin" + separator + "java.exe";
        }
        return javaHome + separator + "bin" + separator + "java";
    }

    /**
     * 获取当前进程PID
     * 优先使用Java 9+ ProcessHandle API，回退到RuntimeMXBean方式
     * @return 当前进程ID
     */
    public static long getCurrentPID() {
        try {
            // Java 9+ : ProcessHandle.current().pid()
            return ProcessHandle.current().pid();
        } catch (Exception e) {
            // 回退方案
            return getPIDFromRuntimeMXBean();
        }
    }

    /**
     * 从 RuntimeMXBean 的名称中检索PID（回退方案）
     */
    private static long getPIDFromRuntimeMXBean() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pidString = name.split("@")[0];
        return Long.parseLong(pidString);
    }
}
