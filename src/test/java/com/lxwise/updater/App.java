package com.lxwise.updater;

import com.lxwise.updater.service.FXUpdater;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 测试类主程序
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class App extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox ap = new VBox();
        ap.setAlignment(Pos.CENTER);
        ap.setSpacing(30.0);
        Label label = new Label("This is an update test for the updater");
        label.setStyle(" -fx-font-size: 14px;-fx-text-fill: #d73e39;");

        Button button = new Button("start update");

        button.setOnAction(event -> {
            FXUpdater updater;
            try {
                updater = new FXUpdater(App.class);
//                updater = new FXUpdater(new URL("http://localhost:8080/updater/updateConfig.json"), "1.0.0", 1, 1, new URL("http://localhost:8080/updater/themeCssUrl.css"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updater.checkAppUpdate();
        });
        ap.getChildren().addAll(label,button);
        Scene scene = new Scene(ap);
        primaryStage.setScene(scene);
        primaryStage.setTitle("fx-updater");
        primaryStage.getIcons().add(new Image("images/fx-updater-logo.png"));
        primaryStage.setWidth(400);
        primaryStage.setHeight(400);
        primaryStage.show();

    }
}
