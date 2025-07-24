package kr.hhplus.be.server.small.product.controller;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.GeneralExceptionAdvice;
import kr.hhplus.be.server.common.response.ResultCode;
import kr.hhplus.be.server.product.controller.ProductController;
import kr.hhplus.be.server.product.usecase.GetBestProductsService;
import kr.hhplus.be.server.product.usecase.GetProductDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class})
@Import(value = {GeneralExceptionAdvice.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private GetProductDetailService getProductDetailService;
    @MockitoBean
    private GetBestProductsService getBestProductsService;

    @Test
    void 상품을_성공적으로_조회하면_200응답과_상품_세부사항을_반환한다() throws Exception {
        // given
        Long productId = 1L;
        GetProductDetailService.Output output = new GetProductDetailService.Output(productId, "테스트상품", 100L, 1000);

        given(getProductDetailService.execute(new GetProductDetailService.Input(productId)))
                .willReturn(output);

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(productId))
                .andExpect(jsonPath("$.result.name").value(output.name()))
                .andExpect(jsonPath("$.result.price").value(output.price()))
                .andExpect(jsonPath("$.result.quantity").value(output.quantity()));
    }

    @Test
    void 존재하지않는_상품을_조회하면_400응답과_오류상세내역을_반환한다() throws Exception {
        // given
        given(getProductDetailService.execute(any()))
                .willThrow(new CommonException(ResultCode.NOT_FOUND_RESOURCE, "상품"));

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCd").value(ResultCode.NOT_FOUND_RESOURCE.getCode()))
                .andExpect(jsonPath("$.resultMsg").value(ResultCode.NOT_FOUND_RESOURCE.getMessage("상품")));
    }

    @Test
    void 인기상품을_성공적으로_조회하면_200응답과_상품목록을_반환한다() throws Exception {
        // given
        given(getBestProductsService.execute())
                .willReturn(new GetBestProductsService.Output(
                        List.of(
                                new GetBestProductsService.Output.ProductInfo(1L, "상품1", 1000L),
                                new GetBestProductsService.Output.ProductInfo(2L, "상품2", 2000L),
                                new GetBestProductsService.Output.ProductInfo(3L, "상품3", 3000L),
                                new GetBestProductsService.Output.ProductInfo(4L, "상품4", 4000L),
                                new GetBestProductsService.Output.ProductInfo(5L, "상품5", 5000L)
                        ))
                );

        // when & then
        mockMvc.perform(get("/api/v1/products/best"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].id").value(1L))
                .andExpect(jsonPath("$.result[0].name").value("상품1"))
                .andExpect(jsonPath("$.result[0].price").value(1000L))
                .andExpect(jsonPath("$.result[1].id").value(2L))
                .andExpect(jsonPath("$.result[1].name").value("상품2"))
                .andExpect(jsonPath("$.result[1].price").value(2000L));
    }
}