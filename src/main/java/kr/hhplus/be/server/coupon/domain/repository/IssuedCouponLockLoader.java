package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IssuedCouponLockLoader {

    private final IssuedCouponLockJpaRepository issuedCouponLockJpaRepository;

    public Optional<IssuedCoupon> findByUserIdAndCouponId(Long userId, Long couponId) throws OptimisticEntityLockException {
        return issuedCouponLockJpaRepository.findByUserIdAndCouponIdWithVersion(userId, couponId);
    }
}
