package kr.hhplus.be.server.wallet.controller;

import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.wallet.controller.docs.WalletApiSpec;
import kr.hhplus.be.server.wallet.controller.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.controller.dto.WalletViewApi;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/me/wallet")
@RestController
public class WalletController implements WalletApiSpec {

    @GetMapping
    public DataResponse<WalletViewApi.Response> walletView(WalletViewApi.Request request) {
        return DataResponse.success(
                new WalletViewApi.Response(
                        request.userId(),
                        1000L
                )
        );
    }

    @PostMapping("/charge")
    public DataResponse<WalletChargeApi.Response> walletCharge(@RequestBody WalletChargeApi.Request request) {
        return DataResponse.success(
                new WalletChargeApi.Response(
                        request.userId(),
                        1000 + request.amount()
                )
        );
    }
}
