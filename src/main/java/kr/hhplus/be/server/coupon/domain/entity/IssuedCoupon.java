package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import kr.hhplus.be.server.common.time.DateHolder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "issued_coupons")
public class IssuedCoupon extends BaseTimeEntity {
    public enum Status {ACTIVE, USED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long couponId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    public IssuedCoupon(Coupon coupon, Long userId, DateHolder dateHolder) {
        this.userId = userId;
        this.couponId = coupon.getId();
        this.startDate = dateHolder.today();
        this.endDate = startDate.plusDays(coupon.getDates());
        this.status = Status.ACTIVE;
    }

    public IssuedCoupon() {}

    public void validate(DateHolder dateHolder) {
        if (status == Status.USED || startDate.isAfter(dateHolder.today()) ||endDate.isBefore(dateHolder.today()))
            throw new CommonException(ErrorCode.INVALID_REQUEST, "유효하지 않은 쿠폰");
    }

    public void use() {
        this.status = Status.USED;
    }
}
