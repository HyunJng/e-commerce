package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.product.controller.dto.ProductDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    @GetMapping("/{id}")
    public CommonResponse<ProductDetail.Response> viewDetail(@PathVariable Long id) {
        return CommonResponse.success(
                new ProductDetail.Response(id, "테스트상품", 1000, 10)
        );
    }
}
