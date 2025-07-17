package kr.hhplus.be.server.coupon.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.coupon.controller.docs.CouponSchemaDescription;

import java.time.LocalDateTime;

public class CouponIssueApi {

    public record Request(
            @Schema(description = CouponSchemaDescription.userId) Long userId,
            @Schema(description = CouponSchemaDescription.couponId) Long couponId
    ) {

    }

    public record Response(
            @Schema(description = CouponSchemaDescription.issuedId) Long id,
            @Schema(description = CouponSchemaDescription.couponId) Long couponId,
            @Schema(description = CouponSchemaDescription.couponName) String couponName,
            @Schema(description = CouponSchemaDescription.discountAmount) Long discountAmount,
            @Schema(description = CouponSchemaDescription.discountType) String discountType,
            @Schema(description = CouponSchemaDescription.startedAt) LocalDateTime startedAt,
            @Schema(description = CouponSchemaDescription.endAt) LocalDateTime endAt
    ) {

    }
}
