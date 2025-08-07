package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PessimisticLockException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IssuedCouponLockLoader {

    private final IssuedCouponLockJpaRepository issuedCouponLockJpaRepository;

    public Optional<IssuedCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        try {
            return issuedCouponLockJpaRepository.findByUserIdAndCouponIdForUpdate(userId, couponId);
        } catch (PessimisticLockException | LockAcquisitionException | CannotAcquireLockException e) {
            log.error("LOCK EXCEPTION: {} INPUT: {} {}", this.getClass().getName(), userId, couponId, e);
            throw new CommonException(ErrorCode.RACE_CONDITION_EXCEPTION, e);
        }
    }
}
