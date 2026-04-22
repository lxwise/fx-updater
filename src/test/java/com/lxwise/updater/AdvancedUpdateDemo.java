package com.lxwise.updater;

import com.lxwise.updater.service.UpdateConfig;
import com.lxwise.updater.service.UpdateMode;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 测试Demo2 - Builder高级用法演示
 *               展示使用 UpdateConfig Builder 模式进行更新配置
 *               支持4种更新模式：交互式/静默/后台下载/仅检查
 *               支持自动重启、超时控制、重试配置、回调等高级功能
 * @version: 2.0
 * @email: lstart980@gmail.com
 */
public class AdvancedUpdateDemo extends Application {

    private TextArea logArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setSpacing(15.0);
        root.setPadding(new Insets(20));

        // 标题
        Label titleLabel = new Label("FX-Updater Builder 高级用法演示");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label descLabel = new Label("使用 UpdateConfig.fromApplication() Builder 模式");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        // === 方式1：一行式极简调用 ===
        Button btn1 = new Button("★ 一行式极简调用（推荐）");
        btn1.setPrefWidth(350);
        btn1.setStyle("-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btn1.setOnAction(event -> {
            try {
                // 最简用法：仅一行代码即可完成更新检查！
                UpdateConfig.fromApplication(AdvancedUpdateDemo.class).checkUpdate();
                appendLog("[一行式] 更新检查已启动");
            } catch (IOException e) {
                appendLog("[错误] " + e.getMessage());
            }
        });

        // === 方式2：交互式更新 + 回调 + 自动关闭 ===
        Button btn2 = new Button("交互式更新（带回调 + 8秒自动关闭）");
        btn2.setPrefWidth(350);
        btn2.setOnAction(event -> {
            try {
                UpdateConfig.fromApplication(AdvancedUpdateDemo.class)
                        .updateMode(UpdateMode.INTERACTIVE)
                        .autoCloseSeconds(8)
                        .callback(() -> appendLog("[回调] 交互式更新弹窗已关闭"))
                        .checkUpdate();
                appendLog("[交互式] 更新检查已启动，8秒后自动关闭");
            } catch (IOException e) {
                appendLog("[错误] " + e.getMessage());
            }
        });

        // === 方式3：静默更新模式 ===
        Button btn3 = new Button("静默更新（后台自动下载+安装，无UI）");
        btn3.setPrefWidth(350);
        btn3.setOnAction(event -> {
            try {
                UpdateConfig.fromApplication(AdvancedUpdateDemo.class)
                        .updateMode(UpdateMode.SILENT)
                        .autoRestart(true)
                        .maxRetries(5)
                        .connectTimeout(10000)
                        .readTimeout(60000)
                        .callback(() -> appendLog("[回调] 静默更新完成"))
                        .checkUpdate();
                appendLog("[静默] 后台静默更新已启动（5次重试，自动重启）");
            } catch (IOException e) {
                appendLog("[错误] " + e.getMessage());
            }
        });

        // === 方式4：后台下载模式 ===
        Button btn4 = new Button("后台下载（下载完成后弹出安装提示）");
        btn4.setPrefWidth(350);
        btn4.setOnAction(event -> {
            try {
                UpdateConfig.fromApplication(AdvancedUpdateDemo.class)
                        .updateMode(UpdateMode.BACKGROUND_DOWNLOAD)
                        .callback(() -> appendLog("[回调] 后台下载完成"))
                        .checkUpdate();
                appendLog("[后台下载] 后台下载模式已启动");
            } catch (IOException e) {
                appendLog("[错误] " + e.getMessage());
            }
        });

        // === 方式5：仅检查模式 ===
        Button btn5 = new Button("仅检查更新（不下载不安装）");
        btn5.setPrefWidth(350);
        btn5.setOnAction(event -> {
            try {
                UpdateConfig.fromApplication(AdvancedUpdateDemo.class)
                        .updateMode(UpdateMode.CHECK_ONLY)
                        .callback(() -> appendLog("[回调] 检查完成：有可用更新"))
                        .checkUpdate();
                appendLog("[仅检查] 正在检查是否有可用更新...");
            } catch (IOException e) {
                appendLog("[错误] " + e.getMessage());
            }
        });

        // === 方式6：纯Builder手动配置（不读取app.properties） ===
        Button btn6 = new Button("纯Builder手动配置（不读取配置文件）");
        btn6.setPrefWidth(350);
        btn6.setOnAction(event -> {
            UpdateConfig.builder()
                    .configUrl("http://192.168.12.53:81/downloads/app-update-config.json")
                    .version("1.0.0")
                    .releaseId(10000)
                    .licenseVersion(1)
                    .updateMode(UpdateMode.INTERACTIVE)
                    .connectTimeout(15000)
                    .readTimeout(30000)
                    .maxRetries(3)
                    .callback(() -> appendLog("[回调] 手动配置更新完成"))
                    .checkUpdate();
            appendLog("[手动配置] 使用纯Builder方式启动更新检查");
        });

        // 日志区域
        Label logLabel = new Label("操作日志：");
        logLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setStyle("-fx-font-size: 11px; -fx-font-family: 'Consolas';");

        HBox logHeader = new HBox(10);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        Button clearBtn = new Button("清除日志");
        clearBtn.setOnAction(e -> logArea.clear());
        logHeader.getChildren().addAll(logLabel, clearBtn);

        root.getChildren().addAll(
                titleLabel, descLabel,
                btn1, btn2, btn3, btn4, btn5, btn6,
                logHeader, logArea
        );

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("FX-Updater Demo2 - Builder高级用法");
        primaryStage.getIcons().add(new Image("images/fx-updater-logo.png"));
        primaryStage.setWidth(500);
        primaryStage.setHeight(650);
        primaryStage.show();

        appendLog("应用已启动，请选择更新方式进行测试");
    }

    private void appendLog(String message) {
        javafx.application.Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }
}
