package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.event.IssuedCouponEvent;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCouponIssueConsumer {

    private final CouponQuantityRepository couponQuantityRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final DateHolder dateHolder;

    @KafkaListener(
            topics = "${kafka.topics.coupon-issued.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${kafka.topics.coupon-issued.partitions}"
    )
    @Transactional
    public void onMessage(IssuedCouponEvent event) {
        Long couponId = event.getCouponId();
        Long userId = event.getUserId();

        if (couponQuantityRepository.isAlreadyIssued(couponId, userId)) {
            return;
        }

        long limit = couponQuantityRepository.getLimit(couponId);
        long issued = couponQuantityRepository.getIssuedUsersCount(couponId);
        long remaining = Math.max(0, limit - issued);
        if (remaining <= 0) return;

        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        try {
            IssuedCoupon ic = new IssuedCoupon(coupon, userId, dateHolder);
            issuedCouponJpaRepository.save(ic);
            couponQuantityRepository.markIssued(couponId, userId);
        } catch (Exception e) {
            log.error("[ISSUED COUPON ERROR] 쿠폰 발급 실패 couponId = {}, userId = {}, error  {}"
                    , couponId, userId, e.getMessage(), e);
        }
    }

}
