package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.common.cache.CacheKey;
import kr.hhplus.be.server.common.cache.spring.SpringCacheName;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaveBestProductInCacheScheduler {

    private static final long SCHEDULED_RATE = 5L * 60 * 1000; // 5분

    private final ProductJpaRepository productJpaRepository;
    private final CacheManager cacheManager;
    private final DateHolder dateHolder;

    @Scheduled(fixedRate = SCHEDULED_RATE)
    public void execute() {
        LocalDate searchEndDay = dateHolder.today();
        LocalDate searchStartDay = searchEndDay.minusDays(3);

        Pageable pageable = Pageable.ofSize(5);
        List<Product> bestProducts = productJpaRepository.findBestProductsBetweenDays(searchStartDay, searchEndDay, pageable);

        Cache cache = Optional.of(cacheManager.getCache (SpringCacheName.BEST_PRODUCTS))
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));

        cache.put(CacheKey.bestProductsKey(), bestProducts);
    }
}
