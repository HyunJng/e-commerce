package kr.hhplus.be.server.wallet.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletCommandService {

    private final WalletJpaRepository walletJpaRepository;

    public void use(Long userId, Long payAmount) {
        Wallet wallet = walletJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));
        wallet.pay(payAmount);
    }
}
