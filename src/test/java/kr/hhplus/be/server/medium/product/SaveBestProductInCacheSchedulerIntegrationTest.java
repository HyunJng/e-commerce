package kr.hhplus.be.server.medium.product;

import kr.hhplus.be.server.common.cache.CacheKey;
import kr.hhplus.be.server.common.cache.spring.SpringCacheName;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.product.usecase.SaveBestProductInCacheScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
    인기 상품 조회 테스트를 위한 데이터 정리.
    | product_id | 주문 횟수 |
    | ----------- | ----- |
    | 1           |  4   |
    | 2           |  3   |
    | 3           |  2   |
    | 4           |  2   |
    | 5           |  2   |
    | 6           | 1    |
    | 7           | 1    |

    인기 상품은 주문 횟수 기준으로 정렬된다.
 */
@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/best-product-in-cache-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class SaveBestProductInCacheSchedulerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private CacheManager cacheManager;

    private SaveBestProductInCacheScheduler saveBestProductInCacheScheduler;

    private DateHolder dateHolder;

    @BeforeEach
    void init() {
        dateHolder = new MockDateHolderImpl(2025, Month.JULY, 31, 0, 0);
        saveBestProductInCacheScheduler = new SaveBestProductInCacheScheduler(
                productJpaRepository,
                cacheManager,
                dateHolder
        );
    }

    @Test
    void 인기상품이_정상적으로_조회된다() throws Exception {
        // given
        LocalDate searchEndDay = dateHolder.today();
        LocalDate searchStartDay = searchEndDay.minusDays(3);
        Pageable pageable = Pageable.ofSize(5);

        // when
        List<Product> bestProducts = productJpaRepository.findBestProductsBetweenDays(searchStartDay, searchEndDay, pageable);

        // then
        assertThat(bestProducts).hasSize(5);
        assertThat(bestProducts.get(0).getId()).isEqualTo(1L);
        assertThat(bestProducts.get(1).getId()).isEqualTo(2L);
        assertThat(bestProducts.get(2).getId()).isIn(3L, 4L, 5L);
        assertThat(bestProducts.get(3).getId()).isIn(3L, 4L, 5L);
        assertThat(bestProducts.get(4).getId()).isIn(3L, 4L, 5L);
    }

    @Test
    void 조회된_인기상품이_캐시에_저장된다() throws Exception {
        // when
        saveBestProductInCacheScheduler.execute();

        // then
        Cache cache = cacheManager.getCache(SpringCacheName.BEST_PRODUCTS);
        assertThat(cache).isNotNull();

        List<Product> cachedBestProducts = cache.get(CacheKey.bestProductsKey(), List.class);
        assertThat(cachedBestProducts).hasSize(5);
    }
}