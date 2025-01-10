package com.lxwise.updater.service;

import com.google.gson.Gson;
import com.lxwise.updater.model.AppInfoModel;
import com.lxwise.updater.model.ReleaseInfoModel;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 获取更新配置任务
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class AcquireUpdateConfigService extends Service<AppInfoModel> {

    private final URL updateConfigUrl;

    public AcquireUpdateConfigService(URL updateConfigUrl) {
        this.updateConfigUrl = updateConfigUrl;
    }

    @Override
    protected Task<AppInfoModel> createTask() {
        return new Task<>() {
            @Override
            protected AppInfoModel call() throws Exception {
                try {
                    // 打开连接并获取输入流
                    URLConnection connection = updateConfigUrl.openConnection();
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        Gson gson = new Gson();
                        // 从 JSON 文件解析为 Application 对象
                        AppInfoModel application = gson.fromJson(reader, AppInfoModel.class);
                        for (ReleaseInfoModel release : application.getReleases()) {
                            release.setAppInfo(application);
                        }
                        return application;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };
    }
}



