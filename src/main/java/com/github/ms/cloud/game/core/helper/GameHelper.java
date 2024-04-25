package com.github.ms.cloud.game.core.helper;

import com.github.microservice.core.util.JsonUtil;
import com.github.microservice.core.util.path.PathUtil;
import com.github.ms.cloud.game.core.model.GameItemModel;
import com.github.ms.cloud.game.core.model.GameModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@EnableScheduling
public class GameHelper implements ApplicationRunner {

    @Autowired
    private DiskHelper diskHelper;


    @SneakyThrows
    public GameModel load() {
        String json = FileUtils.readFileToString(diskHelper.getGameConfPath(), "UTF-8");
        return JsonUtil.toObject(json, GameModel.class);
    }

    @SneakyThrows
    private void _store(GameModel gameModel) {
////        GameModel store = new GameModel();
////        BeanMap.create(store).putAll(JsonUtil.loadToObject(JsonUtil.toJson(gameModel), Object.class));
//        GameModel store = JsonUtil.loadToObject(JsonUtil.toJson(gameModel), GameModel.class);
//        store.getItems().stream().forEach((it)->{
//            it.setPath(it.getPath().replaceAll("\\\\","/"));
//        });
        FileUtils.writeStringToFile(diskHelper.getGameConfPath(), JsonUtil.toJson(gameModel, true));
    }


    @Scheduled(fixedRate = 30000)
    private void scanGame() {
        final GameModel gameModel = diskHelper.getGameConfPath().exists() ? load() : new GameModel();

        final File gameFle = diskHelper.getGamePath();
        Collection<File> games = FileUtils.listFiles(gameFle, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        }, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        });


        //游戏缓存路径
        final Set<String> gameCachePath = gameModel.getItems().stream().map(it -> it.getPath()).collect(Collectors.toSet());

        //磁盘扫描的路径
        Set<String> diskGamePaths = games.stream().map(it -> {
            String path = it.getAbsolutePath();
            return formatPath(path.substring(gameFle.getAbsolutePath().length(), it.getPath().length()));
        }).collect(Collectors.toSet());


        long lastUpdateTime = gameModel.getUpdateTime();
        diskGamePaths.stream().filter(it -> !gameCachePath.contains(it))
                .forEach((it) -> {
            final File file = new File(diskHelper.getGamePath().getAbsolutePath() + "/" + it);
            gameModel.getItems().add(GameItemModel.builder().path(it).length(file.length()).name(file.getName()).build());
            gameModel.setUpdateTime(System.currentTimeMillis());
        });


        if (gameModel.getUpdateTime() != lastUpdateTime) {
            log.info("store : {}", gameModel.getItems().size());
            _store(gameModel);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }


    private static String formatPath(String path) {
        String ret = path;
        while (ret.indexOf("\\") > -1) {
            ret = ret.replaceAll("\\\\", "/");
        }
        while (ret.indexOf("//") > -1) {
            ret = ret.replaceAll("//", "/");
        }
        return ret;
    }

}
