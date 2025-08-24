package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.resolver.IssueCouponLockKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueProcessor {
    private final CouponQuantityRepository couponQuantityRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final DateHolder dateHolder;

    @Transactional
    @DistributedLock(resolver = IssueCouponLockKeyResolver.class)
    public void drain(Long couponId, Integer maxBatch) {
        long limit = couponQuantityRepository.getLimit(couponId);
        long issued = couponQuantityRepository.getIssuedUsersCount(couponId);
        long remaining = Math.max(0, limit - issued);
        if (remaining <= 0) return;

        int n = (int) Math.min(remaining, maxBatch);
        var userIds = couponQuantityRepository.popNext(couponId, n);
        if (userIds.isEmpty()) return;

        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        for (Long userId : userIds) {
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
}
