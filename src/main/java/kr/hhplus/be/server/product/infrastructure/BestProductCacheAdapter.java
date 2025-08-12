package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.cache.CacheName;
import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.application.port.BestProductCacheWriter;
import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BestProductCacheAdapter implements BestProductCacheReader, BestProductCacheWriter {

    private final ProductQueryService productQueryService;

    @Override
    @CachePut(value = CacheName.BEST_PRODUCTS)
    public List<BestProduct> update() {
        return productQueryService.findBestProducts();
    }

    @Override
    @Cacheable(value = CacheName.BEST_PRODUCTS)
    public List<BestProduct> get() {
        return List.of();
    }
}
