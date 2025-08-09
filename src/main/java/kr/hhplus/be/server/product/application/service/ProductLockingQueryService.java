package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductLockLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLockingQueryService {

    private final ProductLockLoader productLockLoader;

    public Map<Long, Product> findProducts(List<Long> productIds) {
        return productLockLoader.findAllByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }
}
