package com.lxwise.updater.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 安装脚本工具类
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class InstallationScriptUtil {
	private InstallationScriptUtil() { }

	/**
	 * 复制安装脚本到临时目录
	 * @param name 脚本文件名
	 * @return 脚本路径
	 * @throws Exception 复制失败
	 */
	public static Path copyScript(String name) throws Exception {
		Path script = Paths.get(System.getProperty("java.io.tmpdir"), name);

		try (OutputStream fos = Files.newOutputStream(script, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
				InputStream is = InstallationScriptUtil.class.getResourceAsStream(name)) {
			if (is == null) {
				throw new IllegalStateException("Script resource not found: " + name);
			}
			is.transferTo(fos);
		}

		UpdateLogger.debug("Copied script '%s' to: %s", name, script);
		return script;
	}

	/**
	 * 获取当前进程的 PID。
	 * 优先使用 Java 9+ ProcessHandle API。
	 * @return 当前进程ID
	 */
	public static long getPID() {
		return AppRestartUtil.getCurrentPID();
	}
}
