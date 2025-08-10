package kr.hhplus.be.server.small.order.application.usecase;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.order.application.service.CouponPricingService;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
import kr.hhplus.be.server.order.domain.entity.DiscountInfo;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.repository.OrderJpaRepository;
import kr.hhplus.be.server.product.application.service.ProductLockingQueryService;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.wallet.application.service.WalletCommandService;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본상품;
import static kr.hhplus.be.server.mock.DomainTestFixtures.기본지갑;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class PlaceOrderUseCaseTest {

    @InjectMocks
    private PlaceOrderUseCase placeOrderUseCase;

    @Mock
    private OrderJpaRepository orderJpaRepository;
    @Mock
    private ProductLockingQueryService productQueryService;
    @Mock
    private CouponPricingService couponPricingService;
    @Mock
    private WalletCommandService walletCommandService;
    @Mock
    private DateHolder dateHolder;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 유저정보와_주문할_상품정보를_요청하면_주문된_내역을_반환한다() {
        // given
        Product product = 기본상품();
        Wallet wallet = 기본지갑();
        Integer quantity = 2;

        List<PlaceOrderUseCase.Input.OrderProduct> orderProducts = List.of(new PlaceOrderUseCase.Input.OrderProduct(product.getId(), quantity));
        PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(wallet.getUserId(), null, orderProducts);

        given(productQueryService.findProducts(anyList())).willReturn(Map.of(1L, product));
        given(couponPricingService.applyCouponPricing(isNull(), eq(wallet.getUserId()), anyList())).willReturn(DiscountInfo.none());
        given(orderJpaRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PlaceOrderUseCase.Output output = placeOrderUseCase.execute(input);

        // then
        assertThat(output).isNotNull();
        assertThat(output.userId()).isEqualTo(wallet.getUserId());
        assertThat(output.totalAmount()).isEqualTo(product.calculateAmount(quantity));
        assertThat(output.discountAmount()).isEqualTo(0L);
        assertThat(output.paidAmount()).isEqualTo(product.calculateAmount(quantity));
    }
}