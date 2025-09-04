package kr.hhplus.be.server.small.coupon.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import org.junit.jupiter.api.Test;

import java.time.Month;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본쿠폰;
import static org.assertj.core.api.Assertions.*;

class IssuedCouponTest {

    @Test
    void 쿠폰정보를_기반으로_현재일자를_기준으로_발급_일자를_계산한다() throws Exception {
        // given
        Long userId = 1L;
        Coupon coupon = 기본쿠폰();

        MockDateHolderImpl mockDateHolder = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0);

        // when
        IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId, mockDateHolder);

        // then
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getCouponId()).isEqualTo(coupon.getId());
        assertThat(issuedCoupon.getStartDate().toString()).isEqualTo("2025-07-24");
        assertThat(issuedCoupon.getEndDate().toString()).isEqualTo("2025-07-31");
    }

    @Test
    void 쿠폰이_활성_상태이고_사용일자_사이라면_오류를_반환하지_않는다() throws Exception {
        // given
        Long userId = 1L;
        Coupon coupon = 기본쿠폰();
        MockDateHolderImpl mockDateHolder = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0);

        IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId, mockDateHolder);

        // when & then
        assertThatCode(() -> issuedCoupon.validate(mockDateHolder))
                .doesNotThrowAnyException();
    }

    @Test
    void 사용일자가_지난_쿠폰이라면_오류를_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Coupon coupon = 기본쿠폰();
        MockDateHolderImpl lastDateHolder = new MockDateHolderImpl(2025, Month.JUNE, 24, 2, 0);
        MockDateHolderImpl todayDateHolder = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0);

        // when
        IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId, lastDateHolder);

        // then
        assertThatThrownBy(() -> issuedCoupon.validate(todayDateHolder))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(ErrorCode.INVALID_REQUEST.getMessage("유효하지 않은 쿠폰"));
    }
}