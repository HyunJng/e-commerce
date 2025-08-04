package kr.hhplus.be.server.small.wallet.usecase;

import kr.hhplus.be.server.wallet.application.usecase.GetWalletBalanceUseCase;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본지갑;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

class GetWalletBalanceUseCaseTest {

    @InjectMocks
    private GetWalletBalanceUseCase getWalletBalanceUseCase;

    @Mock
    private WalletJpaRepository walletJpaRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 유저_아이디를_통해_유저의_잔액을_조회할_수_있다() {
        // given
        Wallet wallet = 기본지갑();

        Long userId = wallet.getUserId();
        Long balance = 1000L;

        GetWalletBalanceUseCase.Input input = new GetWalletBalanceUseCase.Input(userId);
        given(walletJpaRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        // when
        GetWalletBalanceUseCase.Output output = getWalletBalanceUseCase.execute(input);

        // then
        assertNotNull(output);
        assertEquals(userId, output.userId());
        assertEquals(balance, output.balance());
    }
}