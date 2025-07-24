package kr.hhplus.be.server.wallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.wallet.controller.docs.WalletSchemaDescription;
import kr.hhplus.be.server.wallet.usecase.ChargeWalletBalanceService;

public class WalletChargeApi {

    public record Request(
            @Schema(description = WalletSchemaDescription.userId) Long userId,
            @Schema(description = WalletSchemaDescription.amount) Long amount
    ) {
        public ChargeWalletBalanceService.Input to() {
            return new ChargeWalletBalanceService.Input(userId, amount);
        }
    }


    public record Response(
            @Schema(description = WalletSchemaDescription.userId) Long userId,
            @Schema(description = WalletSchemaDescription.balance) Long balance
    ) {
        public static Response from(ChargeWalletBalanceService.Output result) {
            return new Response(
                    result.userId(),
                    result.balance()
            );
        }
    }
}
