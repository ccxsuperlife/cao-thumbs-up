package com.cao.thumbsup.job;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 小曹同学
 * @date 2025/5/9
 */
@SpringBootTest
class SyncThumb2DBJobTest {

    @Resource
    private SyncThumb2DBJob syncThumb2DBJob;
    @Test
    void run() {
        syncThumb2DBJob.run();
    }
}