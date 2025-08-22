## 선착순 쿠폰 발급 디자인 설계

### 1. 개요
- 목표: **제한 수량**의 쿠폰을 **선착순**으로 발급한다.
- 제약:
    - 중복 발급 방지
    - 동시성 환경에서도 정확한 발급 수량 유지

---

### 2. 데이터 구조

- **쿠폰 한도 (String)**
    - Key: `coupon:{couponId}:limit`
    - Value: 쿠폰 발급 총 한도(N)

- **발급 사용자 Set (Set)**
    - Key: `coupon:{couponId}:issued-users`
    - Member: userId

- 대기열 Queue (ZSet; 시간순)
    - Key: `coupon:{couponId}:issued-queue`
    - Member: userId
    - Score: 요청 시각(nowMillis)
---

### 3. 처리 흐름

#### (1) 요청 수신
1. 이미 발급한 유저인지 확인
```redis
SISMEMBER coupon:{couponId}:issued-users {userId}
```
2. 신규 유저라면 대기열 삽입  
```redis
ZADD coupon:{couponId}:issued-queue {nowMillis} {userId}
```
#### (2) Processor 실행 (분산락 보장)
1. 잔여 수량 계산  
```redis
GET coupon:{couponId}:limit  
SCARD coupon:{couponId}:issued-users
```
2. 배치 크기 결정 및 후보 추출  
```redis
ZPOPMIN coupon:{couponId}:issued-queue n
```
3. 발급 확정 및 영속화  
```mysql
INSERT INTO issued_coupon(user_id, coupon_id, ...)
```
4. 발급 사용자 기록  
```redis
SADD coupon:{couponId}:issued-users {userId}
```
### 4. 동시성 제어

- **분산락**
    - Processor 실행 시 쿠폰 단위 락을 걸어 “남은 수량 계산 → 발급 확정” 구간의 원자성을 보장한다.

- **이유**
    - 다중 서버일 시 제한쿠폰 개수를 중복하여 조회할 수 있으므로 초과 발급 가능해지기에 분산락으로 이를 제어했다.