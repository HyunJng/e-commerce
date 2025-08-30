# 주문-결제 MSA 설계 보고서

## 1. 배경 및 문제 정의
* 주문생성 중 상품, 쿠폰, 포인트를 모두 하나의 DB를 바라보고 처리하고 있다
* 하지만 서비스가 커지고 트래픽이 늘어나 애플리케이션 서버와 DB를 도메인 별로 분리하기로 결정되어 트랜잭션을 분리해야하는 상황이라고 가정한다.
* 트랜잭션 분리가 일어나되 정합성과 재시도/멱등을 보장하도록 설계계획을 작성하였다.

---

## 2. 해결 전략

* **예약 상태 추가**: 주문 생성 시 재고·쿠폰·포인트를 즉시 차감하지 말고 **예약(대기)** 상태로 묶어두어 비동기 처리 중의 중복 사용을 제한한다.
* **Choreography Saga + 트랜잭셔널 Outbox**:

    * 트랜잭션 내에서 각 도메인 서비스에 API 형태로 사용(예약)처리 + **Outbox 이벤트**를 기록 → 커밋 후 비동기로 이벤트 전파.
    * 각 모듈이 이벤트를 구독하여 자신 역할 수행(코레오그래피).

---

## 3. 도메인 상태 추가

1. **PRODUCTS**

    * `reserved_quantity INT DEFAULT 0` 추가
    * `available_quantity = stock_quantity - reserved_quantity` (조회 시 계산)

2. **WALLETS**

    * `hold_balance BIGINT DEFAULT 0` 추가
    * `available_balance = balance - hold_balance`

3. **ISSUED_COUPONS**

    * `state` 값을 확장: `ACTIVE, RESERVED, USED`

4. **예약/홀드 상세 테이블**

    * **PRODUCTS_RESERVATIONS**(주문별 재고 예약)
    * **WALLET_HOLDS**(주문별 포인트 홀드)
    * **COUPON_RESERVATIONS**(주문별 쿠폰 홀드)

5. **비동기 보조 테이블**

    * **OUTBOX_EVENTS**(트랜잭셔널 아웃박스)

6. **ORDERS**

    * `state` 컬럼 추가: `CREATED, PENDING_PAYMENT, PAID, PAYMENT_FAILED, CANCELLED`
    * `traceId` 컬럼 추가: 중복 요청을 제어/주문 추적을 위해 UNIQUE 컬럼으로 제한

---

## 4. 트랜잭션 경계 & 단계별 흐름

### 4.1 주문 생성

1. **검증**: 
   - 상품 : 상품 서비스에 API형태로 사용 가능한 상품 수량인지 확인하고 예약상태로 바꾼다.
     - PRODUCTS.reserved_quantity += qty` (조건: `(stock - reserved) >= qty`) + `PRODUCTS_RESERVATIONS 적재`
   - 쿠폰 : 쿠폰 서비스에 API형태로 사용 가능한 쿠폰인지 확인하고 예약 상태로 바꾼다.
     - `ISSUED_COUPONS.state=RESERVED` + `COUPON_RESERVATIONS 적재`
   - 포인트: 구매 금액이 소유한 포인트보다 많은지 확인하고 예약 상태로 바꾼다.
     - `WALLETS.hold_balance += amount` (조건: `(balance - hold) >= amount`) + `WALLET_HOLDS 적재`
2. **주문 저장**: `ORDERS.state = PENDING_PAYMENT`.
3. **Outbox 기록**: `OrderEvent(traceId, amount, productIds, couponId)` 이벤트를 **OUTBOX_EVENTS**에 PENDING 으로 적재.
4. **커밋**: 커밋 이후 퍼블리셔가 비동기로 **도메인 이벤트** 전파 → 외부 PG 호출 시작.
   - Order와 결제는 동일한 도메인이라고 생각되어 분산 이벤트로 나누지 않았다.
> 실패 시 전체 롤백. 재시도 시 예약 테이블의 UNIQUE 제약으로 멱등 처리.

> 전체 롤백은 분산 이벤트 발행을 통해 각 도메인에 요청 
> → 만약 상품에서 실패 처리되었더라도 쿠폰/포인트에 롤백 이벤트가 전달되겠지만, 예약/홀드 테이블이 존재하니 문제없을 것이라 판단.

### 4.2 결제 처리(비동기)

* **OrderEvent 수신 → 외부 PG에 결제 요청**

    * 성공 
      * `OUTBOX_EVENTS.state=SUCCESS` 아웃박스 기록(INSERT)
      * 주문: `ORDERS.state=PAID`
    * 실패/타임아웃 
      * `OUTBOX_EVENTS.state=FAILED/TIMED_OUT` 기록(INSERT)
      * 주문: `ORDERS.state=PAYMENT_FAILED`

### 4.3 확정/보상(비동기)

* **외부 PG 수신 → 예약 확정 분산 이벤트**:

    * 재고: `stock -= qty`, `reserved -= qty`, `PRODUCTS_RESERVATIONS=COMMITTED`
    * 쿠폰: `ISSUED_COUPONS=USED` , `COUPON_RESERVATIONS=COMMITTED`
    * 포인트: `balance -= amount`, `hold_balance -= amount`, `WALLET_HOLDS=COMMITED`

* **실패/타임아웃 수신 → 예약 해제 분산 이벤트**:

    * 재고: `reserved -= qty`, `PRODUCTS_RESERVATIONS=RELEASED`
    * 쿠폰: `state=ACTIVE`, `COUPON_RESERVATIONS=RELEASED`
    * 포인트: `hold_balance -= amount`, `WALLET_HOLDS=RELEASED`

### 4.4 예약 만료 배치
* 네트워크나 오류 등으로 인해 예약 상태에서 더이상 처리가 되지 않는 거래가 존재할 수 있다.
* Order서비스에서 일정 시간동안 배치가 돌며 예약 상태이면서 만료시간을 넘긴 데이터가 존재하는지 확인하고, 취소 이벤트를 발행한다