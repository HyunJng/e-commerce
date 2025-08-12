package kr.hhplus.be.server.product.domain.entity;

public record BestProduct(Long id,
                          String name,
                          Long price,
                          Integer quantity,
                          Long orderCount) {

}
