package com.cao.thumbsup.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author 小曹同学
 * @date 2025/5/23
 * @descpription 热点数据缓存到本地缓存
 */
@Component
@Slf4j
public class CacheManager {

    /**
     * 热点key检测器
     */
    private TopK hotKeyDetector;

    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Bean
    public TopK getHotKeyDetector() {
        hotKeyDetector = new HeavyKeeper(
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
        return hotKeyDetector;
    }

    @Bean
    public Cache<String, Object> getLocalCache() {
        localCache = Caffeine.newBuilder()
                .maximumSize(1000)                // 最大容量 10,000 个条目
                .expireAfterWrite(Duration.ofMinutes(5))  // 写入 5 分钟后过期
                .build();
        return localCache;
    }

    /**
     * 构造复合key
     *
     * @param hashKey
     * @param key
     * @return
     */
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    public Object get(String hashKey, String key) {
        // 参数校验
        if (hashKey == null || key == null) {
            log.warn("hashKey or key is null");
            return null;
        }
        // 构造唯一的 compositeKey
        String compositeKey = buildCacheKey(hashKey, key);
        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
            // 记录访问次数(每次访问次数+1)
            if (hotKeyDetector != null) {
                hotKeyDetector.add(key, 1);
            }
            return value;
        }
        // 2. 本地缓存未命中,查分布式缓存
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if (redisValue == null) {
            return null;
        }
        // 记录访问次数(每次访问次数+1)
        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }
        return redisValue;
    }

    /**
     * 将热点数据写入本地缓存
     *
     * @param hashKey
     * @param key
     * @param value
     */
    public void putIfPresent(String hashKey, String key, Object value) {
        // 参数校验
        if (hashKey == null || key == null || value == null) {
            return;
        }
        String compositeKey = buildCacheKey(hashKey, key);
        localCache.put(compositeKey, value);
    }

    // 定期清理过期的热点key,检测数据
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void clearHotKeys() {
        try {
            if (hotKeyDetector != null) {
                hotKeyDetector.fading();
            }
        } catch (Exception e) {
            log.error("clear hot keys error", e);
        }

    }

}
