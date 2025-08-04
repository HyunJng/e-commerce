package kr.hhplus.be.server.product.application.port;

import kr.hhplus.be.server.product.domain.Product;

import java.util.List;

public interface BestProductCacheReader {

    List<Product> get();
}
