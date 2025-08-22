## 인기상품 조회 디자인 설계

### 1. 개요
- 목표: 최근 **3일(72시간)** 간의 구매 데이터를 기반으로 **실시간 Top‑5** 인기상품을 제공한다.
- 핵심 아이디어: **시간(시) 단위 버킷**을 사용하여 매시 정각에 `+newHour −oldHour`로 
**증분 집계**하고, **후보군(aggregate Top‑C + current Top‑C)** 에 대해서만 최종 Top‑K를 계산한다.

---

### 2. 데이터 구조

- **시간 버킷 ZSet**
  - **Key**: `best-product:{yyyyMMddHH}`
  - **Score**: 구매량(또는 가중치)
  - **TTL**: 72시간
- **집계 ZSet**
  - **최종**: `best-product:aggregate`
  - **작업용**: `best-product:aggregate:tmp` (임시 계산 후 `RENAME`로 원자 교체)
  - **Score**: 최근 72시간 누적 구매량

---

### 3. 처리 흐름

#### (1) 쓰기(이벤트)
구매 이벤트 수신 시 현재 시각 버킷에 가중 증가
```redis
ZINCRBY best-product:{nowHour} {weight} {productId}
```
- 시간복잡도: O(log N_hour)

  (N_hour = 해당 시간 버킷 내 유니크 상품 수)

#### (2) 집계(스케줄러, 매시 정각 실행)

1. 임시키에 증분 합산/차감
```redis
ZUNIONSTORE best-product:aggregate:tmp 3 
best-product:aggregate best-product:{nowHour} best-product:{oldHour}
WEIGHTS 1 1 -1
```

2. 점수 0 이하 제거
```redis
ZREMRANGEBYSCORE best-product:aggregate:tmp -inf 0
```

3. 후보군 상한 유지(낮은 점수부터 제거)
```redis
ZCARD best-product:aggregate:tmp -> size
if size > C_past:
    ZREMRANGEBYRANK best-product:aggregate:tmp 0 (size - C_past - 1)
```

4. 원자 교체
```redis
RENAME best-product:aggregate:tmp best-product:aggregate
```

- 시간복잡도 요약
  - `ZUNIONSTORE`: O(N log N), 여기서 N ≤ C_past + S_new + S_old
    (S_new/S_old = 해당 두 시간 버킷의 유니크 상품 수)
  - `ZREMRANGEBYSCORE`: O(M) (삭제된 멤버 수)
  - `ZREMRANGEBYRANK`: O(R) (삭제된 멤버 수; size − C_past)
  - `RENAME`: O(1)

---
#### (3) 조회(Read)

1. 후보 추출(합집합 후보 U)
```redis
aggIds  = ZREVRANGE best-product:aggregate 0 (C_past - 1)
currIds = ZREVRANGE best-product:{nowHour} 0 (C_curr - 1)
candidates = distinct(aggIds ∪ currIds)
```

2. 점수 조회 파이프라인(ZMSCORE 2회)
```redis
ZMSCORE best-product:aggregate candidates  -> aggScores
ZMSCORE best-product:{nowHour} candidates  -> currScores
sum[i] = (aggScores[i] ?: 0) + (currScores[i] ?: 0)
```

3. min-heap으로 Top‑K 선별 → 내림차순 정렬 → DB 일괄 조회(findAllById(ids))

- 시간복잡도
  - 후보 추출: O(C_past + C_curr)
  - 점수 조회: O(U) (평균 멤버 단건 조회 O(1) × U)
  - heap Top‑K: O(U log K)
  - DB 조회: O(K)
  - 합계: O(U log K) (U는 고정 값)

---
### 4. 후보군 설계의 이유

1. **성능 상한 고정(읽기 경로)**
  
    조회 시 전체 집합을 재가중합하지 않고, aggregate Top‑C_past + current Top‑C_curr의 합집합 후보(U)만 대상으로 
    점수 합산과 Top‑K 선별을 수행한다. 따라서 읽기 복잡도는 O(U log K)로 상한화된다.

2. **실시간 반응성** 

    누적 강자(aggregate)와 당일 급상승(current) 을 모두 후보에 포함하여 최신 트렌드를 즉시 Top‑K에 반영한다.