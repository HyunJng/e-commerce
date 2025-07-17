package kr.hhplus.be.server.small.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.wallet.controller.WalletController;
import kr.hhplus.be.server.wallet.controller.dto.WalletChargeApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@WebMvcTest(value = WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원정보를_전달하면_지갑정보를_응답한다() throws Exception {
        //given
        String userId = "1";

        //when & then
        mockMvc.perform(get("/api/v1/me/wallet")
                        .param("userId", userId)
                ).andExpect(jsonPath("$.resultCd").value("0000"))
                .andExpect(jsonPath("$.result.userId").value(userId))
                .andExpect(jsonPath("$.result.balance").value(1000)); //TODO: 변경 필요
    }

    @Test
    void 회원정보와_충전금액을_전달하여_충전할_수_있다() throws Exception {
        //given
        WalletChargeApi.Request request = new WalletChargeApi.Request(1L, 1000L);
        String content = objectMapper.writeValueAsString(request);

        //when & then
        mockMvc.perform(post("/api/v1/me/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                ).andExpect(jsonPath("$.resultCd").value("0000"))
                .andExpect(jsonPath("$.result.userId").value(request.userId()))
                .andExpect(jsonPath("$.result.balance").value(1000 + request.amount())); //TODO: 변경 필요
    }
}