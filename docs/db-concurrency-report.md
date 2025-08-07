# DB레벨 동시성 처리 보고서
## 📌 선착순 쿠폰 발급 API
### 문제 상황

---
선착순 쿠폰 발급 기능은 한정된 수량의 쿠폰을 여러 사용자가 동시에 요청할 수 있는 상황을 처리해야 한다.

동시성 제어가 없을 경우, 다음과 같은 문제가 발생할 수 있다:
- 중복 발급: 쿠폰 재고가 남아있지 않음에도 불구하고, 동시에 여러 요청이 처리되면 중복으로 쿠폰이 발급될 수 있음.
- 정합성 훼손: 실제 DB에는 쿠폰 수량이 100개로 제한되어 있음에도, 101개 이상의 쿠폰이 발급되는 현상이 발생할 수 있음.
### 해결 방안

----
1. MySql의 배타락 활용
    
    쿠폰 수량을 차감하는 쿼리를 즉시 UPDATE 문으로 실행함으로써, MySQL이 제공하는 암시적 배타 락을 활용함으로써,
      ```sql
        UPDATE
            COUPONS_QUANTITY CQ1_0 
        SET
            ISSUED_QUANTITY=(CQ1_0.ISSUED_QUANTITY+1) 
        WHERE
            CQ1_0.COUPON_ID=1 
            AND CQ1_0.ISSUED_QUANTITY<CQ1_0.TOTAL_QUANTITY
      ```
   
    **기본 배타락 활용의 이점:**
    - 불필요한 락 대기, 데드락 가능성을 최소화


2. CouponQuantity 테이블 정규화

   초기에는 쿠폰 테이블(coupon)에 수량 필드가 포함되어 있었으나, 수량 업데이트와 발급 이력을 분리하기 위해 아래와 같이 정규화하였다
    - coupon_quantity: 수량만을 관리하는 테이블
    - coupon: 쿠폰 정보 (이름, 기간 등)

   정규화된 설계의 이점:
   - 수량 변경 시 다른 컬럼과 무관한 최소 단위의 변경이 가능
    - 락 경합 범위를 줄여 동시성 처리 성능 향상


3. 비동기 처리를 통한 트랜잭션 최소화

   DB 락 점유 시간을 줄이기 위해 쿠폰 발급 과정을 동기/비동기로 분리하였다
   1. ``coupon_quantity`` 차감(``update``) 
   2. 쿠폰 발급 성공 여부 응답
   3. 비동기로 ``issued_coupon`` 저장
   비동기 처리 이점:
   - 트랜잭션이 쿠폰 수량 차감에만 집중되므로 락 보유 시간이 짧아짐
   - 후속 작업은 별도 스레드로 처리되기 때문에 응답 시간 단축
   - 데드락 위험 감소

## 📌 포인트 충전 API
### 문제 상황

---
포인트 충전 API는 사용자가 동일한 요청을 반복해서 전송할 수 있는 구조로, 
네트워크 지연 상황에서 중복 요청이 발생할 수 있다.

- 중복 충전: 동일한 요청이 여러 번 처리되어 실제보다 많은 포인트가 적립됨
- 정합성 오류: DB에 저장된 포인트 잔액이 실제 비즈니스 로직과 불일치

### 해결 방안

---
1. 비관락 적용

    포인트 충전은 반드시 정확하고 순차적으로 처리되어야 하므로 비관적 락을 적용하였다. 
   - 중복 반영 방지: 같은 row에 대해 동시에 충전이 적용되는 문제 방지
   - 정확한 순서 보장: 사용자별로 충전 요청이 순차적으로 처리되어 예측 가능한 결과 유지
    ```sql
   SELECT
        W1_0.ID,
        W1_0.BALANCE,
        W1_0.CREATE_AT,
        W1_0.UPDATE_AT,
        W1_0.USER_ID 
    FROM
        WALLETS W1_0 
    WHERE
        W1_0.USER_ID=1 FOR UPDATE
   ```

