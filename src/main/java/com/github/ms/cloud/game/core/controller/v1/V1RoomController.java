package com.github.ms.cloud.game.core.controller.v1;

import com.github.microservice.core.util.JsonUtil;
import com.github.microservice.core.util.net.apache.UrlEncodeUtil;
import com.github.microservice.core.util.random.RandomUtil;
import com.github.microservice.core.util.token.TokenUtil;
import com.github.ms.cloud.game.core.conf.CloudGameConf;
import com.github.ms.cloud.game.core.controller.TokenValidateHelper;
import com.github.ms.cloud.game.core.helper.docker.CreateCommand;
import com.github.ms.cloud.game.core.helper.docker.DockerHelper;
import com.github.ms.cloud.game.core.helper.docker.StartCommand;
import com.github.ms.cloud.game.core.ret.ResultContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("v1/room")
public class V1RoomController {

    @Autowired
    @Delegate
    private TokenValidateHelper tokenValidateHelper;

    @Autowired
    private DockerHelper dockerHelper;
    @Autowired
    private CloudGameConf cloudGameConf;


    @SneakyThrows
    @Operation(summary = "获取所有的房间列表", description = "获取所有的房间列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ResultContent<List<Map<String, Object>>> list() {
        this.auth();
        return ResultContent.buildContent(listRooms());
    }

    @SneakyThrows
    @Operation(summary = "删除房间", description = "删除房间")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResultContent<Object> delete(
            @Schema(name = "id", example = "c98a61b2fc32b5070bfb9b03db1cc5b9bef0163264f941710bb5e81598764bf3", description = "房间id") @RequestParam(value = "id") String id
    ) {
        this.auth();
        return ResultContent.buildContent(this.dockerHelper.containersDelete(id, true));
    }


    @SneakyThrows
    @Operation(summary = "创建房间", description = "创建房间")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResultContent<Object> create(
            @Schema(name = "gamePath", example = "/dino.zip", description = "游戏路径") @RequestParam(value = "gamePath") String gamePath
    ) {
        this.auth();
        //通过模板拷贝
        CreateCommand dockerRunCommand = JsonUtil.toObject(JsonUtil.toJson(cloudGameConf.getDocker().getContainerTemplate()), CreateCommand.class);
        BeanUtils.copyProperties(cloudGameConf.getDocker().getContainerTemplate(), dockerRunCommand);


        //挂载模拟器和游戏
        dockerRunCommand.getHostConfig().setBinds(new String[]{
                this.cloudGameConf.getWorkspacePath() + "/store/config.yaml:/usr/local/share/cloud-game/configs/config.yaml",
                this.cloudGameConf.getWorkspacePath() + "/store/cores:/usr/local/share/cloud-game/assets/cores",
                this.cloudGameConf.getWorkspacePath() + "/store/games" + gamePath + ":/usr/local/share/cloud-game/assets/games/" + gamePath
        });

        final int port = RandomUtil.nextInt(20000, 60000);

        dockerRunCommand.setExposedPorts(new HashMap<>() {{
            put("8000/tcp", Map.of());
//            put("8018/tcp", Map.of());
//            put("8443/udp", Map.of());
        }});


        //端口
        dockerRunCommand.getHostConfig().setPortBindings(
                new HashMap<>() {
                    {
                        put("8000/tcp", new Object[]{Map.of("HostIp", "0.0.0.0", "HostPort", String.valueOf(port))});
//                        put("8018/tcp", new Object[]{Map.of("HostIp", "0.0.0.0", "HostPort", "8018")});
//                        put("8443/udp", new Object[]{Map.of("HostIp", "0.0.0.0", "HostPort", "8443")});
                    }
                }
        );


        //容器名
        final String containerName = this.cloudGameConf.getDocker().getContainerPreName() + gamePath.replaceAll("/", "_") + "-" + TokenUtil.create();

        //创建容器
        CreateCommand.Ret createRet = this.dockerHelper.containersCreate(dockerRunCommand, containerName);
        //启动容器
        this.dockerHelper.containersStart(StartCommand.builder().id(createRet.getId()).build());
        return ResultContent.buildContent(Map.of(
                "id", createRet.getId(),
                "name", containerName,
                "port", port
        ));
    }


    @Autowired
    private void initCloseRoom(ApplicationContext applicationContext) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.listRooms().forEach(it -> {
                this.dockerHelper.containersDelete(String.valueOf(it.get("id")), true);
            });
        }));
    }


    /**
     * 取出所有的房间列表
     *
     * @return
     */
    private List<Map<String, Object>> listRooms() {
        String filters = JsonUtil.toJson(Map.of("name", new String[]{cloudGameConf.getDocker().getContainerPreName()}));
        List<Map<String, Object>> ret = this.dockerHelper.containersList(UrlEncodeUtil.encode(filters));
        return ret.stream()
                .map(it -> Map.of(
                                "id", it.get("Id"),
                                "names", it.get("Names")
                        )
                ).collect(Collectors.toList());
    }


}
