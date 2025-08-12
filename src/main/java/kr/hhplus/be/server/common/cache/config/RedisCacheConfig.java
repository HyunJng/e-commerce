package kr.hhplus.be.server.common.cache.config;

import kr.hhplus.be.server.common.cache.CacheName;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@EnableCaching
@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> caches = new HashMap<>();
        caches.put(CacheName.BEST_PRODUCTS, new CacheConfig(
                Duration.ofMinutes(5).toMillis(),
                Duration.ofMinutes(2).toMillis()));
        return new RedissonSpringCacheManager(redissonClient, caches);
    }
}

