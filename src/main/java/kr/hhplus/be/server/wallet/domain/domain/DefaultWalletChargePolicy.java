package kr.hhplus.be.server.wallet.domain.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class DefaultWalletChargePolicy implements WalletChargePolicy {

    @Override
    public void validate(Long amount) {
        if(amount < 1000) {
            throw new CommonException(ErrorCode.INVALID_POLICY, "충전 금액은 1000원 이상이어야 합니다.");
        }
    }
}
