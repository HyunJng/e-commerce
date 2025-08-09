package kr.hhplus.be.server.wallet.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.repository.WalletLockLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletCommandService {

    private final WalletLockLoader walletLockLoader;

    public void use(Long userId, Long payAmount) {
        Wallet wallet = walletLockLoader.findByUserId(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));
        wallet.pay(payAmount);
    }
}
