package kr.hhplus.be.server.coupon.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.response.ResultCode;
import lombok.Getter;

@Getter
@Entity
@Table(name = "coupons")
public class Coupon {
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

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "created_at")
    private Long createdAt;

    public Coupon(Long id, String name, Long discountAmount, DiscountType discountType, Integer dates, Integer totalQuantity, Long createdAt) {
        this.id = id;
        this.name = name;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.dates = dates;
        this.totalQuantity = totalQuantity;
        this.createdAt = createdAt;
    }

    public Coupon() {}

    public Long calculateDiscountAmount(Long totalAmount) {
        switch (discountType) {
            case PERCENT:
                return totalAmount * discountAmount / 100;
            case FIXED_AMOUNT:
                return discountAmount;
            default:
                throw new CommonException(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }
}
