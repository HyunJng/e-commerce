package kr.hhplus.be.server.small.product.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.entity.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본상품;

class ProductTest {

    @ParameterizedTest(name = "상품수량이 10일 떄 주문 수량이 {0}인 경우 상품 수량을 감소시킨다")
    @ValueSource(ints = {5, 10})
    void 주문_수량이_상품_수량과_같거나_작은_경우_상품수량을_감소시킨다(int quentity) throws Exception {
        // given
        Product product = 기본상품();

        // when & then
        Assertions.assertThatCode(() -> product.decreaseQuantity(quentity))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "상품수량이 10일 떄 주문 수량이 {0}인 경우 상품 수량을 감소시키지 못한다")
    @ValueSource(ints = {11, 20})
    void 주문_수량이_상품_수량보다_많은_경우_상품수량을_감소시키지_못한다(int quentity) throws Exception {
        // given
        Product product = 기본상품();

        // when & then
        Assertions.assertThatThrownBy(() -> product.decreaseQuantity(quentity))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_RESOURCE.getMessage("상품 재고"));
    }
}