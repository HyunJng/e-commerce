package kr.hhplus.be.server.coupon.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.coupon.application.usecase.IssuedCouponUseCase;
import kr.hhplus.be.server.coupon.presentation.docs.CouponSchemaDescription;

public class CouponIssueApi {

    public record Request(
            @Schema(description = CouponSchemaDescription.userId) Long userId,
            @Schema(description = CouponSchemaDescription.couponId) Long couponId
    ) {
        public IssuedCouponUseCase.Input to() {
            return new IssuedCouponUseCase.Input(couponId, userId);
        }
    }

    public record Response(
            @Schema(description = CouponSchemaDescription.isSuccess) boolean isSuccess
    ) {
        public static Response from(IssuedCouponUseCase.Output output) {
            return new Response(output.isSuccess());
        }
    }
}
