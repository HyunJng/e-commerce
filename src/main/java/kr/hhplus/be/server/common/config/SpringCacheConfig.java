package kr.hhplus.be.server.common.config;

import kr.hhplus.be.server.common.vo.CacheName;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@Deprecated
public class SpringCacheConfig {

    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CacheName.BEST_PRODUCTS);
    }
}
