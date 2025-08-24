package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.vo.CacheName;
import kr.hhplus.be.server.product.application.port.BestProductRankingCacheWriter;
import kr.hhplus.be.server.product.application.port.BestProductRankingReader;
import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Deprecated
@RequiredArgsConstructor
public class BestProductRankingCacheAdapter implements BestProductRankingReader, BestProductRankingCacheWriter {

    private final ProductQueryService productQueryService;

    @Override
    @CachePut(value = CacheName.BEST_PRODUCTS)
    public List<BestProduct> update() {
        return productQueryService.findBestProducts();
    }

    @Override
    @Cacheable(value = CacheName.BEST_PRODUCTS, unless = "#result == null || #result.isEmpty()")
    public List<BestProduct> get() {
        return productQueryService.findBestProducts();
    }
}