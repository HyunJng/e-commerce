package kr.hhplus.be.server.small.product.application.service;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@Disabled
class ProductQueryServiceTest {

    @InjectMocks
    private ProductQueryService productQueryService;

    @Mock
    private ProductJpaRepository productJpaRepository;
    @Mock
    private DateHolder dateHolder;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 가장_인기있는_상품목록을_응답한다() throws Exception {
        // given
        LocalDate today = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0).today();
        given(dateHolder.today()).willReturn(today);
        Pageable pageable = Pageable.ofSize(5);

        given(productJpaRepository.findBestProductsBetweenDays(today.minusDays(3), today, pageable))
                .willReturn(List.of(
                        new BestProduct(1L, "상품1", 1000L, 10, 5L),
                        new BestProduct(2L, "상품2", 2000L, 10, 4L),
                        new BestProduct(3L, "상품3", 3000L, 10, 3L),
                        new BestProduct(4L, "상품4", 4000L, 10, 2L),
                        new BestProduct(5L, "상품5", 5000L, 10, 1L)
                ));

        // when
        List<BestProduct> bestProducts = productQueryService.findBestProducts();

        // then
        assertThat(bestProducts.size()).isEqualTo(5);
        assertThat(bestProducts).extracting("id").containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(bestProducts).extracting("name").containsExactly("상품1", "상품2", "상품3", "상품4", "상품5");
        assertThat(bestProducts).extracting("price").containsExactly(1000L, 2000L, 3000L, 4000L, 5000L);
    }

}