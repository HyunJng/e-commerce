package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.usecase.SaveBestProductInCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveBestProductsInCacheScheduler {

    private static final long SCHEDULED_RATE = 5L * 60 * 1000; // 5분

    private final SaveBestProductInCacheService saveBestProductInCacheService;

    @Scheduled(fixedRate = SCHEDULED_RATE)
    public void execute() {
        saveBestProductInCacheService.execute();
    }
}
