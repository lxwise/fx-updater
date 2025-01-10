package com.lxwise.updater.model;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 平台枚举
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public enum EPlatformModel {

    win_x64("win_x64"),
    win_x86("win_x86"),
    mac("mac"),
    linux("linux"),
    other("other"),
    ;

    private final String os;

    EPlatformModel(String os) {
        this.os = os;
    }

    public String getOs() {
        return os;
    }
}
