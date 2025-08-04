package kr.hhplus.be.server.wallet.application.usecase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletChargePolicy;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChargeWalletBalanceUseCase {

    public record Input(
            Long userId,
            Long amount
    ) {
    }

    public record Output(
            Long userId,
            Long balance
    ) {
    }

    private final WalletJpaRepository walletJpaRepository;
    private final WalletChargePolicy walletChargePolicy;

    @Transactional
    public Output execute(Input input) {
        Long userId = input.userId;

        Wallet wallet = walletJpaRepository.findByUserId(userId).orElseThrow(
                () -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑")
        );

        wallet.charge(input.amount, walletChargePolicy);

        Wallet save = walletJpaRepository.save(wallet);

        return new Output(
                save.getUserId(),
                save.getBalance()
        );
    }
}
