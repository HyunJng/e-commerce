package kr.hhplus.be.server.small.wallet.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.GeneralExceptionAdvice;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.wallet.presentation.controller.WalletController;
import kr.hhplus.be.server.wallet.presentation.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.application.usecase.ChargeWalletBalanceUseCase;
import kr.hhplus.be.server.wallet.application.usecase.GetWalletBalanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@Import(value = {GeneralExceptionAdvice.class})
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetWalletBalanceUseCase getWalletBalanceUseCase;
    @MockitoBean
    private ChargeWalletBalanceUseCase chargeWalletBalanceUseCase;

    @Test
    void 잔액확인에_성공하면_200응답과_지갑정보를_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long balance = 1000L;

        given(getWalletBalanceUseCase.execute(new GetWalletBalanceUseCase.Input(userId)))
                .willReturn(new GetWalletBalanceUseCase.Output(userId, balance));

        // when & then
        mockMvc.perform(get("/api/v1/me/wallet")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    void 잔액확인요청이_존재하지않는_유저라면_400응답과_오류상세내역을_반환한다() throws Exception {
        // given
        given(getWalletBalanceUseCase.execute(any()))
                .willThrow(new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));

        // when & then
        mockMvc.perform(get("/api/v1/me/wallet")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCd").value(ErrorCode.NOT_FOUND_RESOURCE.getCode()))
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.NOT_FOUND_RESOURCE.getMessage("지갑")));
    }

    @Test
    void 충전에_성공하면_200응답과_잔액정보를_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long balance = 2000L;

        WalletChargeApi.Request request = new WalletChargeApi.Request(userId, 1000L);
        String content = objectMapper.writeValueAsString(request);

        given(chargeWalletBalanceUseCase.execute(new ChargeWalletBalanceUseCase.Input(request.userId(), request.amount())))
                .willReturn(new ChargeWalletBalanceUseCase.Output(userId, balance));

        // when & then
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    void 충전요청이_존재하지않는_유저라면_400응답과_오류상세내역을_반환한다() throws Exception {
        // given
        WalletChargeApi.Request request = new WalletChargeApi.Request(1L, 1000L);
        String content = objectMapper.writeValueAsString(request);

        given(chargeWalletBalanceUseCase.execute(any()))
                .willThrow(new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));

        // when & then
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCd").value(ErrorCode.NOT_FOUND_RESOURCE.getCode()))
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.NOT_FOUND_RESOURCE.getMessage("지갑")));
    }

    @Test
    void 충전_정책에_어긋난_요청이면_400응답과_오류상세내역을_반환한다() throws Exception {
        // given
        WalletChargeApi.Request request = new WalletChargeApi.Request(1L, 1000L);
        String content = objectMapper.writeValueAsString(request);

        given(chargeWalletBalanceUseCase.execute(any()))
                .willThrow(new CommonException(ErrorCode.INVALID_POLICY));

        // when & then
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCd").value(ErrorCode.INVALID_POLICY.getCode()))
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_POLICY.getMessage()));
    }
}