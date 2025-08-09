package kr.hhplus.be.server.medium.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.wallet.domain.repository.WalletJpaRepository;
import kr.hhplus.be.server.wallet.presentation.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/wallet-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class WalletIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WalletJpaRepository walletJpaRepository;

    @Test
    void 지갑_조회를_요청이_성공하면_데이터베이스_에서_지갑정보를_가져와_응답한다() throws Exception {
        //given
        Long userId = 1L;

        var wallet = walletJpaRepository.findByUserId(userId).orElseThrow();

        //when & then
        mockMvc.perform(get("/api/v1/me/wallet")
                        .param("userId", String.valueOf(userId))
                )
                .andExpect(jsonPath("$.userId").value(wallet.getUserId()))
                .andExpect(jsonPath("$.balance").value(wallet.getBalance()));
    }

    @Test
    void 충전_요청이_성공하면_요청금액만큼_잔액이_증가한다() throws Exception {
        // given
        Long userId = 1L;

        WalletChargeApi.Request request = new WalletChargeApi.Request(userId, 1000L);
        String content = objectMapper.writeValueAsString(request);

        Wallet beforeWallet = walletJpaRepository.findByUserId(userId).get();

        // when
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        Wallet afterWallet = walletJpaRepository.findByUserId(userId).orElse(null);

        assertThat(afterWallet).isNotNull();
        assertThat(afterWallet.getBalance()).isEqualTo(beforeWallet.getBalance() + request.amount());
        assertThat(afterWallet.getUserId()).isEqualTo(beforeWallet.getUserId());
        assertThat(afterWallet.getId()).isEqualTo(beforeWallet.getId());
        assertThat(afterWallet.getCreateAt()).isEqualTo(beforeWallet.getCreateAt());
        assertThat(afterWallet.getUpdateAt()).isAfter(beforeWallet.getUpdateAt());
    }

    @Test
    void 충전_요청이_실패하면_기존_잔액이_유지된다() throws Exception {
        // given
        Long userId = 1L;

        WalletChargeApi.Request request = new WalletChargeApi.Request(userId, 100L); // 충전 정책 위반
        String content = objectMapper.writeValueAsString(request);

        Wallet beforeWallet = walletJpaRepository.findByUserId(userId).get();

        // when
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        // then
        Wallet afterWallet = walletJpaRepository.findByUserId(userId).orElse(null);

        assertThat(afterWallet).isNotNull();
        assertThat(afterWallet.getBalance()).isEqualTo(beforeWallet.getBalance());
        assertThat(afterWallet.getUserId()).isEqualTo(beforeWallet.getUserId());
        assertThat(afterWallet.getId()).isEqualTo(beforeWallet.getId());
        assertThat(afterWallet.getCreateAt()).isEqualTo(beforeWallet.getCreateAt());
        assertThat(afterWallet.getUpdateAt()).isEqualTo(beforeWallet.getUpdateAt());
    }
}
