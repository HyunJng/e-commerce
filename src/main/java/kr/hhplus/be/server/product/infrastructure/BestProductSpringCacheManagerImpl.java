package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.cache.CacheKey;
import kr.hhplus.be.server.common.cache.spring.SpringCacheName;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.usecase.port.BestProductCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BestProductSpringCacheManagerImpl implements BestProductCacheManager {

    private final CacheManager cacheManager;

    @Override
    public void save(List<Product> bestProducts) {
        Cache cache = Optional.of(cacheManager.getCache (SpringCacheName.BEST_PRODUCTS))
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));

        cache.put(CacheKey.bestProductsKey(), bestProducts);
    }

    @Override
    public List<Product> get() {
        Cache cache = Optional.ofNullable(cacheManager.getCache(SpringCacheName.BEST_PRODUCTS))
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        List<Product> bestProducts = Optional.ofNullable(cache.get(CacheKey.bestProductsKey(), List.class)).orElse(List.of());
        return bestProducts;
    }
}
