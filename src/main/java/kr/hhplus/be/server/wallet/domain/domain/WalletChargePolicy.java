package kr.hhplus.be.server.wallet.domain.domain;

public interface WalletChargePolicy {

    void validate(Long amount);
}
