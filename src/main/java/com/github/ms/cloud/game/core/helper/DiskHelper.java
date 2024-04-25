package com.github.ms.cloud.game.core.helper;

import com.github.microservice.core.helper.ApplicationHomeHelper;
import com.github.ms.cloud.game.core.conf.CloudGameConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskHelper extends ApplicationHomeHelper {

    private final static String GameStoreConfFileName = "game.json";


    @Autowired
    private CloudGameConf cloudGameConf;

    @Override
    public Class<?> getMeClass() {
        return DiskHelper.class;
    }


    /**
     * 获取游戏路径
     *
     * @return
     */
    public File getGamePath() {
        return makePath(cloudGameConf.getStorePath(), "games");
    }

    /**
     * 获取核心路径
     * @return
     */
    public File getCoresPath() {
        return makePath(cloudGameConf.getStorePath(), "cores");
    }

    public File getGameConfPath(){
        return makePath(cloudGameConf.getGameConfPath());
    }


    private File makePath(String root, String... path) {
        //绝对路径
        File file = null;
        if (root.startsWith("/")) {
            file = new File(root + "/" + String.join("/", path));
        }
        //相对路径
        file = new File(getHomeFile().getAbsolutePath() + "/" + root + "/" + String.join("/", path));
        return file;
    }

}
