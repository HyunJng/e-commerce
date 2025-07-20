package kr.hhplus.be.server.product.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.product.controller.docs.ProductSchemaDescription;

public class ProductDetail {

    public record Response(
            @Schema(description = ProductSchemaDescription.id) Long id,
            @Schema(description = ProductSchemaDescription.name) String name,
            @Schema(description = ProductSchemaDescription.price) Integer price,
            @Schema(description = ProductSchemaDescription.quantity) Integer quantity
    ) {
    }
}
