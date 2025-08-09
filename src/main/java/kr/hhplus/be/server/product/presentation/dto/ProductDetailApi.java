package kr.hhplus.be.server.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.product.presentation.docs.ProductSchemaDescription;
import kr.hhplus.be.server.product.application.usecase.GetProductDetailUseCase;

public class ProductDetailApi {

    public record Response(
            @Schema(description = ProductSchemaDescription.id) Long id,
            @Schema(description = ProductSchemaDescription.name) String name,
            @Schema(description = ProductSchemaDescription.price) Long price,
            @Schema(description = ProductSchemaDescription.quantity) Integer quantity
    ) {
        public static Response from (GetProductDetailUseCase.Output output){
            return new Response(
                    output.id(),
                    output.name(),
                    output.price(),
                    output.quantity()
            );
        }
    }
}
