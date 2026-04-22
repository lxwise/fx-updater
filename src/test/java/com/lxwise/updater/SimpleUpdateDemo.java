package com.lxwise.updater;

import com.lxwise.updater.service.FXUpdater;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 测试Demo1 - 经典用法演示
 *               展示使用传统 FXUpdater 构造方式进行更新检查
 *               包含三种经典调用方式：无回调、带回调、带回调+自动关闭
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class SimpleUpdateDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20.0);
        root.setPadding(new Insets(30));

        // 标题
        Label titleLabel = new Label("FX-Updater 经典用法演示");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label descLabel = new Label("使用 new FXUpdater(Class<?>) 传统构造方式");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        // 方式1：最简单的用法 - 无回调
        Button btn1 = new Button("方式1：基础更新检查（无回调）");
        btn1.setPrefWidth(300);
        btn1.setOnAction(event -> {
            try {
                // 最简用法：通过应用主类自动读取 /app.properties 和 /theme.css
                FXUpdater updater = new FXUpdater(SimpleUpdateDemo.class);
                updater.checkAppUpdate();
            } catch (IOException e) {
                showError("更新检查失败: " + e.getMessage());
            }
        });

        // 方式2：带回调的用法
        Button btn2 = new Button("方式2：带回调的更新检查");
        btn2.setPrefWidth(300);
        btn2.setOnAction(event -> {
            try {
                FXUpdater updater = new FXUpdater(SimpleUpdateDemo.class);
                updater.checkAppUpdate(() -> {
                    System.out.println("[回调] 更新提示关闭或完成后执行回调逻辑");
                });
            } catch (IOException e) {
                showError("更新检查失败: " + e.getMessage());
            }
        });

        // 方式3：带回调 + 自动关闭
        Button btn3 = new Button("方式3：带回调 + 5秒自动关闭");
        btn3.setPrefWidth(300);
        btn3.setOnAction(event -> {
            try {
                FXUpdater updater = new FXUpdater(SimpleUpdateDemo.class);
                updater.checkAppUpdate(() -> {
                    System.out.println("[回调] 弹窗已自动关闭或用户手动关闭");
                }, 5);
            } catch (IOException e) {
                showError("更新检查失败: " + e.getMessage());
            }
        });

        // 方式4：仅自动关闭，不带回调
        Button btn4 = new Button("方式4：10秒自动关闭（无回调）");
        btn4.setPrefWidth(300);
        btn4.setOnAction(event -> {
            try {
                FXUpdater updater = new FXUpdater(SimpleUpdateDemo.class);
                updater.checkAppUpdate(10);
            } catch (IOException e) {
                showError("更新检查失败: " + e.getMessage());
            }
        });

        // 方式5：使用完整参数构造
        Button btn5 = new Button("方式5：手动指定参数构造");
        btn5.setPrefWidth(300);
        btn5.setOnAction(event -> {
            FXUpdater updater = new FXUpdater(
                    "http://192.168.12.53:81/downloads/app-update-config.json",
                    "1.0.0",
                    10000,
                    1,
                    null  // 不使用自定义主题
            );
            updater.checkAppUpdate();
        });

        // 状态标签
        Label statusLabel = new Label("点击按钮开始测试不同的更新检查方式");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        root.getChildren().addAll(titleLabel, descLabel, btn1, btn2, btn3, btn4, btn5, statusLabel);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("FX-Updater Demo1 - 经典用法");
        primaryStage.getIcons().add(new Image("images/fx-updater-logo.png"));
        primaryStage.setWidth(450);
        primaryStage.setHeight(500);
        primaryStage.show();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
