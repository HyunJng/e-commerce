package kr.hhplus.be.server.product.controller.dto;

public class BestProductApi {

    public record Response(
            Long id,
            String name,
            Integer price
    ) {

    }
}
