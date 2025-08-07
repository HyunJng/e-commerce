package kr.hhplus.be.server.medium.product;

import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.application.port.BestProductCacheWriter;
import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

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
        @Sql(value = "/sql/best-product-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class BestProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BestProductCacheWriter bestProductCacheWriter;
    @Autowired
    private BestProductCacheReader bestProductCacheReader;
    @Autowired
    private ProductQueryService productQueryService;

    @Test
    void 인기상품이_정상적으로_조회된다() throws Exception {
        // when
        List<Product> bestProducts = productQueryService.findBestProducts();

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
        bestProductCacheWriter.update();

        // then
        List<Product> cachedBestProducts = bestProductCacheReader.get();
        assertThat(cachedBestProducts).hasSize(5);
    }
}