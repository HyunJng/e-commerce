package kr.hhplus.be.server.wallet.application.usecase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.domain.WalletChargePolicy;
import kr.hhplus.be.server.wallet.domain.repository.WalletLockLoader;
import kr.hhplus.be.server.wallet.domain.repository.WalletJpaRepository;
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
        public static Output from(Wallet wallet) {
            return new Output(
                    wallet.getUserId(),
                    wallet.getBalance()
            );
        }
    }

    private final WalletLockLoader walletLockLoader;
    private final WalletJpaRepository walletJpaRepository;
    private final WalletChargePolicy walletChargePolicy;

    @Transactional
    public Output execute(Input input) {
        Long userId = input.userId;

        Wallet wallet = walletLockLoader.findByUserId(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));

        wallet.charge(input.amount, walletChargePolicy);

        Wallet save = walletJpaRepository.save(wallet);

        return Output.from(save);
    }
}
