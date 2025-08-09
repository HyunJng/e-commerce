package kr.hhplus.be.server.coupon.domain.event;

import lombok.Getter;

@Getter
public class IssuedCouponEvent {
    private Long couponId;
    private Long userId;

    public IssuedCouponEvent(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
    }
}
