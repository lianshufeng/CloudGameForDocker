package com.github.ms.cloud.game.core.init;

import com.github.ms.cloud.game.core.conf.CloudGameConf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class NginxConfClean implements ApplicationRunner {

    @Autowired
    private CloudGameConf cloudGameConf;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        File file = new File(this.cloudGameConf.getStorePath() + "/nginx.conf/");

        if (file.exists()) {
            int fileCount = file.list().length;
            if (fileCount > 0) {
                log.info("nginx clean : {}", fileCount);
                FileUtils.cleanDirectory(file);
            }
        }
    }
}
