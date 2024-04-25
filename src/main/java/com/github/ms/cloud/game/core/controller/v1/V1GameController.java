package com.github.ms.cloud.game.core.controller.v1;

import com.github.ms.cloud.game.core.controller.TokenValidateHelper;
import com.github.ms.cloud.game.core.helper.DiskHelper;
import com.github.ms.cloud.game.core.helper.GameHelper;
import com.github.ms.cloud.game.core.model.GameModel;
import com.github.ms.cloud.game.core.ret.ResultContent;
import io.swagger.v3.oas.annotations.Operation;
import lombok.experimental.Delegate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Collection;

@RestController
@RequestMapping("v1/game")
public class V1GameController {

    @Autowired
    @Delegate
    private TokenValidateHelper tokenValidateHelper;


    @Autowired
    private GameHelper gameHelper;


    /**
     * 获取所有列表
     *
     * @return
     */

    @RequestMapping(value = "list", method = RequestMethod.GET)
    @Operation(summary = "获取所有游戏", description = "获取所有游戏")
    public ResultContent<GameModel[]> list() {
        this.auth();
        return ResultContent.buildContent(this.gameHelper.load());
    }


}
