package kr.hhplus.be.server.coupon.application.usecase;

import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueCouponUseCase {

    public record Input(Long couponId, Long userId) {
    }

    public record Output(
            boolean isSuccess
    ) {
    }

    private final CouponQuantityJpaRepository couponQuantityJpaRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Output execute(Input input) {
        // 유저정보의 유효성은 인증/인가 과정에서 처리되었을 것이라 가정하였음
        boolean isSuccess = (couponQuantityJpaRepository.increaseIssuedCouponQuantity(input.couponId)) != 0;
        if (isSuccess) {
            applicationEventPublisher.publishEvent(new IssuedCouponEvent(input.couponId, input.userId));
        }
        return new Output(isSuccess);
    }
}
