package kr.hhplus.be.server.small.wallet.application.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletChargePolicy;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import kr.hhplus.be.server.wallet.application.usecase.ChargeWalletBalanceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static kr.hhplus.be.server.mock.DomainTestFixtures.기본지갑;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class ChargeWalletBalanceUseCaseTest {

    @InjectMocks
    private ChargeWalletBalanceUseCase chargeWalletBalanceUseCase;

    @Mock
    private WalletJpaRepository walletJpaRepository;
    @Mock
    private WalletChargePolicy walletChargePolicy;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 사용자ID와_충전금액을_요청하면_충전된_잔액을_반환한다() throws Exception {
        // given
        Wallet wallet = 기본지갑();

        Long userId = wallet.getUserId();
        Long amount = 2000L;

        ChargeWalletBalanceUseCase.Input input = new ChargeWalletBalanceUseCase.Input(
                userId,
                amount
        );

        given(walletJpaRepository.findByUserId(userId)).willReturn(
                Optional.of(wallet)
        );
        given(walletJpaRepository.save(any(Wallet.class))).willAnswer(
                invocation -> invocation.getArgument(0)
        );

        // when
        ChargeWalletBalanceUseCase.Output output = chargeWalletBalanceUseCase.execute(input);

        // then
        assertThat(output).isNotNull();
        assertThat(output.userId()).isEqualTo(userId);
        assertThat(output.balance()).isEqualTo(3000L);
    }

    @Test
    void 유효하지_않은_유저가_충전요청을_보내면_오류를_반환한다() throws Exception {
        // given
        Long userId = 1000L;

        ChargeWalletBalanceUseCase.Input input = new ChargeWalletBalanceUseCase.Input(
                userId,
                1000L
        );

        given(walletJpaRepository.findByUserId(userId)).willReturn(
                Optional.empty()
        );

        // when & then
        assertThatThrownBy(() -> chargeWalletBalanceUseCase.execute(input))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("지갑"));
    }

    @Test
    void 충전_요청_중_충전요청_정책이_호출된다() throws Exception {
        // given
        Wallet wallet = 기본지갑();

        Long userId = wallet.getUserId();
        Long amount = 2000L;

        ChargeWalletBalanceUseCase.Input input = new ChargeWalletBalanceUseCase.Input(
                userId,
                amount
        );

        given(walletJpaRepository.findByUserId(userId)).willReturn(
                Optional.of(wallet)
        );
        given(walletJpaRepository.save(any(Wallet.class))).willAnswer(
                invocation -> invocation.getArgument(0)
        );

        // when
        chargeWalletBalanceUseCase.execute(input);

        // then
        verify(walletChargePolicy).validate(amount);
    }
}