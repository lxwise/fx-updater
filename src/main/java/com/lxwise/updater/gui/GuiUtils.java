package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.UpdateLogger;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * @author lxwise
 * @create 2024-05
 * @description: GUI公共工具类，提取更新对话框和进度窗口的公共逻辑
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public final class GuiUtils {

    private static final String DEFAULT_ICON_PATH = "images/fx-updater-logo.png";

    private GuiUtils() { }

    /**
     * 设置窗口图标
     * @param release 发布信息
     * @param stage 目标窗口
     */
    public static void setStageIcon(ReleaseInfoModel release, Stage stage) {
        try {
            String iconStr = release.getAppInfo().getIcon();
            if (iconStr != null && !iconStr.isBlank()) {
                stage.getIcons().add(new Image(iconStr));
            } else {
                stage.getIcons().add(new Image(DEFAULT_ICON_PATH));
            }
        } catch (Exception e) {
            UpdateLogger.warn("Failed to load icon, using default: %s", e.getMessage());
            try {
                stage.getIcons().add(new Image(DEFAULT_ICON_PATH));
            } catch (Exception ignored) {
                // 默认图标也加载失败，忽略
            }
        }
    }

    /**
     * 设置窗口关闭确认对话框
     * @param stage 目标窗口
     * @param i18nBundle 国际化资源
     * @param onConfirmClose 确认关闭时的回调（可为null）
     */
    public static void setupCloseConfirmation(Stage stage, ResourceBundle i18nBundle, Runnable onConfirmClose) {
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(i18nBundle.getString("infotext.title"));
            alert.setHeaderText(null);
            alert.setContentText(i18nBundle.getString("alert.confirm.exit"));

            ButtonType ok = new ButtonType(i18nBundle.getString("button.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType(i18nBundle.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(ok, cancel);
            alert.initOwner(stage);

            alert.showAndWait().ifPresent(response -> {
                if (response == cancel) {
                    event.consume();
                } else {
                    if (onConfirmClose != null) {
                        onConfirmClose.run();
                    }
                }
            });
        });
    }

    /**
     * 构建窗口标题
     * @param release 发布信息
     * @param i18nBundle 国际化资源
     * @return 窗口标题字符串
     */
    public static String buildStageTitle(ReleaseInfoModel release, ResourceBundle i18nBundle) {
        return release.getAppInfo().getName() + i18nBundle.getString("infotext.title");
    }

    /**
     * 字节转化为人类可读的格式
     * @param fileSizeInBytes 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    public static String formatFileSize(double fileSizeInBytes) {
        final double BYTE_UNIT = 1024.0;

        if (fileSizeInBytes < BYTE_UNIT) {
            return String.format("%.0f B", fileSizeInBytes);
        }

        int unitIndex = (int) (Math.log(fileSizeInBytes) / Math.log(BYTE_UNIT));
        char unitPrefix = "kMGTPE".charAt(unitIndex - 1);
        double convertedSize = fileSizeInBytes / Math.pow(BYTE_UNIT, unitIndex);
        return String.format("%.1f %sB", convertedSize, unitPrefix);
    }
}
