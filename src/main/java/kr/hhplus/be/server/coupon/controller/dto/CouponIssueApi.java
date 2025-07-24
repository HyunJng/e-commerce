package kr.hhplus.be.server.coupon.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.coupon.controller.docs.CouponSchemaDescription;
import kr.hhplus.be.server.coupon.usecase.IssuedCouponService;

import java.time.LocalDate;

public class CouponIssueApi {

    public record Request(
            @Schema(description = CouponSchemaDescription.userId) Long userId,
            @Schema(description = CouponSchemaDescription.couponId) Long couponId
    ) {
        public IssuedCouponService.Input to() {
            return new IssuedCouponService.Input(couponId, userId);
        }
    }

    public record Response(
            @Schema(description = CouponSchemaDescription.issuedId) Long id,
            @Schema(description = CouponSchemaDescription.couponId) Long couponId,
            @Schema(description = CouponSchemaDescription.couponName) String couponName,
            @Schema(description = CouponSchemaDescription.discountAmount) Long discountAmount,
            @Schema(description = CouponSchemaDescription.discountType) String discountType,
            @Schema(description = CouponSchemaDescription.startedAt) LocalDate startedAt,
            @Schema(description = CouponSchemaDescription.endAt) LocalDate endAt
    ) {
        public static Response from(IssuedCouponService.Output output) {
            return new Response(
                    output.id(),
                    output.couponId(),
                    output.couponName(),
                    output.discountAmount(),
                    output.discountType(),
                    output.startedAt(),
                    output.endAt()
            );
        }
    }
}
