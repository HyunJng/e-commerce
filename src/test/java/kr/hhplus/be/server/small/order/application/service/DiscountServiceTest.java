package kr.hhplus.be.server.small.order.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.order.application.service.DiscountService;
import kr.hhplus.be.server.order.domain.DiscountInfo;
import kr.hhplus.be.server.order.domain.OrderItem;
import org.assertj.core.api.Assertions;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class DiscountServiceTest {

    @InjectMocks
    private DiscountService discountService;
    @Mock
    private CouponJpaRepository couponJpaRepository;
    @Mock
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Mock
    private DateHolder dateHolder;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 쿠폰이_존재하면_검증을_시도한다() {
        // given
        IssuedCoupon issuedCoupon = Mockito.mock(IssuedCoupon.class);
        given(issuedCouponJpaRepository.findByUserIdAndCouponId(1L, 1L)).willReturn(Optional.of(issuedCoupon));

        // when
        discountService.validateOrThrow(1L, 1L);

        // then
        verify(issuedCoupon).validate(dateHolder);
    }

    @Test
    void 쿠폰이_존재하지_않으면_오류를_반환한다() {
        // given
        given(issuedCouponJpaRepository.findByUserIdAndCouponId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> discountService.validateOrThrow(1L, 1L))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("쿠폰"));
    }

    @Test
    void 쿠폰이_존재하면_할인정보를_응답한다() throws Exception {
        // given
        Long couponId = 1L, useId = 1L;
        OrderItem orderItem = Mockito.mock(OrderItem.class);
        IssuedCoupon issuedCoupon = Mockito.mock(IssuedCoupon.class);

        given(orderItem.totalAmount()).willReturn(10000L);
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(기본쿠폰()));
        given(issuedCouponJpaRepository.findByUserIdAndCouponId(useId, couponId)).willReturn(Optional.ofNullable(issuedCoupon));

        // when
        DiscountInfo discountInfo = discountService.calculate(1L, 1L, List.of(orderItem));

        // then
        verify(issuedCoupon).use();
        assertThat(discountInfo.discountAmount()).isEqualTo(1000);//10000의 10퍼
    }
}