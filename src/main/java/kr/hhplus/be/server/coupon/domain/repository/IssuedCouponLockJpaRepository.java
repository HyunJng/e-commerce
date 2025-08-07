package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IssuedCouponLockJpaRepository extends JpaRepository<IssuedCoupon, Long> {

    @Query("""
               SELECT a
               FROM IssuedCoupon a
               WHERE a.userId = :userId AND a.couponId = :couponId
            """)
    Optional<IssuedCoupon> findByUserIdAndCouponIdForUpdate(Long userId, Long couponId);
}
