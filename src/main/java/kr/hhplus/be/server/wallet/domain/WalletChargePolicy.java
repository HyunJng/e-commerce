package kr.hhplus.be.server.wallet.domain;

public interface WalletChargePolicy {

    void validate(Long amount);
}
