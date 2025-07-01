package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新弹窗控制器
 * @version: 1.0
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
            stage.setTitle(release.getAppInfo().getName() + i18nBundle.getString("infotext.title"));

            String iconStr = release.getAppInfo().getIcon();
            if (iconStr != null && !iconStr.isBlank()) {
                stage.getIcons().add(new Image(iconStr));
            } else {
                stage.getIcons().add(new Image("images/fx-updater-logo.png"));
            }

            stage.setAlwaysOnTop(true);
            stage.show();
            stage.toFront();

            // 自动关闭逻辑
            if (autoCloseSeconds > 0) {
                PauseTransition pause = new PauseTransition(Duration.seconds(autoCloseSeconds));
                pause.setOnFinished(event -> {
                    if (stage.isShowing()) {
                        stage.close();
                        if (callback != null) callback.run();
                    }
                });
                pause.play();
            }


            // 关闭请求逻辑
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
                        if (callback != null) callback.run();
                    }
                });
            });

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }


    private void initialize() {

        String changelog = release.getAppInfo().getChangelog();
        if (changelog != null && !changelog.isBlank()) {
            // 异步加载线上内容并设置到现有的 TextArea
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
     * 异步加载线上内容并设置到现有的
     * @param url
     */
    private void loadContentIntoTextArea(String url) {
        // 使用后台线程读取线上内容，避免阻塞 UI
        new Thread(() -> {
            try (InputStream inputStream = new URL(url).openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // 更新 UI 必须在 JavaFX 应用线程中完成
                Platform.runLater(() -> textArea.setText(content.toString()));
            } catch (IOException e) {
                Platform.runLater(() -> textArea.setText("Failed to load content from the URL: " + e.getMessage()));
            }
        }).start();
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
        close();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        close();
    }

    @FXML
    public void executeUpdateAction(ActionEvent actionEvent) {
        UpdaterProgressController.performUpdate(release, themeCssUrl);
        close();
    }
}
