package kr.hhplus.be.server.wallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.wallet.controller.docs.WalletSchemaDescription;
import kr.hhplus.be.server.wallet.usecase.GetWalletBalanceService;

public class WalletViewApi {

    public record Request(
            @Schema(description = WalletSchemaDescription.userId) Long userId
    ) {
        public GetWalletBalanceService.Input to() {
            return new GetWalletBalanceService.Input(userId);
        }
    }

    public record Response(
            @Schema(description = WalletSchemaDescription.userId) Long userId,
            @Schema(description = WalletSchemaDescription.balance) Long balance) {

        public static Response from(GetWalletBalanceService.Output result) {
            return new Response(result.userId(), result.balance());
        }
    }
}
