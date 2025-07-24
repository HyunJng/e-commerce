package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.product.controller.docs.ProductApiSpec;
import kr.hhplus.be.server.product.controller.dto.BestProductApi;
import kr.hhplus.be.server.product.controller.dto.ProductDetailApi;
import kr.hhplus.be.server.product.usecase.GetBestProductsService;
import kr.hhplus.be.server.product.usecase.GetProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApiSpec {

    private final GetProductDetailService getProductDetailService;
    private final GetBestProductsService getBestProductsService;

    @GetMapping("/{id}")
    public DataResponse<ProductDetailApi.Response> viewDetail(@PathVariable Long id) {
        GetProductDetailService.Output output = getProductDetailService.execute(new GetProductDetailService.Input(id));

        return DataResponse.success(ProductDetailApi.Response.from(output));
    }

    @GetMapping("/best")
    public DataResponse<List<BestProductApi.Response>> viewBest() {
        GetBestProductsService.Output output = getBestProductsService.execute();
        return DataResponse.success(output.products().stream()
                .map(BestProductApi.Response::from)
                .toList());
    }
}
