package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponQuantityJpaRepository extends JpaRepository<CouponQuantity, Long> {

    @Modifying
    @Query("""
    UPDATE CouponQuantity a
    SET a.issuedQuantity = a.issuedQuantity + 1
    WHERE a.couponId = :couponId AND a.issuedQuantity < a.totalQuantity
    """)
    int issuedCoupon(Long couponId);
}
