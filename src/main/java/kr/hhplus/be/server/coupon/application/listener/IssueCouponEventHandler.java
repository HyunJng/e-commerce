package kr.hhplus.be.server.coupon.application.listener;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class IssueCouponEventHandler {

    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final DateHolder dateHolder;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(IssuedCouponEvent event) {
        try {
            Coupon coupon = couponJpaRepository.findById(event.getCouponId())
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

            IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, event.getUserId(), dateHolder);

            issuedCouponJpaRepository.save(issuedCoupon);
        } catch (Exception e) {
            // 실제라면 실패내역을 db 저장
            throw e;
        }
    }
}
