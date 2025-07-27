package kr.hhplus.be.server.wallet.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.response.ResultCode;
import kr.hhplus.be.server.common.time.DateHolder;
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

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Builder
    public Wallet(Long id, Long userId, Long balance, LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public Wallet() {
    }

    public void charge(Long amount, WalletChargePolicy walletChargePolicy) {
        walletChargePolicy.validate(amount);

        this.balance += amount;
        this.updateAt = LocalDateTime.now();

        // TODO: 이벤트발행하여 history 적재 구현하기
    }

    public void pay(Long paidAmount, DateHolder dateHolder) {
        if (this.balance < paidAmount) {
            throw new CommonException(ResultCode.INVALID_REQUEST, "잔액 부족");
        }
        this.balance -= paidAmount;
        this.updateAt = dateHolder.now();
    }
}
