package kr.hhplus.be.server.medium.product;

import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import kr.hhplus.be.server.product.infrastructure.BestProductCacheAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private BestProductCacheAdapter bestProductCacheAdapter;
    @MockitoSpyBean
    private ProductQueryService productQueryService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 인기상품이_정상적으로_조회된다() throws Exception {
        // when
        List<BestProduct> bestProducts = productQueryService.findBestProducts();

        // then
        assertThat(bestProducts).hasSize(5);
        assertThat(bestProducts.get(0).id()).isEqualTo(1L);
        assertThat(bestProducts.get(1).id()).isEqualTo(2L);
        assertThat(bestProducts.get(2).id()).isIn(3L, 4L, 5L);
        assertThat(bestProducts.get(3).id()).isIn(3L, 4L, 5L);
        assertThat(bestProducts.get(4).id()).isIn(3L, 4L, 5L);
    }

    @Test
    void 조회_시_캐시에_데이터가_없으면_쿼리를_통해_데이터를_가져오고_캐시에_넣는다() throws Exception {
        // when
        bestProductCacheAdapter.get();
        bestProductCacheAdapter.get();

        // then
        verify(productQueryService, times(1)).findBestProducts();
    }
}