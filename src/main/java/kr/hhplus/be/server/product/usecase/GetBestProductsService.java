package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.usecase.port.BestProductCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    private final BestProductCacheManager bestProductCacheManager;
    private final SaveBestProductInCacheService saveBestProductInCacheService;

    public Output execute() {
        List<Product> bestProducts = Optional.ofNullable(bestProductCacheManager.get()).orElse(List.of());

        if (bestProducts.isEmpty()) {
            saveBestProductInCacheService.execute();
            bestProducts = bestProductCacheManager.get();
        }

        List<Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new Output.ProductInfo(product.getId(), product.getName(), product.getPrice()))
                .toList();
        return new Output(productInfos);
    }
}