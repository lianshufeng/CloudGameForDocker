package com.github.ms.cloud.game.core.controller;

import com.github.ms.cloud.game.core.conf.CloudGameConf;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class TokenValidateHelper {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CloudGameConf cloudGameConf;

    /**
     * 权限校验
     */
    public void auth() {
        String token = request.getHeader("token");
        if (StringUtils.hasText(this.cloudGameConf.getToken())) {
            Assert.hasText(token, "token不能为空");
            Assert.state(this.cloudGameConf.getToken().equals(token), "token错误");
        }
    }

}
