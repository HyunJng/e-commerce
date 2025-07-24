package kr.hhplus.be.server.product.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.product.controller.docs.ProductSchemaDescription;
import kr.hhplus.be.server.product.usecase.GetBestProductsService;

public class BestProductApi {

    public record Response(
            @Schema(description = ProductSchemaDescription.id) Long id,
            @Schema(description = ProductSchemaDescription.name) String name,
            @Schema(description = ProductSchemaDescription.price) Long price
    ) {

        public static Response from(GetBestProductsService.Output.ProductInfo productInfo) {
            return new Response(
                    productInfo.id(),
                    productInfo.name(),
                    productInfo.price()
            );
        }
    }
}
