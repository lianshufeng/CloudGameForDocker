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
public class StartCommand {

//    https://docs.docker.com/engine/api/v1.45/#tag/Container/operation/ContainerStart

    private String id;



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