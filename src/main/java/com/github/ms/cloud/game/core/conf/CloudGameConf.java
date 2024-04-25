package com.github.ms.cloud.game.core.conf;

import com.github.ms.cloud.game.core.helper.docker.CreateCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "cloud.game")
public class CloudGameConf {

    //接口令牌
    private String token;

    //路径
    private String storePath = "store";

    //游戏文件名
    private String gameConfPath = "game.json";

    //运行目录
    private String workspacePath ;

    //docker配置
    private Docker docker = new Docker();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Docker {

        //api路径
        private String dockerSock = "/var/run/docker.sock";

        //api版本号
        private String apiVersion = "v1.45";


        //镜像前缀
        private String containerPreName = "CloudGame-";


        //容器的模板
        private CreateCommand containerTemplate = new CreateCommand() {{
            setImage("lianshufeng/cloud-game");
            setEnv(new String[]{
                    "DISPLAY=:99",
                    "MESA_GL_VERSION_OVERRIDE=3.3",
                    "CLOUD_GAME_WEBRTC_SINGLEPORT=8443",
                    "CLOUD_GAME_WEBRTC_ICEIPMAP=127.0.0.1",
                    "PION_LOG_TRACE=all"
            });
            setCmd(new String[]{
                    "bash", "-c", "./coordinator & ./worker"
            });
        }};


    }

}
