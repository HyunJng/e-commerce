package kr.hhplus.be.server.coupon.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.coupon.controller.dto.CouponIssueApi;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponApiSpec {

    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰을 발급합니다.")
    CommonResponse<CouponIssueApi.Response> issued(CouponIssueApi.Request request);

}
