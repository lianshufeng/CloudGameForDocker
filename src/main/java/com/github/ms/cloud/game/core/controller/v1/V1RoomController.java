package com.github.ms.cloud.game.core.controller.v1;

import com.github.microservice.core.util.JsonUtil;
import com.github.microservice.core.util.net.apache.UrlEncodeUtil;
import com.github.microservice.core.util.random.RandomUtil;
import com.github.microservice.core.util.script.GroovyUtil;
import com.github.microservice.core.util.spring.SpringELUtil;
import com.github.microservice.core.util.token.TokenUtil;
import com.github.ms.cloud.game.core.conf.CloudGameConf;
import com.github.ms.cloud.game.core.controller.TokenValidateHelper;
import com.github.ms.cloud.game.core.helper.DiskHelper;
import com.github.ms.cloud.game.core.helper.docker.CreateCommand;
import com.github.ms.cloud.game.core.helper.docker.DockerHelper;
import com.github.ms.cloud.game.core.helper.docker.StartCommand;
import com.github.ms.cloud.game.core.ret.ResultContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("v1/room")
public class V1RoomController  {

    @Autowired
    @Delegate
    private TokenValidateHelper tokenValidateHelper;

    @Autowired
    private DockerHelper dockerHelper;
    @Autowired
    private CloudGameConf cloudGameConf;
    @Autowired
    private ConfigurationPropertiesAutoConfiguration configurationPropertiesAutoConfiguration;
    @Autowired
    private DiskHelper diskHelper;


    @SneakyThrows
    @Operation(summary = "获取所有的房间列表", description = "获取所有的房间列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResultContent<List<Map<String, Object>>> list() {
        this.auth();
        return ResultContent.buildContent(listRooms());
    }

    @SneakyThrows
    @Operation(summary = "删除房间", description = "删除房间")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResultContent<Object> delete(@Schema(name = "id", example = "c98a61b2fc3,c98a61b2fc2", description = "房间id") @RequestParam(value = "id") String[] id) {
        this.auth();

        Map<String, Object> ret = new HashMap<>();
        for (String i : id) {
            ret.put(i, this.dockerHelper.containersDelete(i, true));
        }
        return ResultContent.buildContent(ret);
    }


    @SneakyThrows
    @Operation(summary = "删除所有房间", description = "删除所有房间")
    @RequestMapping(value = "deleteAll", method = RequestMethod.POST)
    public ResultContent<Object> deleteAll() {
        this.auth();
        List<Map<String, Object>> items = this.listRooms();
        List<String> ret = new ArrayList<>();
        items.forEach(it -> {
            String id = String.valueOf(it.get("id"));
            ret.add(id);
            this.dockerHelper.containersDelete(id, true);
        });
        return ResultContent.buildContent(ret);
    }


    @SneakyThrows
    @Operation(summary = "创建房间", description = "创建房间")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResultContent<Object> create(@Schema(name = "gamePath", example = "/dino.zip,/other/madcell.sfc", description = "游戏路径") @RequestParam(value = "gamePath") String[] gamePath) {
        this.auth();

        //随机端口
        final int port = RandomUtil.nextInt(cloudGameConf.getDocker().getMinPort(), cloudGameConf.getDocker().getMaxPort());

        final String uuid = TokenUtil.create();

        final String RoomNameTemplate = this.cloudGameConf.getDocker().getContainerPreName() + "-%s-" + uuid;
        final String gameName = String.format(RoomNameTemplate, "game");
        final String nginxName = String.format(RoomNameTemplate, "nginx");


        // game
        final CreateCommand gameRunCommand = JsonUtil.toObject(JsonUtil.toJson(cloudGameConf.getDocker().getCloudGameTemplate()), CreateCommand.class);

        List<String> gameMounts = new ArrayList<>() {{
            add(cloudGameConf.getStoreDir() + "/config.yaml:/usr/local/share/cloud-game/configs/config.yaml");
            add(cloudGameConf.getStoreDir() + "/cores:/usr/local/share/cloud-game/assets/cores");
            add(cloudGameConf.getStoreDir() + "/certs:/certs");
            add(cloudGameConf.getStoreDir() + "/web:/usr/local/share/cloud-game/web");
        }};


        final String[] binds = ArrayUtils.addAll(gameMounts.toArray(String[]::new), Arrays.stream(gamePath).map(it -> {
            return this.cloudGameConf.getStoreDir() + "/games/" + it + ":/usr/local/share/cloud-game/assets/games/" + it;
        }).toArray(String[]::new));

        //挂载模拟器和游戏
        gameRunCommand.getHostConfig().setBinds(binds);


        //端口
//        gameRunCommand.setExposedPorts(new HashMap<>() {{
//            put("8000/tcp", Map.of());
//        }});
//        gameRunCommand.getHostConfig().setPortBindings(
//                new HashMap<>() {
//                    {
//                        put("8000/tcp", new Object[]{Map.of("HostIp", "0.0.0.0", "HostPort", "8000")});
//                    }
//                }
//        );
        //创建容器并启动容器
        CreateCommand.Ret createGameRet = this.dockerHelper.containersCreate(gameRunCommand, gameName);
        this.dockerHelper.containersStart(StartCommand.builder().id(createGameRet.getId()).build());


        // nginx
        final CreateCommand nginxRunCommand = JsonUtil.toObject(JsonUtil.toJson(cloudGameConf.getDocker().getNginxTemplate()), CreateCommand.class);
        //模板文件
        String sslText = GroovyUtil.textTemplate(new HashMap<>() {{
            put("port", port);
            put("containerName", gameName);
            put("ssl", cloudGameConf.getSsl());
        }}, this.cloudGameConf.getDocker().getNginxSSLTemplate());
        File nginxConfFile = new File(diskHelper.getNginxConPath().getAbsolutePath() + "/game-" + uuid + ".conf");
        FileUtils.writeStringToFile(nginxConfFile, sslText);
        nginxRunCommand.getHostConfig().setBinds(new String[]{this.cloudGameConf.getStoreDir() + "/certs:/certs", this.cloudGameConf.getStoreDir() + "/nginx.conf/" + nginxConfFile.getName() + ":/etc/nginx/conf.d/" + cloudGameConf.getSsl().getDomain() + ".conf"});
        nginxRunCommand.setExposedPorts(new HashMap<>() {{
            put(port + "/tcp", Map.of());
        }});
        nginxRunCommand.getHostConfig().setPortBindings(new HashMap<>() {
            {
                put(port + "/tcp", new Object[]{Map.of("HostIp", "0.0.0.0", "HostPort", String.valueOf(port))});
            }
        });

        //link
        nginxRunCommand.getHostConfig().setLinks(new String[]{gameName});


        //创建容器并启动容器
        CreateCommand.Ret createNginxRet = this.dockerHelper.containersCreate(nginxRunCommand, nginxName);
        this.dockerHelper.containersStart(StartCommand.builder().id(createNginxRet.getId()).build());

        return ResultContent.buildContent(new HashMap<>() {{
            put("port", port);
            put("room", new HashMap<>() {{
                put(gameName, Map.of("id", createGameRet.getId()));
                put(nginxName, Map.of("id", createNginxRet.getId()));
            }});
        }});
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
        return ret.stream().map(it -> Map.of("id", it.get("Id"), "names", it.get("Names"))).collect(Collectors.toList());
    }


}
