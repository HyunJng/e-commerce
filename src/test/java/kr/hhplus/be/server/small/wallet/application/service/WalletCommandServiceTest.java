package kr.hhplus.be.server.small.wallet.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.application.service.WalletCommandService;
import kr.hhplus.be.server.wallet.domain.repository.WalletLockLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

class WalletCommandServiceTest {

    @InjectMocks
    private WalletCommandService walletCommandService;
    @Mock
    private WalletLockLoader walletJpaRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 지갑이_존재하지_않으면_오류를_반환한다() {
        // given
        Long userId = 1L;

        given(walletJpaRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> walletCommandService.use(userId, 1000L))
                .isInstanceOf(CommonException.class)
                .hasMessage(ErrorCode.NOT_FOUND_RESOURCE.getMessage("지갑"));
    }

}