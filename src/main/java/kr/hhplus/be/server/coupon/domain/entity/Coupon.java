package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import lombok.Getter;

@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    public enum DiscountType {PERCENT, FIXED_AMOUNT}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "dates")
    private Integer dates;

    public Coupon(Long id, String name, Long discountAmount, DiscountType discountType, Integer dates) {
        this.id = id;
        this.name = name;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.dates = dates;
    }

    public Coupon() {}

    public Long calculateDiscountAmount(Long totalAmount) {
        switch (discountType) {
            case PERCENT:
                return totalAmount * discountAmount / 100;
            case FIXED_AMOUNT:
                return discountAmount;
            default:
                throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
