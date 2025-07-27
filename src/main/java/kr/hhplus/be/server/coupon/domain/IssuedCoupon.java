package kr.hhplus.be.server.coupon.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.response.ResultCode;
import kr.hhplus.be.server.common.time.DateHolder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "issued_coupons")
public class IssuedCoupon {
    public enum Status {ACTIVE, USED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long couponId;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private Status state;

    @Column(name = "order_id")
    private Long orderId;

    public IssuedCoupon(Coupon coupon, Long userId, DateHolder dateHolder) {
        this.userId = userId;
        this.couponId = coupon.getId();
        this.issuedAt = dateHolder.now();
        this.startAt = dateHolder.today();
        this.endAt = startAt.plusDays(coupon.getDates());
        this.state = Status.ACTIVE;
    }

    public IssuedCoupon() {}

    public void validate(DateHolder dateHolder) {
        if (!(state == Status.ACTIVE &&
                ((startAt.isAfter(dateHolder.today()) &&
                endAt.isBefore(dateHolder.today())) ||
                (startAt.isEqual(dateHolder.today())
                        || endAt.isEqual(dateHolder.today())
                ))
        )) {
            throw new CommonException(ResultCode.INVALID_REQUEST, "유효하지 않은 쿠폰");
        }
    }

    public void use(Long orderId) {
        this.state = Status.USED;
        this.orderId = orderId;
    }
}
