package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.common.cache.CacheKey;
import kr.hhplus.be.server.common.cache.spring.SpringCacheName;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetBestProductsService {

    public record Output(
            List<ProductInfo> products
    ) {
        public record ProductInfo(
                Long id,
                String name,
                Long price
        ) {
        }
    }

    private final CacheManager cacheManager;
    private final SaveBestProductInCacheScheduler saveBestProductInCacheScheduler;

    public Output execute() {
        Cache cache = Optional.ofNullable(cacheManager.getCache(SpringCacheName.BEST_PRODUCTS))
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));

        List<Product> bestProducts = Optional.ofNullable(cache.get(CacheKey.bestProductsKey(), List.class)).orElse(List.of());
        if (bestProducts.isEmpty()) {
            saveBestProductInCacheScheduler.execute();
            bestProducts = cache.get(CacheKey.bestProductsKey(), List.class);
        }

        List<Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new Output.ProductInfo(product.getId(), product.getName(), product.getPrice()))
                .toList();
        return new Output(productInfos);
    }
}