package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.utils.UpdateLogger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新弹窗控制器
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class UpdaterDialogController {

    @FXML
    private ResourceBundle resources;
    @FXML
    public Label versionInfoLabel;
    @FXML
    public TextArea textArea;

    private ReleaseInfoModel release;
    private Integer currentReleaseId;
    private String currentVersion;
    private Integer currentLicenseVersion;
    private String themeCssUrl;
    private Runnable callback; // 回调函数

    /**
     * 显示更新弹窗
     * @param release 发布版本信息
     * @param releaseId 当前内部版本
     * @param version 当前版本
     * @param licenseVersion 当前授权版本
     * @param themeCssUrl 主题样式
     * @param callback 回调方法
     * @param autoCloseSeconds 弹窗自动关闭时间
     */
    public static void showUpdateDialog(
            ReleaseInfoModel release,
            Integer releaseId,
            String version,
            int licenseVersion,
            String themeCssUrl,
            Runnable callback,
            int autoCloseSeconds
    ) {
        try {
            UpdateLogger.info("Showing update dialog for version %s", release.getVersion());

            ResourceBundle i18nBundle = ResourceBundle.getBundle("com.lxwise.updater.i18n.updater");
            FXMLLoader loader = new FXMLLoader(UpdaterDialogController.class.getResource("UpdaterDialog.fxml"), i18nBundle);
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent page = loader.load();
            UpdaterDialogController controller = loader.getController();

            controller.release = release;
            controller.currentReleaseId = releaseId;
            controller.currentVersion = version;
            controller.currentLicenseVersion = licenseVersion;
            controller.themeCssUrl = themeCssUrl;
            controller.callback = callback;
            controller.initialize();

            Scene scene = new Scene(page);
            if (themeCssUrl != null) {
                scene.getStylesheets().add(themeCssUrl);
            }

            final Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(GuiUtils.buildStageTitle(release, i18nBundle));
            GuiUtils.setStageIcon(release, stage);

            stage.setAlwaysOnTop(true);
            stage.show();
            stage.toFront();

            // 自动关闭逻辑
            if (autoCloseSeconds > 0) {
                PauseTransition pause = new PauseTransition(Duration.seconds(autoCloseSeconds));
                pause.setOnFinished(event -> {
                    if (stage.isShowing()) {
                        UpdateLogger.info("Auto-closing update dialog after %d seconds", autoCloseSeconds);
                        stage.close();
                        if (callback != null) callback.run();
                    }
                });
                pause.play();
            }

            // 关闭请求逻辑
            GuiUtils.setupCloseConfirmation(stage, i18nBundle, callback);

        } catch (Throwable ex) {
            UpdateLogger.error("Failed to show update dialog", ex);
        }
    }


    private void initialize() {
        String changelog = release.getAppInfo().getChangelog();
        if (changelog != null && !changelog.isBlank()) {
            loadContentIntoTextArea(changelog);
        } else {
            textArea.setVisible(false);
            textArea.setManaged(false);
        }

        Object[] messageArguments = { release.getAppInfo().getName(), currentVersion, release.getVersion() };
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(resources.getLocale());
        formatter.applyPattern(resources.getString("infotext.freeUpgrade"));
        versionInfoLabel.setText(formatter.format(messageArguments));
        versionInfoLabel.autosize();
    }

    /**
     * 异步加载线上内容并设置到现有的 TextArea，使用 JavaFX Task 代替裸线程
     * @param url 内容URL
     */
    private void loadContentIntoTextArea(String url) {
        Task<String> loadTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                try (InputStream inputStream = new URL(url).openStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    return content.toString();
                }
            }
        };

        loadTask.setOnSucceeded(event -> textArea.setText(loadTask.getValue()));
        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            UpdateLogger.error("Failed to load changelog", ex);
            textArea.setText("Failed to load content from the URL: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void close() {
        Stage stage = (Stage) versionInfoLabel.getScene().getWindow();
        stage.close();
        if (callback != null) {
            callback.run();
        }
    }

    @FXML
    public void ignoreVersionAction(ActionEvent actionEvent) {
        UpdateLogger.info("User chose to ignore this version");
        close();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        UpdateLogger.info("User chose to remind later");
        close();
    }

    @FXML
    public void executeUpdateAction(ActionEvent actionEvent) {
        UpdateLogger.info("User chose to update now");
        UpdaterProgressController.performUpdate(release, themeCssUrl);
        close();
    }
}
