package com.github.ms.cloud.game.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameModel {

    //游戏
    private List<GameItemModel> items = new ArrayList<>();


    //更新时间
    private long updateTime;


}
