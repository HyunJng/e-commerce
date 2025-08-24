package kr.hhplus.be.server.product.application.port;

import kr.hhplus.be.server.product.domain.entity.BestProduct;

import java.util.List;

public interface BestProductRankingCacheWriter {

    List<BestProduct> update();
}
