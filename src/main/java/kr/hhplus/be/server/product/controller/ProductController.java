package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.product.controller.docs.ProductApiSpec;
import kr.hhplus.be.server.product.controller.dto.BestProductApi;
import kr.hhplus.be.server.product.controller.dto.ProductDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/products")
@RestController
public class ProductController implements ProductApiSpec {

    @GetMapping("/{id}")
    public CommonResponse<ProductDetail.Response> viewDetail(@PathVariable Long id) {
        return CommonResponse.success(
                new ProductDetail.Response(id, "테스트상품", 1000, 10)
        );
    }

    @GetMapping("/best")
    public CommonResponse<List<BestProductApi.Response>> viewBest() {
        return CommonResponse.success(
                List.of(
                        new BestProductApi.Response(1L, "FIRST", 1000),
                        new BestProductApi.Response(2L, "SECOND", 2000),
                        new BestProductApi.Response(3L, "THIRD", 3000),
                        new BestProductApi.Response(4L, "FOURTH", 4000),
                        new BestProductApi.Response(5L, "FIFTH", 5000)
                )
        );
    }
}
