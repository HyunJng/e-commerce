package kr.hhplus.be.server.small.product.controller;

import kr.hhplus.be.server.product.controller.ProductController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static kr.hhplus.be.server.mock.ControllerTestFixtures.기본_성공_포맷_검증;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = ProductController.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 상품아이디로_상품상세정보를_조회할_수_있다() throws Exception {
        //given
        String productId = "1";

        //when & then
        기본_성공_포맷_검증(
                mockMvc.perform(get("/api/v1/products/{id}", productId)
        ))
                .andExpect(jsonPath("$.result.id").value(productId))
                .andExpect(jsonPath("$.result.name").value("테스트상품")) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.price").value(1000)) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.quantity").value(10)); //TODO: 변경 필요
    }

    @Test
    void 인기판매상품을_조회_성공하면_5개의_상품정보가_조회된다() throws Exception {
        //when & then
        기본_성공_포맷_검증(
                mockMvc.perform(get("/api/v1/products/best"))
        )
                .andExpect(jsonPath("$.result", hasSize(5)))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("FIRST"))
                .andExpect(jsonPath("$.result[0].price").value(1000))
                .andExpect(jsonPath("$.result[4].name").value("FIFTH"));
    }
}