package kr.hhplus.be.server.small.product.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.product.application.usecase.GetProductDetailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class GetProductDetailUseCaseTest {

    @InjectMocks
    private GetProductDetailUseCase getProductDetailUseCase;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 상품ID로_요청하면_상품상세정보를_응답한다() throws Exception {
        // given
        Long productId = 1L;
        Product product = new Product(productId, "테스트상품", 100L, 1000, null, null);

        GetProductDetailUseCase.Input input = new GetProductDetailUseCase.Input(productId);

        given(productJpaRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        GetProductDetailUseCase.Output output = getProductDetailUseCase.execute(input);

        // then
        assertThat(output).isNotNull();
        assertThat(output.id()).isEqualTo(productId);
        assertThat(output.name()).isEqualTo(product.getName());
        assertThat(output.price()).isEqualTo(product.getPrice());
        assertThat(output.quantity()).isEqualTo(product.getQuantity());
    }

    @Test
    void 존재하지않는_상품ID로_조회하면_오류를_반환한다() throws Exception {
        // given
        GetProductDetailUseCase.Input input = new GetProductDetailUseCase.Input(1L);

        given(productJpaRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getProductDetailUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("상품"));
    }
}