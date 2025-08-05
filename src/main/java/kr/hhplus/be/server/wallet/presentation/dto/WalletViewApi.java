package kr.hhplus.be.server.wallet.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.wallet.presentation.docs.WalletSchemaDescription;
import kr.hhplus.be.server.wallet.application.usecase.GetWalletBalanceUseCase;

public class WalletViewApi {

    public record Request(
            @Schema(description = WalletSchemaDescription.userId) Long userId
    ) {
        public GetWalletBalanceUseCase.Input to() {
            return new GetWalletBalanceUseCase.Input(userId);
        }
    }

    public record Response(
            @Schema(description = WalletSchemaDescription.userId) Long userId,
            @Schema(description = WalletSchemaDescription.balance) Long balance) {

        public static Response from(GetWalletBalanceUseCase.Output result) {
            return new Response(result.userId(), result.balance());
        }
    }
}
