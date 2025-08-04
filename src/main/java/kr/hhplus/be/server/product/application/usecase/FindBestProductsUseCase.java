package kr.hhplus.be.server.product.application.usecase;

import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.application.port.BestProductCacheWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindBestProductsUseCase {

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

    private final BestProductCacheReader bestProductCacheReader;
    private final BestProductCacheWriter bestProductCacheWriter;

    public Output execute() {
        List<Product> bestProducts = bestProductCacheReader.get();

        if (bestProducts.isEmpty()) {
            bestProducts = bestProductCacheWriter.update();
        }

        List<Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new Output.ProductInfo(product.getId(), product.getName(), product.getPrice()))
                .toList();
        return new Output(productInfos);
    }
}