package com.lxwise.updater.service;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新模式枚举，支持多种更新策略
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public enum UpdateMode {

    /**
     * 交互式更新（默认）：弹出对话框，用户手动确认是否更新
     */
    INTERACTIVE,

    /**
     * 静默更新：后台自动下载并安装，不弹出任何UI
     */
    SILENT,

    /**
     * 后台下载模式：后台自动下载，下载完成后弹出安装提示
     */
    BACKGROUND_DOWNLOAD,

    /**
     * 仅检查模式：仅检查是否有更新，通过回调通知结果，不执行任何下载或安装
     */
    CHECK_ONLY
}
