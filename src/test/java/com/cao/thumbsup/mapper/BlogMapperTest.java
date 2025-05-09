package com.cao.thumbsup.mapper;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 小曹同学
 * @date 2025/5/9
 */
@SpringBootTest
class BlogMapperTest {


    @Resource
    private BlogMapper blogMapper;

    @Test
    void batchUpdateThumbCount() {
        Map<Long, Long> countMap = new HashMap<>();
        countMap.put(1L, 5L); // id=1 的博客点赞数 +5
        countMap.put(2L, -3L); // id=2 的博客点赞数 -3
        blogMapper.batchUpdateThumbCount(countMap);


    }
}