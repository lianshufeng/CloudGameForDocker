package com.github.ms.cloud.game.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameItemModel {

    //游戏名称
    private String name;

    //路径
    private String path;

    //长度
    private long length;

}
