package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import com.lxwise.updater.service.ExecuteInstallerService;
import com.lxwise.updater.service.InstallFileDownloadService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 更新进度条控制器
 * @version: 1.0
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
            setupInstallButton();
            updateInfoLabel.setText(resources.getString("label.downloaded"));
        }));

        // 下载失败后，显示错误信息
        downloadService.setOnFailed((event) -> Platform.runLater(() -> {
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
    }

    // 执行更新过程
    public static void performUpdate(ReleaseInfoModel release, URL themeCssUrl) {
        try {
            ResourceBundle i18nBundle = ResourceBundle.getBundle("com.lxwise.updater.i18n.updater");

            FXMLLoader loader = new FXMLLoader(UpdaterProgressController.class.getResource("UpdaterProgress.fxml"), i18nBundle);
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent page = loader.load();
            UpdaterProgressController controller = loader.getController();
            controller.release = release;
            controller.initialize();

            Scene scene = new Scene(page);
            if (themeCssUrl != null) {
                scene.getStylesheets().add(themeCssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(release.getAppInfo().getName()+i18nBundle.getString("infotext.title"));
            setStageIcon(release, stage);
            stage.show();
            stage.setResizable(false);
            stage.toFront();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    // 设置窗口图标
    private static void setStageIcon(ReleaseInfoModel release, Stage stage) {
        if (Objects.nonNull(release.getAppInfo().getIcon())) {
            stage.getIcons().add(new Image(release.getAppInfo().getIcon().toExternalForm()));
        } else {
            stage.getIcons().add(new Image("images/fx-updater-logo.png"));
        }
    }

    // 更新进度标签
    private void updateProgressLabel() {
        MessageFormat mf = new MessageFormat(resources.getString("label.progress"), resources.getLocale());
        Object[] params = {formatFileSize(downloadService.getWorkDone()), formatFileSize(downloadService.getTotalWork())};

        updateProgressLabel.setText(mf.format(params));
    }

    // 字节转化可读的格式
    private String formatFileSize(double fileSizeInBytes) {
        final double BYTE_UNIT = 1024.0; // 单位为 1024 字节

        // 如果文件大小小于 1 KB，直接返回字节数
        if (fileSizeInBytes < BYTE_UNIT) {
            return String.format("%.0f B", fileSizeInBytes); // 返回以字节为单位的大小
        }

        // 计算文件大小对应的单位级别 (kB, MB, GB 等)
        int unitIndex = (int) (Math.log(fileSizeInBytes) / Math.log(BYTE_UNIT));
        char unitPrefix = "kMGTPE".charAt(unitIndex - 1); // 获取单位前缀 (K, M, G 等)

        // 计算转换后的文件大小，并格式化结果
        double convertedSize = fileSizeInBytes / Math.pow(BYTE_UNIT, unitIndex);
        return String.format("%.1f %sB", convertedSize, unitPrefix);
    }


    // 关闭窗口
    public void close() {
        ((Stage) updateProgressLabel.getScene().getWindow()).close();
    }

    @FXML
    public void executeAction(ActionEvent actionEvent) {
        System.out.println("执行操作");

        downloadService.cancel();
        close();
    }

    @FXML
    public void install(ActionEvent installEvent) {
        actionButton.setDisable(true);
        updateProgressBar.setProgress(-1.0);
        updateProgressLabel.setText("");
        updateInfoLabel.setText(resources.getString("label.installing"));

        ExecuteInstallerService installService = new ExecuteInstallerService(downloadService.getValue());

        installService.setOnFailed((evt) -> Platform.runLater(() -> {
            actionButton.setDisable(false);
            setupCancelButton();
            updateProgressBar.setProgress(1.0);
            updateInfoLabel.setText(resources.getString("label.installFailed"));
        }));

        installService.start();
    }
}
