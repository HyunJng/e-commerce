package kr.hhplus.be.server.small.product.application.usecase;

import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class FindBestProductsUseCaseTest {

    @InjectMocks
    private FindBestProductsUseCase findBestProductsUsecase;

    @Mock
    private BestProductCacheReader bestProductCacheReader;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 가장_인기있는_상품목록을_응답한다() throws Exception {
        // given
        given(bestProductCacheReader.get())
                .willReturn(List.of(
                        new BestProduct(1L, "상품1", 1000L, 10, 5L),
                        new BestProduct(2L, "상품2", 2000L, 10, 4L),
                        new BestProduct(3L, "상품3", 3000L, 10, 3L),
                        new BestProduct(4L, "상품4", 4000L, 10, 2L),
                        new BestProduct(5L, "상품5", 5000L, 10, 1L)
                ));

        // when
        FindBestProductsUseCase.Output output = findBestProductsUsecase.execute();

        // then
        assertThat(output.products()).hasSize(5);
        assertThat(output.products()).extracting("id").containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(output.products()).extracting("name").containsExactly("상품1", "상품2", "상품3", "상품4", "상품5");
        assertThat(output.products()).extracting("price").containsExactly(1000L, 2000L, 3000L, 4000L, 5000L);
    }
}