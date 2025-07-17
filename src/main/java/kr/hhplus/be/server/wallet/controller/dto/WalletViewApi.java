package kr.hhplus.be.server.wallet.controller.dto;

public class WalletViewApi {

    public record Request(Long userId) {
    }

    public record Response(Long userId, Long balance) {
    }
}
