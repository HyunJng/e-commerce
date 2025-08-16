package kr.hhplus.be.server.small.coupon.application.usecase;

import kr.hhplus.be.server.coupon.application.usecase.IssueCouponUseCase;
import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class IssueCouponUseCaseTest {

    private IssueCouponUseCase issueCouponUseCase;

    @Mock
    private CouponQuantityJpaRepository couponQuantityJpaRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Captor
    private ArgumentCaptor<IssuedCouponEvent> eventCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        issueCouponUseCase = new IssueCouponUseCase(
                couponQuantityJpaRepository,
                applicationEventPublisher
        );
    }

    @Test
    void 쿠폰아이디로_발급요청을_하면_성공여부를_응답하고_이벤트를_발행한다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        IssueCouponUseCase.Input input = new IssueCouponUseCase.Input(couponId, userId);

        given(couponQuantityJpaRepository.increaseIssuedCouponQuantity(couponId)).willReturn(1);

        // when
        IssueCouponUseCase.Output output = issueCouponUseCase.execute(input);

        // then
        assertThat(output.isSuccess()).isEqualTo(true);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        IssuedCouponEvent event = eventCaptor.getValue();
        assertThat(event.getCouponId()).isEqualTo(couponId);
        assertThat(event.getUserId()).isEqualTo(userId);
    }

    @Test
    void 쿠폰발행에_실패하면_이벤트를_생성하지_않는다() throws Exception {
        // given
        Long couponId = 999L;
        Long userId = 1L;
        IssueCouponUseCase.Input input = new IssueCouponUseCase.Input(couponId, userId);

        given(couponQuantityJpaRepository.increaseIssuedCouponQuantity(couponId)).willReturn(0);

        // when
        IssueCouponUseCase.Output output = issueCouponUseCase.execute(input);

        // then
        assertThat(output.isSuccess()).isEqualTo(false);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}