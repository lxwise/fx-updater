package com.lxwise.updater.utils;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 语义化版本比较工具，支持标准SemVer格式 (major.minor.patch)
 *               消除对 releaseId 的强依赖，直接通过版本号字符串进行比较
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class VersionUtil {

    private VersionUtil() { }

    /**
     * 比较两个版本号的大小
     * 支持格式: "1.0.0", "2.1", "3", "1.0.0-beta1" 等
     *
     * @param v1 版本号1
     * @param v2 版本号2
     * @return 正数表示v1 > v2, 负数表示v1 < v2, 0表示相等
     */
    public static int compare(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;

        // 去除前缀 'v' 或 'V'
        v1 = stripPrefix(v1.trim());
        v2 = stripPrefix(v2.trim());

        // 分离预发布标签 (e.g., "1.0.0-beta1" → "1.0.0" + "beta1")
        String[] parts1 = splitPreRelease(v1);
        String[] parts2 = splitPreRelease(v2);

        // 比较数字部分
        int result = compareNumericParts(parts1[0], parts2[0]);
        if (result != 0) return result;

        // 数字部分相等时比较预发布标签
        // 有预发布标签的版本 < 没有预发布标签的版本 (1.0.0-beta < 1.0.0)
        boolean hasPre1 = !parts1[1].isEmpty();
        boolean hasPre2 = !parts2[1].isEmpty();

        if (hasPre1 && !hasPre2) return -1;
        if (!hasPre1 && hasPre2) return 1;
        if (hasPre1) return parts1[1].compareTo(parts2[1]);

        return 0;
    }

    /**
     * 判断 newVersion 是否比 currentVersion 更新
     *
     * @param currentVersion 当前版本
     * @param newVersion     新版本
     * @return true 表示有更新可用
     */
    public static boolean isNewer(String currentVersion, String newVersion) {
        return compare(newVersion, currentVersion) > 0;
    }

    /**
     * 从版本号字符串生成一个数值ID（用于向后兼容需要releaseId的场景）
     * 规则：major * 1000000 + minor * 1000 + patch
     * 例如 "2.1.3" → 2001003, "1.0.0" → 1000000
     *
     * @param version 版本号字符串
     * @return 数值ID
     */
    public static int toReleaseId(String version) {
        if (version == null || version.isBlank()) return 0;
        version = stripPrefix(version.trim());
        String numericPart = splitPreRelease(version)[0];
        String[] segments = numericPart.split("\\.");

        int id = 0;
        if (segments.length > 0) id += parseSegment(segments[0]) * 1_000_000;
        if (segments.length > 1) id += parseSegment(segments[1]) * 1_000;
        if (segments.length > 2) id += parseSegment(segments[2]);
        return id;
    }

    private static String stripPrefix(String version) {
        if (version.startsWith("v") || version.startsWith("V")) {
            return version.substring(1);
        }
        return version;
    }

    private static String[] splitPreRelease(String version) {
        int dashIndex = version.indexOf('-');
        if (dashIndex >= 0) {
            return new String[]{ version.substring(0, dashIndex), version.substring(dashIndex + 1) };
        }
        return new String[]{ version, "" };
    }

    private static int compareNumericParts(String v1, String v2) {
        String[] seg1 = v1.split("\\.");
        String[] seg2 = v2.split("\\.");
        int maxLen = Math.max(seg1.length, seg2.length);

        for (int i = 0; i < maxLen; i++) {
            int n1 = i < seg1.length ? parseSegment(seg1[i]) : 0;
            int n2 = i < seg2.length ? parseSegment(seg2[i]) : 0;
            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    private static int parseSegment(String segment) {
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
