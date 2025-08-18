package kr.hhplus.be.server.product.application.usecase;

import kr.hhplus.be.server.product.application.port.BestProductCacheReader;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
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

    public Output execute() {
        List<BestProduct> bestProducts = bestProductCacheReader.get();

        List<Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new Output.ProductInfo(product.id(), product.name(), product.price()))
                .toList();
        return new Output(productInfos);
    }
}