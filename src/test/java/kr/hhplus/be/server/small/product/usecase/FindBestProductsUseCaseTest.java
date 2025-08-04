package kr.hhplus.be.server.small.product.usecase;

import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static kr.hhplus.be.server.mock.DomainTestFixtures.포맷상품;
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
                        포맷상품(1L),
                        포맷상품(2L),
                        포맷상품(3L),
                        포맷상품(4L),
                        포맷상품(5L)
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