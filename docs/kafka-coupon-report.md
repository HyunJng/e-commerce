# 쿠폰 발급 처리: Redis → Kafka 전환 보고서

## 1. 배경

초기 구현에서는 **Redis Sorted Set(ZSET)** 과 **스케줄러**를 활용하여 쿠폰 발급 요청을 큐잉하고, 주기적으로 `popMin` 방식으로 꺼내어 처리했다. 
이 방식은 빠른 응답성을 제공했지만 다음과 같은 한계가 있었다:

* **스케줄러 기반 배치 처리**: 1초 단위로 드레인(drain)하면서 요청–처리 사이에 지연이 발생
* **단일 큐 병목**: 트래픽이 몰릴 경우 Redis 키에 집중되어 성능 병목 위험
* **확장성 한계**: 여러 인스턴스를 늘려도 스케줄러/락 관리 복잡성이 증가
---

## 2. Kafka 도입 목적

Kafka 기반으로 전환한 주된 이유는 다음과 같다:

1. **실시간 처리**

    * 큐잉 후 배치 처리 대신, Kafka Consumer가 이벤트를 수신하자마자 발급 로직 수행 → 지연 최소화

2. **수평 확장성**

    * 토픽 파티션을 기반으로 병렬 처리 확장이 용이
    * 쿠폰 ID를 파티션 키로 지정해 동일 쿠폰 요청은 순서 보장, 서로 다른 쿠폰은 병렬 처리

3. **운영 및 안정성 개선(시간 문제로 적용 X)**

    * Kafka의 내장 기능(DLT, 재시도, 모니터링)을 활용해 장애 대응 용이
    * 장애 시 메시지를 안전하게 보관하고 재처리 가능

---

## 3. 변경 내용
### 3.1. 아키텍처 변화

* **이전 (Redis 기반)**

    * API → Redis `ZSET` enqueue
    * 스케줄러(@Scheduled) → Redis에서 `popMin` → DB 저장
    * 분산락(Redis Lock)으로 중복 발급 제어

* **이후 (Kafka 기반)**

    * API → Kafka Producer 발행 (`coupon.issue.request` 토픽)
    * Kafka Consumer → 이벤트 수신 즉시 발급 처리(DB 저장 + Redis 마킹)
    * 파티션 키 = couponId (쿠폰 단위 정합성 확보)

### 3.2. 코드 레벨 변경

1. **발급 요청 UseCase**

    * `enqueue()` (Redis 삽입) → `KafkaTemplate.send()` (토픽 발행)

2. **큐 처리**

    * `IssueCouponQueueScheduler` 제거
    * Kafka Listener(`@KafkaListener`)로 이벤트 수신 시 발급 처리

자세한 사항은 87dedc5a 커밋 참조.