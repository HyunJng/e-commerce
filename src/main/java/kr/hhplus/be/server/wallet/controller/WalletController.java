package kr.hhplus.be.server.wallet.controller;

import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.wallet.controller.docs.WalletApiSpec;
import kr.hhplus.be.server.wallet.controller.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.controller.dto.WalletViewApi;
import kr.hhplus.be.server.wallet.usecase.ChargeWalletBalanceService;
import kr.hhplus.be.server.wallet.usecase.GetWalletBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/wallet")
public class WalletController implements WalletApiSpec {

    private final GetWalletBalanceService getWalletBalanceService;
    private final ChargeWalletBalanceService chargeWalletBalanceService;

    @GetMapping
    public DataResponse<WalletViewApi.Response> walletView(WalletViewApi.Request request) {
        GetWalletBalanceService.Output result = getWalletBalanceService.execute(request.to());
        return DataResponse.success(WalletViewApi.Response.from(result));
    }

    @PostMapping("/charge")
    public DataResponse<WalletChargeApi.Response> walletCharge(@RequestBody WalletChargeApi.Request request) {
        ChargeWalletBalanceService.Output result = chargeWalletBalanceService.execute(request.to());
        return DataResponse.success(WalletChargeApi.Response.from(result));
    }
}