2. 멱등성 보장은 Redis 기반 멱등 키로 처리 예정
   비관적 락은 동시 충돌 제어에는 유용하지만, 동일 거래 요청인지 판단하는 기능은 제공하지 않는다.
   
    이를 해결하기 위해 다음과 같은 방향으로 개선을 계획하고 있다
    - 멱등 키(idempotent key): 각 요청마다 고유한 transactionId 또는 requestId를 생성
    - Redis: 이 멱등 키를 Redis에 저장하여 중복 여부를 빠르게 판단
    - 이미 처리된 키가 있다면 요청을 무시하거나, 이전 응답을 재전송

## 📌 주문/결제 API
### 문제 상황

---
주문과 결제는 다양한 리소스(재고, 포인트, 쿠폰 등)의 상태를 변경하는 복합 트랜잭션이므로, 동시에 여러 사용자의 주문 요청이 발생할 경우, 다음과 같은 문제들이 발생할 수 있다

- 재고 부족 상태에서의 주문 성공: 여러 사용자가 동시에 주문을 시도하면 재고 초과 주문이 처리될 수 있음
- 중복 포인트 차감: 동일 포인트에 대해 여러 차감 요청이 겹칠 경우 부정확한 잔액 발생
- 쿠폰 중복 사용: 하나의 쿠폰이 두 개의 주문에 동시에 적용되는 등 쿠폰의 유일성이 보장되지 않음
- 데드락: 여러 리소스에 락을 걸 때 순서가 꼬이면 교착 상태로 인해 시스템 전체가 응답하지 않을 수 있음

### 해결 방안

---
1. 재고 차감: 비관락 + 락 순서 보장
   
   각 Product에 대해 ``@Lock(PESSIMISTIC_WRITE)``를 적용하고, 상품 ID 기준으로 정렬하여 순차적으로 락을 획득하도록 하였다.
    ```java
        public List<Product> findAllByIds(List<Long> productIds) {
            // 생략
            productIds = productIds.stream().sorted().toList();
            return productLockJpaRepository.findAllByIdsForUpdate(productIds);
        }
    ```
   ```sql
    SELECT
        P1_0.ID,
        P1_0.CREATE_AT,
        P1_0.NAME,
        P1_0.PRICE,
        P1_0.STOCK_QUANTITY,
        P1_0.UPDATE_AT 
    FROM
        PRODUCTS P1_0 
    WHERE
        P1_0.ID IN (2) FOR UPDATE
    ```
    - 락 순서 보장을 통해 데드락 방지
    - 락을 획득한 상태에서 수량 변경 → 정합성 보장

2. 포인트 차감: 비관락
   포인트는 사용자의 userId 기준으로 row-level 비관적 락을 적용하여 동시에 두 개 이상의 주문이 포인트를 동시에 차감하지 못하도록 했다.
    ```sql
    SELECT
        W1_0.ID,
        W1_0.BALANCE,
        W1_0.CREATE_AT,
        W1_0.UPDATE_AT,
        W1_0.USER_ID 
    FROM
        WALLETS W1_0 
    WHERE
        W1_0.USER_ID=2 FOR UPDATE
    ```
3. 쿠폰 상태 변경: Pessimistic Lock
   쿠폰 사용 여부를 확인하고 USED 상태로 변경할 때도 ``@Lock(PESSIMISTIC_WRITE)``을 적용하여, 동일 쿠폰이 두 주문에 중복 사용되지 않도록 하였다.
    ```sql
        SELECT
            IC1_0.ID,
            IC1_0.COUPON_ID,
            IC1_0.END_DATE,
            IC1_0.START_DATE,
            IC1_0.STATUS,
            IC1_0.USER_ID 
        FROM
            ISSUED_COUPONS IC1_0 
        WHERE
            IC1_0.USER_ID=1 
            AND IC1_0.COUPON_ID=1 FOR UPDATE
    ```
   쿠폰 발급 시에는 조건문으로 수량 감소를 수행했지만, 주문 결제 시에는 이와 같은 조건문 방식 대신 명시적으로 쿠폰 객체를 조회하여 상태를 변경하였다.
   
   그 이유는 다음과 같다.
   - 쿠폰 발급은 **정량적 자원 차감(남은 수량)**에 가까우며, 조건문으로도 동시성 안전하게 처리 가능
   - 반면 비즈니스 로직 중간 과정인 쿠폰 상태 변경이 DB에서 처리되는 것이 적절하지 않다고 여겨 JPA의 더티체킹을 사용하여 처리하였다.