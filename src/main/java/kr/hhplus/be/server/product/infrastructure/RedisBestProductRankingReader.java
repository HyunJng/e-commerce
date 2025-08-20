package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.application.port.BestProductRankingReader;
import kr.hhplus.be.server.product.domain.entity.BestProductProperties;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisBestProductRankingReader implements BestProductRankingReader {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyResolver redisKeyResolver;
    private final ProductJpaRepository productJpaRepository;
    private final DateHolder dateHolder;
    private final BestProductProperties properties;

    @Override
    public List<BestProduct> get() {

        String aggregateKey = RedisKey.BEST_PRODUCT_AGGREGATE.getKey();
        String currentKey = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());

        // 후보 추출
        Set<String> aggIds = stringRedisTemplate.opsForZSet().reverseRange(aggregateKey, 0L, properties.getAggregatePastCandidate() - 1L);
        Set<String> currIds = stringRedisTemplate.opsForZSet().reverseRange(currentKey, 0L, properties.getAggregatePastCandidate() - 1L);

        if ((aggIds == null || aggIds.isEmpty()) && (currIds == null || currIds.isEmpty())) {
            return List.of();
        }

        // 합집합 후보
        List<String> candidates = new ArrayList<>();
        if (aggIds != null) {
            candidates.addAll(aggIds);
        }
        if (currIds != null) {
            candidates.addAll(currIds);
        }
        candidates = candidates.stream().distinct().toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 파이프라인: ZMSCORE(agg, candidates), ZMSCORE(curr, candidates)
        List<String> finalCandidates = candidates;
        List<Object> pipelineResult = stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            byte[][] members = finalCandidates.stream()
                    .map(s -> s.getBytes())
                    .toArray(byte[][]::new);

            connection.zSetCommands().zMScore(aggregateKey.getBytes(), members);
            connection.zSetCommands().zMScore(currentKey.getBytes(), members);
            return null;
        });

        List<Double> aggScores = (List<Double>) pipelineResult.get(0);
        List<Double> currScores = (List<Double>) pipelineResult.get(1);

        // 4) Top N만 추출 (min-heap)
        PriorityQueue<ScoredId> heap = new PriorityQueue<>(Comparator.comparingDouble(x -> x.score));
        for (int i = 0; i < candidates.size(); i++) {
            Double a = aggScores.get(i);
            Double c = currScores.get(i);
            double scoreAgg = (a == null) ? 0.0d : a.doubleValue();
            double scoreCur = (c == null) ? 0.0d : c.doubleValue();
            double sum = scoreAgg + scoreCur;

            if (heap.size() < properties.getTopCount()) {
                heap.offer(new ScoredId(Long.valueOf(candidates.get(i)), sum));
            } else if (!heap.isEmpty() && heap.peek().score < sum) {
                heap.poll();
                heap.offer(new ScoredId(Long.valueOf(candidates.get(i)), sum));
            }
        }

        // 5) 내림차순 정렬 → DB 일괄 조회
        List<ScoredId> top = new ArrayList<>(heap);
        top.sort((x, y) -> Double.compare(y.score, x.score));
        List<Long> ids = top.stream().map(t -> t.id).toList();

        var map = productJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<BestProduct> result = new ArrayList<>();
        for (Long id : ids) {
            var p = map.get(id);
            if (p != null) result.add(new BestProduct(p.getId(), p.getName(), p.getPrice(), p.getQuantity(), null));
        }
        return result;
    }

    private static class ScoredId {
        final Long id;
        final double score;

        ScoredId(Long m, double s) {
            this.id = m;
            this.score = s;
        }
    }

}
