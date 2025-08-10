package kr.hhplus.be.server.coupon.presentation.controller;

import kr.hhplus.be.server.coupon.presentation.docs.CouponApiSpec;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import kr.hhplus.be.server.coupon.application.usecase.IssueCouponUseCase;
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

    private final IssueCouponUseCase issueCouponUseCase;

    @PostMapping("/issue")
    public ResponseEntity<CouponIssueApi.Response> issued(@RequestBody CouponIssueApi.Request request) {
        IssueCouponUseCase.Output output = issueCouponUseCase.execute(request.to());

        return ResponseEntity.ok(CouponIssueApi.Response.from(output));
    }
}
