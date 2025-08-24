package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.application.port.BestProductRankingCacheWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@Deprecated
@RequiredArgsConstructor
public class SaveBestProductsInCacheScheduler {

    private static final long SCHEDULED_RATE = 7L * 60 * 1000; // 7분

    private final BestProductRankingCacheWriter bestProductRankingCacheWriter;

    @Scheduled(fixedRate = SCHEDULED_RATE)
    public void execute() {
        bestProductRankingCacheWriter.update();
    }
}
