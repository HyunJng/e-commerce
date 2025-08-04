package kr.hhplus.be.server.wallet.application.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWalletBalanceUseCase {

    public record Input(
            Long userId
    ) {}

    public record Output(
            Long userId,
            Long balance
    ) {}

    private final WalletJpaRepository walletJpaRepository;

    public Output execute(Input input) {
        Long userId = input.userId();

        Wallet wallet = walletJpaRepository.findByUserId(userId).orElseThrow(
                () -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑")
        );

        return new Output(userId, wallet.getBalance());
    }
}
