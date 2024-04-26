package com.github.ms.cloud.game.core.conf;

import com.github.ms.cloud.game.core.helper.docker.CreateCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "cloud.game")
public class CloudGameConf {

    //接口令牌
    private String token;

    //游戏文件名
    private String gameConfPath = "game.json";

    //路径
    private String storePath = "store";

    //运行目录
    private String workspacePath;

    //docker配置
    private Docker docker = new Docker();

    //ssl 配置
    private SSL ssl;

    //取出StoreFile
    public String getStoreDir() {
        return workspacePath + File.separator + storePath;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SSL {

        private boolean enabled;

        //域名
        private String domain;

        //ssl地址
        private String certificate;

        //ssl密钥
        private String certificateKey;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Docker {

        //镜像前缀
        private String containerPreName = "CloudGame";

        //最小端口
        private int minPort = 20000;

        //最大端口
        private int maxPort = 60000;


        //容器的模板
        private CreateCommand nginxTemplate = new CreateCommand() {{
            setImage("nginx:latest");
        }};

        private String nginxSSLTemplate = """
                server {
                    listen ${port} ssl;  # 1.1版本后这样写
                    server_name ${ssl.domain}; #填写绑定证书的域名
                    ssl_certificate ${ssl.certificate};  # 指定证书的位置，绝对路径
                    ssl_certificate_key ${ssl.certificateKey};  # 绝对路径，同上
                    ssl_session_timeout 5m;
                    ssl_protocols TLSv1 TLSv1.1 TLSv1.2; #按照这个协议配置
                    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;#按照这个套件配置
                    ssl_prefer_server_ciphers on;
                    
                    location / {
                        proxy_pass http://${containerName}:8000;
                        proxy_redirect default ;
                        proxy_set_header Host \\$host;
                        proxy_set_header X-Forwarded-For \\$remote_addr;
                        proxy_set_header X-Forwarded-Host \\$server_name;
                        proxy_set_header X-Real-IP \\$remote_addr;
                        client_max_body_size 2048m;
                        
                        #websocket
                        proxy_http_version 1.1;
                        proxy_set_header Upgrade \\$http_upgrade;
                        proxy_set_header Connection "upgrade";
                    }
                }
                                                """;


        //容器的模板
        private CreateCommand cloudGameTemplate = new CreateCommand() {{
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
