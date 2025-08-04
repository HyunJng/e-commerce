package kr.hhplus.be.server.small.order.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItemsJpaRepository;
import kr.hhplus.be.server.order.domain.OrderJpaRepository;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlaceOrderUseCaseTest {

    @InjectMocks
    private PlaceOrderUseCase placeOrderUseCase;

    @Mock
    private OrderJpaRepository orderJpaRepository;
    @Mock
    private OrderItemsJpaRepository orderItemsJpaRepository;
    @Mock
    private ProductJpaRepository productJpaRepository;
    @Mock
    private CouponJpaRepository couponJpaRepository;
    @Mock
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Mock
    private WalletJpaRepository walletJpaRepository;
    @Mock
    private DateHolder dateHolder;


    public PlaceOrderUseCaseTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 유저정보와_주문할_상품정보를_요청하면_주문된_내역을_반환한다() {
        // given
        Long userId = 1L;
        Long couponId = 2L;

        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(
                new PlaceOrderUseCase.Input.OrderProduct(1L, 2)
        );
        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(userId, couponId, orderProducts);

        Product product = mock(Product.class);
        given(product.getId()).willReturn(1L);
        given(product.getPrice()).willReturn(100L);

        Coupon coupon = mock(Coupon.class);
        given(coupon.calculateDiscountAmount(200L)).willReturn(50L);

        IssuedCoupon issuedCoupon = mock(IssuedCoupon.class);

        Wallet wallet = mock(Wallet.class);

        given(productJpaRepository.findAllById(anyList())).willReturn(List.of(product));
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.of(issuedCoupon));
        given(walletJpaRepository.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(orderJpaRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(orderItemsJpaRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PlaceOrderUseCase.Output output = placeOrderUseCase.execute(input);

        // then
        assertThat(output).isNotNull();
        assertThat(output.userId()).isEqualTo(userId);
        assertThat(output.totalAmount()).isEqualTo(200L);
        assertThat(output.discountAmount()).isEqualTo(50L);
        assertThat(output.paidAmount()).isEqualTo(150L);

        verify(product).decreaseQuantity(2);
        verify(wallet).pay(150L, dateHolder);
        verify(issuedCoupon).use();
    }

    @Test
    void 상품주문시_일부상품이_조회되지_않으면_오류를_반환한다() {
        // given
        Long userId = 1L;
        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(
                new PlaceOrderUseCase.Input.OrderProduct(1L, 2)
        );

        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(userId, null, orderProducts);

        given(productJpaRepository.findAllById(anyList())).willReturn(List.of());

        // when & then
        Assertions.assertThatThrownBy(() -> placeOrderUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.INVALID_REQUEST.getMessage("주문한 상품 중 일부가 존재하지 않습니다."));
    }

    @Test
    void 쿠폰이_존재하지_않으면_오류를_반환한다() {
        // given
        Long userId = 1L;
        Long couponId = 2L;
        Product product = new Product(1L, "Test Product", 100L, 10, null, null);
        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(
                new PlaceOrderUseCase.Input.OrderProduct(1L, 2)
        );

        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(userId, couponId, orderProducts);

        given(productJpaRepository.findAllById(anyList())).willReturn(List.of(product));
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> placeOrderUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("쿠폰"));
    }

    @Test
    void 지갑이_존재하지_않으면_오류를_반환한다() {
        // given
        Long userId = 1L;
        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(
                new PlaceOrderUseCase.Input.OrderProduct(1L, 2)
        );
        Product product = new Product(1L, "Test Product", 100L, 10, null, null);

        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(userId, null, orderProducts);

        given(productJpaRepository.findAllById(anyList())).willReturn(List.of(product));
        given(walletJpaRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> placeOrderUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("지갑"));
    }

    @Test
    void 사용하려는_쿠폰이_이미_사용_상태라면_오류를_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 2L;

        Product product = new Product(1L, "Test Product", 100L, 10, null, null);
        Coupon coupon = new Coupon(couponId, "Test Coupon", 50L, Coupon.DiscountType.FIXED_AMOUNT, 10, null, null);
        IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId, new MockDateHolderImpl(2025, Month.JULY, 1, 1, 10));

        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(
                new PlaceOrderUseCase.Input.OrderProduct(1L, 2)
        );

        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(userId, couponId, orderProducts);

        issuedCoupon.use();
        given(productJpaRepository.findAllById(anyList())).willReturn(List.of(product));
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.of(issuedCoupon));

        // when & then
        Assertions.assertThatThrownBy(() -> placeOrderUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.INVALID_REQUEST.getMessage("유효하지 않은 쿠폰"));
    }

}