package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.application.port.BestProductCacheWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveBestProductsInCacheScheduler {

    private static final long SCHEDULED_RATE = 7L * 60 * 1000; // 7분

    private final BestProductCacheWriter bestProductCacheWriter;

    @Scheduled(fixedRate = SCHEDULED_RATE)
    public void execute() {
        bestProductCacheWriter.update();
    }
}
