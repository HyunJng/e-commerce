package kr.hhplus.be.server.common.cache.config;

import kr.hhplus.be.server.common.cache.CacheName;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@Deprecated
public class SpringCacheConfig {

    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CacheName.BEST_PRODUCTS);
    }
}
