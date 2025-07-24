package kr.hhplus.be.server.wallet.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.response.ResultCode;
import org.springframework.stereotype.Component;

@Component
public class DefaultWalletChargePolicy implements WalletChargePolicy {

    @Override
    public void validate(Long amount) {
        if(amount < 1000) {
            throw new CommonException(ResultCode.INVALID_POLICY, "충전 금액은 1000원 이상이어야 합니다.");
        }
    }
}
