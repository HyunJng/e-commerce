package kr.hhplus.be.server.product.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.product.controller.dto.BestProductApi;
import kr.hhplus.be.server.product.controller.dto.ProductDetailApi;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductApiSpec {

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    ResponseEntity<ProductDetailApi.Response> viewDetail(@Parameter(description = "상품 아이디")Long id);

    @Operation(summary = "인기 판매 상품 조회", description = "인기 판매 상품 5개를 조회합니다.")
    ResponseEntity<List<BestProductApi.Response>> viewBest();
}
