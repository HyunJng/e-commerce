package kr.hhplus.be.server.mock;

import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerTestFixtures {

    public static ResultActions 기본_성공_포맷_검증(ResultActions actions) throws Exception {
        return actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCd").value("0000"))
                .andExpect(jsonPath("$.resultMsg").value("SUCCESS"));

    }
}
