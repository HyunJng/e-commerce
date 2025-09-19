# 📦 ecommerce-huge-traffic

## ✨ 프로젝트 개요

이 프로젝트는 **대규모 트래픽 환경에서도 안정적으로 동작하는 이커머스 핵심 도메인(주문, 결제, 쿠폰, 랭킹 등)** 을 직접 설계하고 구현한 백엔드 시스템입니다.
단순 기능 구현을 넘어 **동시성 제어, 분산 시스템 패턴, 성능 최적화**에 집중하였으며, 실무에서 사용되는 **DDD, 이벤트 기반 아키텍처**를 적용했습니다.

---
## 요구사항
### 1️⃣ 잔액 (Wallet)

* **잔액 조회**: 사용자 식별값으로 조회, 유효하지 않은 경우 오류 반환
* **잔액 충전**: 최소 1,000원부터 가능, 충전 후 잔액 반환 및 로그 적재
* **비기능 요구사항**: 동시성 제어, 멱등성 보장, 로그 비동기 처리

### 2️⃣ 상품 (Product)

* **상품 조회**: 인증 불필요, 존재하지 않으면 오류 반환
* 상품 정보: ID, 이름, 가격, 수량 응답

### 3️⃣ 주문/결제 (Order & Payment)

* **주문 생성/결제**: 잔액·재고·쿠폰 유효성 검증 후 처리
* 주문 당 쿠폰 1개 사용 가능
* 성공 시 주문ID, 결제금액, 할인금액, 지불금액, 생성일시 응답 및 로그 적재
* **비기능 요구사항**: 원자성, 동시성 제어, 안정성, 멱등성, 정합성 보장

### 4️⃣ 쿠폰 (Coupon)

* **선착순 쿠폰 발급**: 수량 제한, 중복 발급 불가
* 유효하지 않은 사용자·쿠폰은 오류 반환
* 성공 시 발급 쿠폰 정보 응답
* **비기능 요구사항**: 고트래픽 대응, 동시성 제어, 정합성 유지

### 5️⃣ 인기상품 (Ranking)

* **최근 3일간 판매량 기준 Top 5 조회**
* 수량이 아닌 주문 횟수 기준
* **비기능 요구사항**: 다수 동시 요청에도 빠른 조회 성능

## 설계 문서
* [시퀀스 다이어그램](https://github.com/HyunJng/e-commerce/blob/main/docs/sequence-diagram.md)
* [ERD](https://github.com/HyunJng/e-commerce/blob/main/docs/erd-diagram.md)
* [아키텍처 선택](https://github.com/HyunJng/e-commerce/blob/main/docs/architecture-explian.md)
---
## ✨ 주요 성과 및 기술적 강조점

### 1. **동시성 제어 & 분산 락**
> 💡 링크를 누르면 작업내용에 대한 자세한 보고서나 블로그 글을 확인실 수 있습니다.
* 쿠폰 발급, 주문/결제, 지갑 충전 등 **원자성이 중요한 시나리오**에서

    * [MySQL의 낙관락/비관락 적용](https://github.com/HyunJng/e-commerce/blob/main/docs/db-concurrency-report.md)
    * [Redisson 기반 분산락 (AOP 커스텀 애노테이션 적용)](https://blog.naver.com/khjung1654/223971615936)
* 수천 건의 동시 요청에도 **중복 발급/이중 결제 방지**를 보장.

### 2. **이벤트 기반 아키텍처 & 확장성**

* 도메인 이벤트를 **Spring ApplicationEvent** → 이후 [Kafka 기반](https://github.com/HyunJng/e-commerce/blob/mission/step-17-18/docs/kafka-coupon-report.md)으로 확장.
* 주문 생성 → 결제 완료 → 쿠폰/포인트 차감 과정을 [MSA로 가정한 **Saga 패턴**과 **보상 트랜잭션**에 대한 설계](https://github.com/HyunJng/e-commerce/blob/misson/step-15-16/docs/msa-transition-report.md) 진행

### 3. **캐싱 & 성능 최적화**

* [**Redis SortedSet**을 활용한 "실시간 인기상품 랭킹" 구현](https://github.com/HyunJng/e-commerce/blob/main/docs/redis-ranking-report.md).

    * 슬라이딩 윈도우 방식으로 최근 72시간 데이터를 집계.
    * TTL + Aggregation 키 관리 전략으로 메모리 최적화.
* [**Spring CacheManager → RedisCacheManager**로 전환하여 조회 성능 **약 70% 개선**](https://github.com/HyunJng/e-commerce/blob/main/docs/cache-report.md)

### 4. **대규모 트래픽 대응**

* **k6 부하테스트**로 주문 API를 초당 300 RPS 이상까지 검증.
* 성능 지표(p95 Latency, Error Rate)를 기반으로 [**병목 지점 분석 및 개선**](https://github.com/HyunJng/e-commerce/blob/main/docs/order-load-test-refact.report.md).
* [DB 인덱스 최적화로 응답 속도 **40% 개선**](https://github.com/HyunJng/e-commerce/blob/main/docs/db-optimization-report.md).

### 5. **아키텍처 및 모듈화**

* [**Clean Architecture**를 기반으로 유스케이스 단위 서비스 설계](https://github.com/HyunJng/e-commerce/blob/main/docs/architecture-explian.md).
* 기술 부채를 최소화하며 **MSA 전환 가능성 고려**.

### 6. **테스트 & 품질 보증**

* Testcontainers (MySQL, Redis, Kafka)로 환경 독립적 통합 테스트 구현.
* JUnit5 + Awaitility로 **동시성 시나리오 검증** (5,000건 요청에서도 중복 없음).
* Mock + Integration Test 혼합으로 도메인 단위부터 시스템 단위까지 보장.

---

## ✨ 사용 기술 스택

* **Backend**: Java 17, Spring Boot 3.5, Spring Data JPA
* **Database**: MySQL, Redis
* **Messaging**: Kafka
* **Infra & Tools**: Docker Compose, Testcontainers, k6, Logback (MDC Trace)
* **Architecture**: DDD, Clean Architecture, Hexagonal

---

## ✨ Before & After (개인 성장 포인트)

* 단순 CRUD 수준의 API 구현 → **분산 시스템, 동시성, 대규모 트래픽 대응 설계 역량** 확보.
* JPA 기본 활용 → **락 전략, 인덱스 설계, 성능 튜닝**을 통한 실무 수준 데이터 핸들링 경험.
* Redis 단순 캐시 → **SortedSet 기반 랭킹, TTL 관리, Aggregation 로직**까지 확장.
* 동기적 이벤트 → **Kafka 기반 비동기 이벤트 아키텍처**로 확장.