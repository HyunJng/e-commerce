package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.domain.BestProduct;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.product.usecase.port.BestProductCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveBestProductInCacheService {

    private final ProductJpaRepository productJpaRepository;
    private final BestProductCacheManager bestProductCacheManager;
    private final DateHolder dateHolder;

    public void execute() {
        LocalDate searchEndDay = dateHolder.today();
        LocalDate searchStartDay = searchEndDay.minusDays(3);

        Pageable pageable = Pageable.ofSize(5);
        List<Product> bestProducts = productJpaRepository.findBestProductsBetweenDays(searchStartDay, searchEndDay, pageable).stream()
                .sorted(Comparator.comparing(BestProduct::count).reversed())
                .map(BestProduct::product)
                .toList();
        bestProductCacheManager.save(bestProducts);
    }
}
