package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.coupon.controller.docs.CouponApiSpec;
import kr.hhplus.be.server.coupon.controller.dto.CouponIssueApi;
import kr.hhplus.be.server.coupon.usecase.IssuedCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponApiSpec {

    private final IssuedCouponService issuedCouponService;

    @PostMapping("/issue")
    public ResponseEntity<CouponIssueApi.Response> issued(@RequestBody CouponIssueApi.Request request) {
        IssuedCouponService.Output output = issuedCouponService.execute(request.to());

        return ResponseEntity.ok(CouponIssueApi.Response.from(output));
    }
}
