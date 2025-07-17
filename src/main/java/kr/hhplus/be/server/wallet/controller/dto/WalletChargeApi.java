package kr.hhplus.be.server.wallet.controller.dto;

public class WalletChargeApi {

    public record Request(Long userId, Long amount) {

    }

    public record Response(Long userId, Long balance) {
    }
}
