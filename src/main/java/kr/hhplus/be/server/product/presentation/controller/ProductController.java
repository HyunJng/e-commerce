package kr.hhplus.be.server.product.presentation.controller;

import kr.hhplus.be.server.product.presentation.docs.ProductApiSpec;
import kr.hhplus.be.server.product.presentation.dto.BestProductApi;
import kr.hhplus.be.server.product.presentation.dto.ProductDetailApi;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;
import kr.hhplus.be.server.product.application.usecase.GetProductDetailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApiSpec {

    private final GetProductDetailUseCase getProductDetailUseCase;
    private final FindBestProductsUseCase findBestProductsUsecase;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailApi.Response> viewDetail(@PathVariable Long id) {
        GetProductDetailUseCase.Output output = getProductDetailUseCase.execute(new GetProductDetailUseCase.Input(id));

        return ResponseEntity.ok(ProductDetailApi.Response.from(output));
    }

    @GetMapping("/best")
    public ResponseEntity<List<BestProductApi.Response>> viewBest() {
        FindBestProductsUseCase.Output output = findBestProductsUsecase.execute();
        return ResponseEntity.ok(output.products().stream()
                .map(BestProductApi.Response::from)
                .toList());
    }
}
