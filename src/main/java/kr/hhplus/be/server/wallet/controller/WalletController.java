package kr.hhplus.be.server.wallet.controller;

import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.wallet.controller.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.controller.dto.WalletViewApi;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/me/wallet")
@RestController
public class WalletController {

    @GetMapping
    public CommonResponse<WalletViewApi.Response> walletView(WalletViewApi.Request request) {
        return CommonResponse.success(
                new WalletViewApi.Response(
                        request.userId(),
                        1000L
                )
        );
    }

    @PostMapping("/charge")
    public CommonResponse<WalletChargeApi.Response> walletCharge(@RequestBody WalletChargeApi.Request request) {
        return CommonResponse.success(
                new WalletChargeApi.Response(
                        request.userId(),
                        1000 + request.amount()
                )
        );
    }
}
