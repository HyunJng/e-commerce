package kr.hhplus.be.server.comparison;

import kr.hhplus.be.server.product.application.service.ProductQueryService;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindBestProductUseCaseForNotCache {

    @Autowired
    private ProductQueryService productQueryService;

    public FindBestProductsUseCase.Output execute() {
        List<BestProduct> bestProducts = productQueryService.findBestProducts();

        List<FindBestProductsUseCase.Output.ProductInfo> productInfos = bestProducts.stream()
                .map(product -> new FindBestProductsUseCase.Output.ProductInfo(product.id(), product.name(), product.price()))
                .toList();
        return new FindBestProductsUseCase.Output(productInfos);
    }
}
