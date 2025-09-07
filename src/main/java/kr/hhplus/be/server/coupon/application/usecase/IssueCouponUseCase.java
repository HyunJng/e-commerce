package kr.hhplus.be.server.coupon.application.usecase;

import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import lombok.RequiredArgsConstructor;
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

    private final CouponQuantityRepository couponQuantityRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public Output execute(Input input) {
        if (couponQuantityRepository.isAlreadyIssued(input.couponId, input.userId)) {
            return new Output(false);
        }

        eventPublisher.publish(
                "coupon-issued",
                input.couponId.toString(),
                new IssuedCouponEvent(input.couponId, input.userId)
        );
        return new Output(true);
    }
}
