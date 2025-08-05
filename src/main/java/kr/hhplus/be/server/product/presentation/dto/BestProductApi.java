package kr.hhplus.be.server.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.product.presentation.docs.ProductSchemaDescription;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;

public class BestProductApi {

    public record Response(
            @Schema(description = ProductSchemaDescription.id) Long id,
            @Schema(description = ProductSchemaDescription.name) String name,
            @Schema(description = ProductSchemaDescription.price) Long price
    ) {

        public static Response from(FindBestProductsUseCase.Output.ProductInfo productInfo) {
            return new Response(
                    productInfo.id(),
                    productInfo.name(),
                    productInfo.price()
            );
        }
    }
}
