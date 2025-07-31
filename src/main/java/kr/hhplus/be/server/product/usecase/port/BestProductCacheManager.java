package kr.hhplus.be.server.product.usecase.port;

import kr.hhplus.be.server.product.domain.Product;

import java.util.List;

public interface BestProductCacheManager {

    void save(List<Product> bestProducts);

    List<Product> get();
}
