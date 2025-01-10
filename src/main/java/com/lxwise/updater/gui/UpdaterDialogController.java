package com.lxwise.updater.gui;

import com.lxwise.updater.model.ReleaseInfoModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.net.URL;
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
    public WebView versionChangeView;

    private ReleaseInfoModel release;
    private Integer currentReleaseId;
    private String currentVersion;
    private Integer currentLicenseVersion;
    private URL themeCssUrl;

    /**
     * 显示更新弹窗
     * @param release
     * @param releaseId
     * @param version
     * @param licenseVersion
     * @param themeCssUrl
     */
    public static void showUpdateDialog(ReleaseInfoModel release, Integer releaseId, String version, int licenseVersion, URL themeCssUrl) {
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
            controller.initialize();

            Scene scene = new Scene(page);
            if (themeCssUrl != null) {
                scene.getStylesheets().add(themeCssUrl.toExternalForm());
            }

            final Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(release.getAppInfo().getName()+i18nBundle.getString("infotext.title"));
            if(Objects.nonNull(release.getAppInfo().getIcon())){
                stage.getIcons().add(new Image(release.getAppInfo().getIcon().toExternalForm()));
            }else {
                stage.getIcons().add(new Image("images/fx-updater-logo.png"));
            }
            stage.show();
            stage.toFront();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void initialize() {
        URL changelog = release.getAppInfo().getChangelog();
        if (changelog != null) {
            WebEngine engine = versionChangeView.getEngine();
            String finalURL = String.format("%s?from=%d&to=%d", changelog, currentReleaseId, release.getId());
            engine.load(finalURL);
        } else {
            versionChangeView.setVisible(false);
            versionChangeView.setManaged(false);
        }

        Object[] messageArguments = { release.getAppInfo().getName(), currentVersion, release.getVersion() };
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(resources.getLocale());
        formatter.applyPattern(resources.getString("infotext.freeUpgrade"));
        versionInfoLabel.setText(formatter.format(messageArguments));
        versionInfoLabel.autosize();
    }

    private void close() {
        ((Stage) versionInfoLabel.getScene().getWindow()).close();
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
