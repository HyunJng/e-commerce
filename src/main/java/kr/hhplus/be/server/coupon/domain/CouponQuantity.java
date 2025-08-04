package kr.hhplus.be.server.coupon.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "coupons_quantity")
public class CouponQuantity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "quantity")
    private Integer quantity;
}
