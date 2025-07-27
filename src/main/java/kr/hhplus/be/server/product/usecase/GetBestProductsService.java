package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBestProductsService {

    public record Output(
            List<ProductInfo> products
    ) {
        public record ProductInfo(
                Long id,
                String name,
                Long price
        ) {
        }
    }

    private final ProductJpaRepository productJpaRepository;

    public Output execute() {
        List<Product> bestProducts = productJpaRepository.findBestProducts();
        List<Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new Output.ProductInfo(product.getId(), product.getName(), product.getPrice()))
                .toList();
        return new Output(productInfos);
    }
}
