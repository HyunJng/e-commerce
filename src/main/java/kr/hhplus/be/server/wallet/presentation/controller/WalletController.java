package kr.hhplus.be.server.wallet.presentation.controller;

import kr.hhplus.be.server.wallet.presentation.docs.WalletApiSpec;
import kr.hhplus.be.server.wallet.presentation.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.presentation.dto.WalletViewApi;
import kr.hhplus.be.server.wallet.application.usecase.ChargeWalletBalanceUseCase;
import kr.hhplus.be.server.wallet.application.usecase.GetWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/wallet")
public class WalletController implements WalletApiSpec {

    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final ChargeWalletBalanceUseCase chargeWalletBalanceUseCase;

    @GetMapping
    public ResponseEntity<WalletViewApi.Response> walletView(WalletViewApi.Request request) {
        GetWalletBalanceUseCase.Output result = getWalletBalanceUseCase.execute(request.to());
        return ResponseEntity.ok(WalletViewApi.Response.from(result));
    }

    @PostMapping("/charge")
    public ResponseEntity<WalletChargeApi.Response> walletCharge(@RequestBody WalletChargeApi.Request request) {
        ChargeWalletBalanceUseCase.Output result = chargeWalletBalanceUseCase.execute(request.to());
        return ResponseEntity.ok(WalletChargeApi.Response.from(result));
    }
}
