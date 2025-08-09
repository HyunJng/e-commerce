package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

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

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    public Coupon(Long id, String name, Long discountAmount, DiscountType discountType, Integer dates, LocalDateTime createAt) {
        this.id = id;
        this.name = name;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.dates = dates;
        this.createAt = createAt;
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
