package com.github.ms.cloud.game.core.helper.docker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCommand {

    // https://docs.docker.com/engine/api/v1.45/#tag/Container/operation/ContainerCreate

    @JsonProperty("Image")
    private String Image;

    @JsonProperty("Cmd")
    private String[] Cmd;

    @JsonProperty("Env")
    private String[] Env;

    @JsonProperty("Volumes")
    private Map<String, Object> Volumes;

    @JsonProperty("ExposedPorts")
    private Map<String, Object> ExposedPorts;

    @JsonProperty("HostConfig")
    private HostConfig HostConfig = new HostConfig();





    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HostConfig {

        @JsonProperty("AutoRemove")
        private Boolean AutoRemove;

        //磁盘挂载
        @JsonProperty("Binds")
        private String[] Binds;

        //端口
        @JsonProperty("PortBindings")
        private Map<String, Object[]> PortBindings;

    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ret {

        //id
        @JsonProperty("Id")
        private String Id;

    }

}