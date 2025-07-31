package kr.hhplus.be.server.small.product.usecase;

import kr.hhplus.be.server.common.cache.CacheKey;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.usecase.GetBestProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GetBestProductsServiceTest {

    @InjectMocks
    private GetBestProductsService getBestProductsService;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 가장_인기있는_상품목록을_응답한다() throws Exception {
        // given
        Cache cache = mock(Cache.class);

        given(cacheManager.getCache(any())).willReturn(cache);
        given(cache.get(CacheKey.bestProductsKey(), List.class))
                .willReturn(List.of(
                        new Product(1L, "상품1", 1000L, 10, null, null),
                        new Product(2L, "상품2", 2000L, 20, null, null),
                        new Product(3L, "상품3", 3000L, 30, null, null),
                        new Product(4L, "상품4", 4000L, 40, null, null),
                        new Product(5L, "상품5", 5000L, 50, null, null)
                ));

        // when
        GetBestProductsService.Output output = getBestProductsService.execute();

        // then
        assertThat(output.products()).hasSize(5);
        assertThat(output.products()).extracting("id").containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(output.products()).extracting("name").containsExactly("상품1", "상품2", "상품3", "상품4", "상품5");
        assertThat(output.products()).extracting("price").containsExactly(1000L, 2000L, 3000L, 4000L, 5000L);
    }
}