package kr.hhplus.be.server.coupon.application.usecase;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoField;

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
    private final DateHolder dateHolder;

    @Transactional
    public Output execute(Input input) {
        if (couponQuantityRepository.isAlreadyIssued(input.couponId, input.userId)) {
            return new Output(false);
        }
        long now = dateHolder.now().getLong(ChronoField.MILLI_OF_SECOND);
        boolean isSuccess = couponQuantityRepository.enqueue(input.couponId, input.userId, now);

        return new Output(isSuccess);
    }
}
