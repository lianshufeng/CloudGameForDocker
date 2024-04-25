package com.github.ms.cloud.game.core.service;

import org.springframework.stereotype.Service;

@Service
public class TestService {

    public long time() {
        return System.currentTimeMillis();
    }


}
