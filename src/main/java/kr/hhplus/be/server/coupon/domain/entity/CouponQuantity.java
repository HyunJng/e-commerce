package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
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

    @Column(name = "issued_quantity")
    private Integer issuedQuantity;
}
