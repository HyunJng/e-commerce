package kr.hhplus.be.server.coupon.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import org.springframework.http.ResponseEntity;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponApiSpec {

    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰을 발급합니다.")
    ResponseEntity<CouponIssueApi.Response> issued(CouponIssueApi.Request request);

}
