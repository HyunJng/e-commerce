package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    @Query("""
                SELECT a.id 
                FROM Coupon a 
                WHERE a.state = 'ACTIVE'
            """)
    List<Long> findActiveCouponIds();
}
