package kr.hhplus.be.server.small.product.application.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.application.usecase.GetProductDetailUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본상품;
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
        Product product = 기본상품();

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