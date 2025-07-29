package kr.hhplus.be.server.medium.product;

import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/product-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void 상품_조회를_요청이_성공하면_데이터베이스_에서_상품정보를_가져와_응답한다() throws Exception {
        // given
        Long productId = 1L;

        Product product = productJpaRepository.findById(productId).get();

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value(product.getName()))
                .andExpect(jsonPath("$.price").value(product.getPrice()))
                .andExpect(jsonPath("$.quantity").value(product.getQuantity()));
    }
}
