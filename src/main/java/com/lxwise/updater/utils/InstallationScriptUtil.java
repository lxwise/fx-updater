package com.lxwise.updater.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 安装脚本工具类
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public final class InstallationScriptUtil {
	private InstallationScriptUtil() { }

	/**
	 * 复制安装脚本
	 * @param name
	 * @return 脚本路径
	 * @throws Exception
	 */
	public static Path copyScript(String name) throws Exception {
		Path script = Paths.get(System.getProperty("java.io.tmpdir"), name);
		
		try(OutputStream fos = Files.newOutputStream(script, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE); 
				InputStream is = InstallationScriptUtil.class.getResourceAsStream(name)) {
			byte[] buffer = new byte[8192];
			int n;
			
			while ((n = is.read(buffer)) != -1) {
				fos.write(buffer, 0, n);
			}
		}
		
		return script;
	}

	/**
	 * 获取当前进程的 PID。
	 * @return
	 */
	public static int getPID() {
		try {
			return getPIDFromRuntimeMXBean();
		} catch (NumberFormatException e) {
			throw new UnsupportedOperationException("Unable to retrieve PID from RuntimeMXBean", e);
		}
	}

	/**
	 * 从 RuntimeMXBean 的名称中检索进程 ID （PID）。
	 * 格式通常为 “PID@hostname”。
	 *
	 * @return 进程 ID （PID）。
	 * @throws NumberFormatException
	 */
	private static int getPIDFromRuntimeMXBean() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		// Format: "12345@hostname"
		String name = runtime.getName();
		// Extract "12345".
		String pidString = name.split("@")[0];
		return Integer.parseInt(pidString);
	}
}
