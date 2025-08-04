package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.cache.spring.SpringCacheName;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.application.service.BestProductQueryService;
import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.application.port.BestProductCacheWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BestProductCacheAdapter implements BestProductCacheReader, BestProductCacheWriter {

    private final BestProductQueryService bestProductQueryService;

    @Override
    @CachePut(value = SpringCacheName.BEST_PRODUCTS)
    public List<Product> update() {
        return bestProductQueryService.findBestProducts();
    }

    @Override
    @Cacheable(value = SpringCacheName.BEST_PRODUCTS)
    public List<Product> get() {
        return List.of();
    }
}
