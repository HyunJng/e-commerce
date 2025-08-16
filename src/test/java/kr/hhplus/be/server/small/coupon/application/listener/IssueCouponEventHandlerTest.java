package kr.hhplus.be.server.small.coupon.application.listener;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.application.listener.IssueCouponEventHandler;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Month;
import java.util.Optional;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본쿠폰;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class IssueCouponEventHandlerTest {

    private IssueCouponEventHandler issueCouponEventHandler;
    @Mock
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Mock
    private CouponJpaRepository couponJpaRepository;
    @Captor
    private ArgumentCaptor<IssuedCoupon> issuedCouponArgumentCaptor;

    private DateHolder dateHolder = new MockDateHolderImpl(2025, Month.JULY, 24, 2, 0);

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        issueCouponEventHandler = new IssueCouponEventHandler(
                couponJpaRepository,
                issuedCouponJpaRepository,
                dateHolder
        );
    }

    @Test
    void 쿠폰발급_이벤트가_발생하면_쿠폰을_발급한다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        IssuedCouponEvent issuedCouponEvent = new IssuedCouponEvent(couponId, userId);

        Coupon coupon = 기본쿠폰();
        given(couponJpaRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when
        issueCouponEventHandler.handle(issuedCouponEvent);

        // then
        verify(issuedCouponJpaRepository).save(issuedCouponArgumentCaptor.capture());

        IssuedCoupon issuedCoupon = issuedCouponArgumentCaptor.getValue();
        assertThat(issuedCoupon.getCouponId()).isEqualTo(coupon.getId());
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.ACTIVE);
    }
}