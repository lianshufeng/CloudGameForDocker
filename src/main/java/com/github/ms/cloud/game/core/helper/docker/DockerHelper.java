package com.github.ms.cloud.game.core.helper.docker;

import com.github.microservice.core.util.JsonUtil;
import com.github.microservice.core.util.os.SystemUtil;
import com.github.ms.cloud.game.core.conf.CloudGameConf;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DockerHelper {

    private static final String DockerSock = "/var/run/docker.sock";

    @Autowired
    private CloudGameConf gameConf;


    @SneakyThrows
    public Map<String, Object> imagesJson(String name) {
        String ret = _get(String.format("images/%s/json", name));
        return JsonUtil.toObject(ret, Map.class);
    }

    @SneakyThrows
    public Map<String, Object> imagesCreate(String name) {
        String ret = _post(String.format("images/create?fromImage=%s", name), Map.of());
        return JsonUtil.toObject(ret, Map.class);
    }

    /**
     * 创建容器
     *
     * @return
     */
    @SneakyThrows
    public CreateCommand.Ret containersCreate(CreateCommand runCommand, String name) {
        String url = "containers/create" + (StringUtils.hasText(name) ? "?name=" + name : "");
        String result = _post(url, runCommand);
        return JsonUtil.toObject(result, CreateCommand.Ret.class);
    }


    @SneakyThrows
    public void containersStart(StartCommand startCommand) {
        _post(String.format("containers/%s/start", startCommand.getId()), Map.of());
    }

    @SneakyThrows
    public List<Map<String, Object>> containersList(String filters) {
        String ret = _get("containers/json" + (StringUtils.hasText(filters) ? "?filters=" + filters : ""));
        return (List<Map<String, Object>>) JsonUtil.toObject(ret, Object.class);
    }

    @SneakyThrows
    public Map<String, Object> containersKill(String id) {
        String ret = _post("containers/%s/kill", id);
        if (!StringUtils.hasText(ret)) {
            return Map.of();
        }
        return (Map<String, Object>) JsonUtil.toObject(ret, Object.class);
    }

    @SneakyThrows
    public Map<String, Object> containersStop(String id) {
        String ret = _post("containers/%s/stop", id);
        if (!StringUtils.hasText(ret)) {
            return Map.of();
        }
        return (Map<String, Object>) JsonUtil.toObject(ret, Object.class);
    }

    @SneakyThrows
    public Map<String, Object> containersDelete(String id, boolean force) {
        String ret = _delete(String.format("containers/%s?force=" + force, id));
        if (!StringUtils.hasText(ret)) {
            return Map.of();
        }
        return (Map<String, Object>) JsonUtil.toObject(ret, Object.class);
    }


    private String _delete(String url) {
        String cmd = String.format("curl --unix-socket %s -X DELETE http://localhost/%s", DockerSock,// docker.sock
                url // 路径
        );
        return _runCmd(cmd);
    }

    private String _get(String url) {
        String cmd = String.format("curl --unix-socket %s -X GET http://localhost/%s", DockerSock,// docker.sock
                url // 路径
        );
        return _runCmd(cmd);
    }

    private String _post(String url, Object parameter) {
        String cmd = String.format("curl --unix-socket %s -H \"Content-Type: application/json\" -d '%s'  -X POST http://localhost/%s", DockerSock,// docker.sock
                JsonUtil.toJson(parameter),//参数
                url // 路径
        );
        return _runCmd(cmd);
    }

    @SneakyThrows
    private String _runCmd(String cmd) {
        log.info("run : {}", cmd);
        @Cleanup("delete") File tempFile = File.createTempFile("cmd_", ".sh");
        FileUtils.writeStringToFile(tempFile, cmd, StandardCharsets.UTF_8);
        Process pr = Runtime.getRuntime().exec(new String[]{"bash", tempFile.getAbsolutePath()});
        pr.waitFor(5, TimeUnit.SECONDS);
        @Cleanup InputStream inputStream = pr.getInputStream();
        String ret = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));
        log.info("ret : {}", ret);
        return ret;
    }


}
