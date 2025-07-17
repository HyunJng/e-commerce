package kr.hhplus.be.server.product.controller.dto;

public class ProductDetail {

    public record Response(Long id, String name, Integer price, Integer quantity) {

    }
}
