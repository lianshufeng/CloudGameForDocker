package com.github.ms.cloud.game.core.init;


import com.github.ms.cloud.game.core.conf.CloudGameConf;
import com.github.ms.cloud.game.core.helper.docker.DockerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ImagePull implements ApplicationRunner {

    @Autowired
    private CloudGameConf cloudGameConf;

    @Autowired
    private DockerHelper dockerHelper;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        pull(cloudGameConf.getDocker().getNginxTemplate().getImage());
        pull(cloudGameConf.getDocker().getCloudGameTemplate().getImage());
    }

    /**
     * 更新镜像
     *
     * @param imageName
     */
    private void pull(final String imageName) {
        Map<String, Object> ret = dockerHelper.imagesJson(imageName);
        if (!ret.containsKey("Id")) {
            log.info("docker pull {}", imageName);
            dockerHelper.imagesCreate(imageName);
        }
    }
}
