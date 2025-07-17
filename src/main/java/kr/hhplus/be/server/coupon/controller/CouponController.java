package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.coupon.controller.docs.CouponApiSpec;
import kr.hhplus.be.server.coupon.controller.dto.CouponIssueApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequestMapping("/api/v1/coupons")
@RestController
public class CouponController implements CouponApiSpec {

    @PostMapping("/issue")
    public DataResponse<CouponIssueApi.Response> issued(@RequestBody CouponIssueApi.Request request) {
        LocalDateTime localDateTime = LocalDateTime.of(2025, 7, 18, 2, 0, 0);
        return DataResponse.success(
                new CouponIssueApi.Response(
                        request.userId(),
                        request.couponId(),
                        "회원가입쿠폰",
                        10L,
                        "PERCENT",
                        localDateTime,
                        localDateTime.plusDays(7)
                )
        );
    }

}
