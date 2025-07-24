package kr.hhplus.be.server.coupon.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssuedCouponJpaRepository extends JpaRepository<IssuedCoupon, Long> {
    Optional<IssuedCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}
