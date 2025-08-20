package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Deprecated
@Component
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductJpaRepository productJpaRepository;
    private final DateHolder dateHolder;

    public List<BestProduct> findBestProducts() {
        LocalDate searchEndDay = dateHolder.today();
        LocalDate searchStartDay = searchEndDay.minusDays(3);

        Pageable pageable = Pageable.ofSize(5);
        return productJpaRepository.findBestProductsBetweenDays(searchStartDay, searchEndDay, pageable).stream()
                .sorted(Comparator.comparing(BestProduct::orderCount).reversed())
                .toList();
    }
}
