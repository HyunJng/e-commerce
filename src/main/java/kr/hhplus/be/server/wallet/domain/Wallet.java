package kr.hhplus.be.server.wallet.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", insertable = false, updatable = false)
    private LocalDateTime updateAt;

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
        this.balance -= paidAmount;
    }
}
