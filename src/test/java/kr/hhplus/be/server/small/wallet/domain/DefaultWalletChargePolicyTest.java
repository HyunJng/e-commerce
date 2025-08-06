package kr.hhplus.be.server.small.wallet.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.domain.DefaultWalletChargePolicy;
import kr.hhplus.be.server.wallet.domain.domain.WalletChargePolicy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultWalletChargePolicyTest {

    private final WalletChargePolicy walletChargePolicy  = new DefaultWalletChargePolicy();

    @ParameterizedTest
    @ValueSource(longs = {-1000, 0, 999})
    void 충전은_1000원_이하의_금액을_제한한다(Long amount) throws Exception {
        // when & then
        assertThatThrownBy(() -> walletChargePolicy.validate(amount))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.INVALID_POLICY.getMessage("충전 금액은 1000원 이상이어야 합니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {1000, 2000, 5000})
    void 충전은_1000원_이상의_금액은_충전이_가능하다(Long amount) throws Exception {
        // when & then
        Assertions.assertThatCode(() -> walletChargePolicy.validate(amount))
                .doesNotThrowAnyException();
    }

}