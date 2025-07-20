package kr.hhplus.be.server.wallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.wallet.controller.docs.WalletSchemaDescription;

public class WalletViewApi {

    public record Request(
            @Schema(description = WalletSchemaDescription.userId) Long userId
    ) {
    }

    public record Response(
            @Schema(description = WalletSchemaDescription.userId) Long userId,
            @Schema(description = WalletSchemaDescription.balance) Long balance) {
    }
}
