package kr.hhplus.be.server.product.application.port;

import kr.hhplus.be.server.product.domain.entity.Product;

import java.util.List;

public interface BestProductCacheWriter {

    List<Product> update();
}
