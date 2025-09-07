package kr.hhplus.be.server.small.order.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponLockLoader;
import kr.hhplus.be.server.order.application.service.CouponPricingService;
import kr.hhplus.be.server.order.domain.entity.DiscountInfo;
import kr.hhplus.be.server.order.domain.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본쿠폰;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class CouponPricingServiceTest {

    @InjectMocks
    private CouponPricingService couponPricingService;
    @Mock
    private CouponJpaRepository couponJpaRepository;
    @Mock
    private IssuedCouponLockLoader issuedCouponLockLoader;
    @Mock
    private DateHolder dateHolder;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 쿠폰이_존재하면_검증을_시도한다() {
        // given
        Long couponId = 1L, useId = 1L;
        OrderItem orderItem = Mockito.mock(OrderItem.class);
        IssuedCoupon issuedCoupon = Mockito.mock(IssuedCoupon.class);

        given(orderItem.totalAmount()).willReturn(10000L);
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(기본쿠폰()));
        given(issuedCouponLockLoader.findByUserIdAndCouponId(useId, couponId)).willReturn(Optional.ofNullable(issuedCoupon));

        // when
        couponPricingService.applyCouponPricing(1L, 1L, List.of(orderItem));

        // then
        verify(issuedCoupon).validate(dateHolder);
    }

    @Test
    void 쿠폰이_존재하지_않으면_오류를_반환한다() {
        // given
        Long couponId = 1L, useId = 1L;
        OrderItem orderItem = Mockito.mock(OrderItem.class);
        IssuedCoupon issuedCoupon = Mockito.mock(IssuedCoupon.class);

        given(orderItem.totalAmount()).willReturn(10000L);
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(기본쿠폰()));
        given(issuedCouponLockLoader.findByUserIdAndCouponId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponPricingService.applyCouponPricing(1L, 1L, List.of(orderItem)))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("발급 쿠폰"));
    }

    @Test
    void 쿠폰이_존재하면_할인정보를_응답한다() throws Exception {
        // given
        Long couponId = 1L, useId = 1L;
        OrderItem orderItem = Mockito.mock(OrderItem.class);
        IssuedCoupon issuedCoupon = Mockito.mock(IssuedCoupon.class);

        given(orderItem.totalAmount()).willReturn(10000L);
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(기본쿠폰()));
        given(issuedCouponLockLoader.findByUserIdAndCouponId(useId, couponId)).willReturn(Optional.ofNullable(issuedCoupon));

        // when
        DiscountInfo discountInfo = couponPricingService.applyCouponPricing(1L, 1L, List.of(orderItem));

        // then
        verify(issuedCoupon).use();
        assertThat(discountInfo.discountAmount()).isEqualTo(1000);//10000의 10퍼
    }
}