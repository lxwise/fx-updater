package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.service.ExecuteInstallerService;
import com.lxwise.updater.service.InstallFileDownloadService;
import com.lxwise.updater.utils.UpdateLogger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新进度条控制器
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class UpdaterProgressController {

    @FXML
    private ResourceBundle resources;
    @FXML
    public Label updateInfoLabel;
    @FXML
    public ProgressBar updateProgressBar;
    @FXML
    public Label updateProgressLabel;
    @FXML
    public Button actionButton;
    @FXML
    public Button manualDownloadButton;


    private ReleaseInfoModel release;
    private InstallFileDownloadService downloadService;

    // 初始化方法，设置下载任务和监听器
    private void initialize() {
        updateInfoLabel.setText(resources.getString("label.downloading"));
        downloadService = new InstallFileDownloadService(release);

        // 更新进度条和标签
        downloadService.workDoneProperty().addListener((observable, oldValue, newValue) -> {
            updateProgressBar.setProgress(downloadService.getWorkDone() / downloadService.getTotalWork());
            updateProgressLabel();
        });

        // 下载完成后，设置安装按钮和信息
        downloadService.setOnSucceeded((event) -> Platform.runLater(() -> {
            UpdateLogger.info("Download completed successfully");
            setupInstallButton();
            updateInfoLabel.setText(resources.getString("label.downloaded"));
        }));

        // 下载失败后，显示错误信息
        downloadService.setOnFailed((event) -> Platform.runLater(() -> {
            UpdateLogger.error("Download failed", downloadService.getException());
            setupCancelButton();
            updateInfoLabel.setText(resources.getString("label.downloadFailed"));
        }));

        downloadService.start();
    }

    // 设置安装按钮的行为
    private void setupInstallButton() {
        actionButton.setDefaultButton(true);
        actionButton.setOnAction(this::install);
        actionButton.setText(resources.getString("button.install"));
        actionButton.autosize();
    }

    // 设置取消按钮的行为
    private void setupCancelButton() {
        actionButton.setDefaultButton(true);
        actionButton.setOnAction(event -> close());
        actionButton.setText(resources.getString("button.cancel"));
        actionButton.autosize();

        manualDownloadButton.setVisible(true); // 显示手动下载按钮
    }

    // 执行更新过程
    public static void performUpdate(ReleaseInfoModel release, String themeCssUrl) {
        try {
            UpdateLogger.info("Opening progress window for update");

            ResourceBundle i18nBundle = ResourceBundle.getBundle("com.lxwise.updater.i18n.updater");

            FXMLLoader loader = new FXMLLoader(UpdaterProgressController.class.getResource("UpdaterProgress.fxml"), i18nBundle);
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent page = loader.load();
            UpdaterProgressController controller = loader.getController();
            controller.release = release;
            controller.initialize();

            Scene scene = new Scene(page);
            if (themeCssUrl != null) {
                scene.getStylesheets().add(themeCssUrl);
            }

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(GuiUtils.buildStageTitle(release, i18nBundle));
            GuiUtils.setStageIcon(release, stage);
            stage.show();
            stage.setResizable(false);
            stage.toFront();
            GuiUtils.setupCloseConfirmation(stage, i18nBundle, null);

        } catch (Throwable ex) {
            UpdateLogger.error("Failed to show progress window", ex);
        }
    }

    // 更新进度标签
    private void updateProgressLabel() {
        MessageFormat mf = new MessageFormat(resources.getString("label.progress"), resources.getLocale());
        Object[] params = {GuiUtils.formatFileSize(downloadService.getWorkDone()), GuiUtils.formatFileSize(downloadService.getTotalWork())};
        updateProgressLabel.setText(mf.format(params));
    }

    // 关闭窗口
    public void close() {
        ((Stage) updateProgressLabel.getScene().getWindow()).close();
    }

    @FXML
    public void executeAction(ActionEvent actionEvent) {
        UpdateLogger.info("User cancelled download");
        downloadService.cancel();
        close();
    }

    @FXML
    public void install(ActionEvent installEvent) {
        actionButton.setDisable(true);
        updateProgressBar.setProgress(-1.0);
        updateProgressLabel.setText("");
        updateInfoLabel.setText(resources.getString("label.installing"));

        UpdateLogger.info("Starting installation: %s", downloadService.getValue());
        ExecuteInstallerService installService = new ExecuteInstallerService(downloadService.getValue());

        installService.setOnFailed((evt) -> Platform.runLater(() -> {
            UpdateLogger.error("Installation failed", installService.getException());
            actionButton.setDisable(false);
            setupCancelButton();
            updateProgressBar.setProgress(1.0);
            updateInfoLabel.setText(resources.getString("label.installFailed"));
        }));

        installService.start();
    }

    @FXML
    public void manualDownload(ActionEvent event) {
        try {
            String address = release.getOfficialDownloadAddress();
            if (address != null && !address.isBlank()) {
                URL downloadUrl = new URL(address);
                java.awt.Desktop.getDesktop().browse(downloadUrl.toURI());
                UpdateLogger.info("Opening manual download URL: %s", address);
            }
            close();
        } catch (Exception e) {
            UpdateLogger.error("Failed to open manual download URL", e);
        }
    }
}
