package com.lxwise.updater.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 统一日志工具类，提供分级日志记录能力
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class UpdateLogger {

    private static final Logger LOGGER = Logger.getLogger("com.lxwise.updater");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static volatile boolean initialized = false;

    private UpdateLogger() { }

    /**
     * 初始化日志系统（仅首次调用生效）
     */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleLogFormatter());
        LOGGER.addHandler(consoleHandler);
    }

    /**
     * 添加自定义日志处理器（如文件处理器）
     * @param handler 日志处理器
     */
    public static void addHandler(Handler handler) {
        init();
        LOGGER.addHandler(handler);
    }

    /**
     * 设置全局日志级别
     * @param level 日志级别
     */
    public static void setLevel(Level level) {
        init();
        LOGGER.setLevel(level);
    }

    public static void debug(String message) {
        init();
        LOGGER.fine(message);
    }

    public static void debug(String format, Object... args) {
        init();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format(format, args));
        }
    }

    public static void info(String message) {
        init();
        LOGGER.info(message);
    }

    public static void info(String format, Object... args) {
        init();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(String.format(format, args));
        }
    }

    public static void warn(String message) {
        init();
        LOGGER.warning(message);
    }

    public static void warn(String format, Object... args) {
        init();
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning(String.format(format, args));
        }
    }

    public static void error(String message) {
        init();
        LOGGER.severe(message);
    }

    public static void error(String message, Throwable throwable) {
        init();
        LOGGER.severe(message + "\n" + getStackTrace(throwable));
    }

    public static void error(String format, Object... args) {
        init();
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe(String.format(format, args));
        }
    }

    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * 简洁的日志格式化器
     */
    private static class SimpleLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("[%s] [%s] [FX-Updater] %s%n",
                    TIME_FMT.format(LocalDateTime.now()),
                    record.getLevel().getName(),
                    record.getMessage());
        }
    }
}
