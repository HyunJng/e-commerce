package kr.hhplus.be.server.product.application.port;

public interface BestProductRankingAggregateWriter {

    void incrementBestProductRanking(Long productId, Integer quantity);

}
