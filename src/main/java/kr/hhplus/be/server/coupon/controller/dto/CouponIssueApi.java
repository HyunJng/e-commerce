package kr.hhplus.be.server.coupon.controller.dto;

import java.time.LocalDateTime;

public class CouponIssueApi {

    public record Request(
            Long userId,
            Long couponId
    ) {

    }

    public record Response(
            Long id,
            Long couponId,
            String couponName,
            Long discountAmount,
            String discountType,
            LocalDateTime startedAt,
            LocalDateTime endAt
    ) {

    }
}
