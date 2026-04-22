package com.lxwise.updater.utils;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 无更新异常，表示当前已是最新版本
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class NoUpdateException extends Exception {
	private static final long serialVersionUID = -11232131231231223L;

	public NoUpdateException() {
		super("No update available, current version is up to date.");
	}

	public NoUpdateException(String message) {
		super(message);
	}
}
