package kr.hhplus.be.server.large.wallet;

import kr.hhplus.be.server.large.AbstractConCurrencyTest;
import kr.hhplus.be.server.wallet.application.usecase.ChargeWalletBalanceUseCase;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.repository.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/wallet-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class ChargeWalletBalanceUseCaseConcurrencyTest extends AbstractConCurrencyTest {

    @Autowired
    private ChargeWalletBalanceUseCase chargeWalletBalanceUseCase;
    @Autowired
    private WalletJpaRepository walletJpaRepository;

    @Test
    void 같은유저의_충전요청이_동시에_들어오면_순차적으로_요청이_처리된다() throws Exception {
        // given
        Long userId = 1L;

        // when
        AbstractConCurrencyTest.runConcurrentTest(2, i -> {
            ChargeWalletBalanceUseCase.Input input = new ChargeWalletBalanceUseCase.Input(userId, 1000L);
            chargeWalletBalanceUseCase.execute(input);
        });

        // then
        Wallet wallet = walletJpaRepository.findByUserId(userId).orElse(null);

        assertThat(wallet).isNotNull();
        assertThat(wallet.getBalance()).isEqualTo(2000);
    }
}