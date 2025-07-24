package kr.hhplus.be.server.small.wallet.usecase;

import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import kr.hhplus.be.server.wallet.usecase.GetWalletBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

class GetWalletBalanceServiceTest {

    @InjectMocks
    private GetWalletBalanceService getWalletBalanceService;

    @Mock
    private WalletJpaRepository walletJpaRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 유저_아이디를_통해_유저의_잔액을_조회할_수_있다() {
        // given
        Long userId = 1L;
        Long balance = 1000L;
        Wallet wallet = new Wallet(1L, userId, balance, null, null);

        GetWalletBalanceService.Input input = new GetWalletBalanceService.Input(userId);
        given(walletJpaRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        // when
        GetWalletBalanceService.Output output = getWalletBalanceService.execute(input);

        // then
        assertNotNull(output);
        assertEquals(userId, output.userId());
        assertEquals(balance, output.balance());
    }
}