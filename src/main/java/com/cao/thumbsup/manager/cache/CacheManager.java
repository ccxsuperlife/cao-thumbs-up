package com.cao.thumbsup.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author 小曹同学
 * @date 2025/5/23
 */
@Component
@Slf4j
public class CacheManager {

    /**
     * 热点key检测器
     */
    private TopK hotKeyDetector;

    private Cache<String, Object> localCache;

    @Bean
    public TopK getHotKeyDetector() {
        HeavyKeeper heavyKeeper = new HeavyKeeper(
                // 监控 Top 100 Key
                10,
                // 宽度
                1000,
                // 深度
                1000,
                // 衰减系数
                0.92,
                // 最小出现 10 次才记录
                0
        );
        return heavyKeeper;
    }

    @Bean
    public Cache<String, Object> getLocalCache() {
        Cache<String, Object> localCache = Caffeine.newBuilder()
                .maximumSize(1000)                // 最大容量 10,000 个条目
                .expireAfterWrite(Duration.ofMinutes(5))  // 写入 5 分钟后过期
                .build();
        return localCache;
    }
}
