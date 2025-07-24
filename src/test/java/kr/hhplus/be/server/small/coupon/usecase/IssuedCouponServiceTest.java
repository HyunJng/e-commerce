package kr.hhplus.be.server.small.coupon.usecase;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.coupon.usecase.IssuedCouponService;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class IssuedCouponServiceTest {

    private IssuedCouponService issuedCouponService;

    @Mock
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Mock
    private CouponJpaRepository couponJpaRepository;

    private DateHolder dateHolder = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0);

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        issuedCouponService = new IssuedCouponService(
                couponJpaRepository,
                issuedCouponJpaRepository,
                dateHolder
        );
    }

    @Test
    void 쿠폰아이디로_발급요청을_하면_발급된_쿠폰을_반환한다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        Coupon coupon = new Coupon(
                couponId,
                "회원가입쿠폰",
                10L,
                Coupon.DiscountType.PERCENT,
                7,
                10,
                null
        );
        IssuedCouponService.Input input = new IssuedCouponService.Input(couponId, userId);

        given(couponJpaRepository.findById(couponId))
                .willReturn(Optional.of(coupon));
        given(issuedCouponJpaRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        IssuedCouponService.Output output = issuedCouponService.execute(input);

        // then
        assertThat(output.couponId()).isEqualTo(couponId);
        assertThat(output.couponName()).isEqualTo(coupon.getName());
        assertThat(output.discountAmount()).isEqualTo(coupon.getDiscountAmount());
        assertThat(output.discountType()).isEqualTo(coupon.getDiscountType().name());
        assertThat(output.startedAt()).isNotNull();
        assertThat(output.endAt()).isNotNull();
    }
}