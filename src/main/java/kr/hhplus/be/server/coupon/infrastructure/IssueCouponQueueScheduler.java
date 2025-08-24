package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.application.service.CouponIssueProcessor;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueCouponQueueScheduler {

    private final CouponIssueProcessor processor;
    private final CouponJpaRepository couponJpaRepository;

    @Scheduled(fixedDelay = 1000) // 1초
    public void drain() {
        var activeCouponIds = couponJpaRepository.findActiveCouponIds();
        for (Long couponId : activeCouponIds) {
            processor.drain(couponId, 200);
        }
    }
}