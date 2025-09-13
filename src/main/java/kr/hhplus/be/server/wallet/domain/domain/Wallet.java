package kr.hhplus.be.server.wallet.domain.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "wallets")
public class Wallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @Column(name = "balance")
    private Long balance;

    @Builder
    public Wallet(Long userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Wallet() {
    }

    public void charge(Long amount, WalletChargePolicy walletChargePolicy) {
        walletChargePolicy.validate(amount);

        this.balance += amount;
        // TODO: 이벤트발행하여 history 적재 구현하기
    }

    public void pay(Long paidAmount) {
        if (this.balance < paidAmount) {
            throw new CommonException(ErrorCode.INVALID_REQUEST, "잔액 부족");
        }
    }
}
