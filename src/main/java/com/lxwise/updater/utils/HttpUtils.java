package com.lxwise.updater.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author lxwise
 * @create 2024-05
 * @description: HTTP连接工具类，提供超时控制、重试机制和校验和验证
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class HttpUtils {

    private static final int DEFAULT_CONNECT_TIMEOUT = 15000;
    private static final int DEFAULT_READ_TIMEOUT = 30000;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private HttpUtils() { }

    /**
     * 打开带超时控制的URL连接
     * @param url 目标URL
     * @param connectTimeout 连接超时（毫秒）
     * @param readTimeout 读取超时（毫秒）
     * @return URLConnection 连接对象
     * @throws IOException 连接失败
     */
    public static URLConnection openConnection(URL url, int connectTimeout, int readTimeout) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(connectTimeout > 0 ? connectTimeout : DEFAULT_CONNECT_TIMEOUT);
        connection.setReadTimeout(readTimeout > 0 ? readTimeout : DEFAULT_READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", "FX-Updater/2.0");
        return connection;
    }

    /**
     * 使用默认超时打开URL连接
     * @param url 目标URL
     * @return URLConnection
     * @throws IOException 连接失败
     */
    public static URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 打开支持断点续传的连接
     * @param url 目标URL
     * @param startByte 起始字节位置
     * @param connectTimeout 连接超时
     * @param readTimeout 读取超时
     * @return URLConnection 连接对象
     * @throws IOException 连接失败
     */
    public static URLConnection openRangeConnection(URL url, long startByte, int connectTimeout, int readTimeout) throws IOException {
        URLConnection connection = openConnection(url, connectTimeout, readTimeout);
        if (startByte > 0) {
            connection.setRequestProperty("Range", "bytes=" + startByte + "-");
        }
        return connection;
    }

    /**
     * 检查服务器是否支持断点续传
     * @param connection URL连接
     * @return 是否支持Range请求
     */
    public static boolean supportsResume(URLConnection connection) {
        String acceptRanges = connection.getHeaderField("Accept-Ranges");
        return "bytes".equalsIgnoreCase(acceptRanges);
    }

    /**
     * 带重试的执行操作
     * @param action 要执行的操作
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 所有重试均失败后抛出最后一次异常
     */
    public static <T> T executeWithRetry(RetryAction<T> action, int maxRetries) throws Exception {
        int retries = Math.max(1, maxRetries);
        Exception lastException = null;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                return action.execute(attempt);
            } catch (Exception e) {
                lastException = e;
                UpdateLogger.warn("Operation failed (attempt %d/%d): %s", attempt, retries, e.getMessage());
                if (attempt < retries) {
                    try {
                        Thread.sleep((long) RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        }
        throw lastException;
    }

    /**
     * 使用默认重试次数执行操作
     */
    public static <T> T executeWithRetry(RetryAction<T> action) throws Exception {
        return executeWithRetry(action, DEFAULT_MAX_RETRIES);
    }

    /**
     * 计算输入流的SHA-256校验和
     * @param inputStream 输入流
     * @return 十六进制校验和字符串
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 算法不支持
     */
    public static String calculateSHA256(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        return bytesToHex(digest.digest());
    }

    /**
     * 验证文件校验和
     * @param expected 期望的校验和
     * @param actual 实际的校验和
     * @return 是否匹配
     */
    public static boolean verifyChecksum(String expected, String actual) {
        if (expected == null || expected.isBlank()) {
            return true; // 没有提供校验和则跳过验证
        }
        return expected.equalsIgnoreCase(actual);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 安全关闭HttpURLConnection
     * @param connection 连接对象
     */
    public static void disconnect(URLConnection connection) {
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection) connection).disconnect();
        }
    }

    /**
     * 可重试操作的函数式接口
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface RetryAction<T> {
        /**
         * 执行操作
         * @param attempt 当前尝试次数（从1开始）
         * @return 操作结果
         * @throws Exception 操作失败时抛出异常
         */
        T execute(int attempt) throws Exception;
    }
}
