package kr.hhplus.be.server.small.wallet.domain;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.wallet.domain.Wallet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본지갑;
import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    @ParameterizedTest
    @ValueSource(longs = {500L, 999L, 1000L})
    void 요청금액이_잔액보다_많으면_잔액을_감소시킨다(long paidAmount) throws Exception {
        // given
        Wallet wallet = 기본지갑();
        DateHolder dateHolder = Mockito.mock(DateHolder.class);

        // when
        wallet.pay(paidAmount, dateHolder);

        // then
        assertThat(wallet.getBalance()).isEqualTo(1000L - paidAmount);
    }

    @ParameterizedTest
    @ValueSource(longs = {1001L, 2000L})
    void 잔액이_부족하면_오류를_발생시킨다(long paidAmount) throws Exception {
        // given
        Wallet wallet = 기본지갑();
        DateHolder dateHolder = Mockito.mock(DateHolder.class);

        // when & then
        Assertions.assertThatThrownBy(() -> wallet.pay(paidAmount, dateHolder))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.INVALID_REQUEST.getMessage("잔액 부족"));
    }

}