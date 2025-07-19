# 0. 공통 멱등성 처리
## 0.1. 중복 요청이 아닌 경우
```mermaid
sequenceDiagram
    participant Client
    participant AOP
    participant 인메모리DB
    participant Controller
    participant Service

    Client->>AOP: 요청 전송 (nonce 포함)
    AOP->>AOP: 데이터 형식 검증
    AOP->>인메모리DB: userId:endpoint:nonce 조회
        
    인메모리DB-->>AOP: 키 없음
    AOP->>인메모리DB: 상태 저장 (PROCESSING, TTL=10s)
    AOP->>Service: 실제 비즈니스 로직 실행
    Service-->>AOP: 처리 결과 반환
    AOP->>인메모리DB: 상태 업데이트 (COMPLETED, 결과 저장)
    AOP-->>Controller: 정상 응답
        
    Controller-->>Client: 최종 응답
```
## 0.2. 중복 요청인 경우
```mermaid
sequenceDiagram
    participant Client
    participant AOP
    participant 인메모리DB
    participant Controller
    participant Service

    Client->>AOP: 요청 전송 (nonce 포함)
    AOP->>AOP: 데이터 형식 검증
    AOP->>인메모리DB: userId:endpoint:nonce 조회

    인메모리DB-->>AOP: 키 있음
    alt 상태가 PROCESSING
        인메모리DB-->>AOP: PROCESSING
        AOP-->>Controller: "요청 처리 중" 응답
    else 상태가 COMPLETED
        인메모리DB-->>AOP: COMPLETED + 결과
        AOP-->>Controller: 이전 응답 재전송
    end
    Controller-->>Client: 최종 응답
```

# 1. 잔액충전 API
## 1.1 잔액 충전 성공
```mermaid
sequenceDiagram
    actor 사용자
    participant 포인트API
    participant 포인트
    participant 충전이벤트큐
    participant 충전로그

    사용자->>포인트API: 포인트 충전 요청 (금액)
        
    note right of 포인트API: 트랜잭션 시작
    activate 포인트API

    포인트API->>포인트: 충전 요청
    activate 포인트
    포인트->>포인트: 금액 증가 및 유효성 검사
    포인트-->>포인트API: 충전 완료
    deactivate 포인트
    note right of 포인트API: 트랜잭션 끝

    포인트API->>충전이벤트큐: 충전 완료 이벤트 발행
    포인트API-->>사용자: 충전 성공 응답
    deactivate 포인트API

    충전이벤트큐-->>충전로그: 충전 이벤트 수신
    activate 충전로그
    충전로그->>충전로그: 충전 로그 저장
    deactivate 충전로그
```

# 2. 상품조회 API
```mermaid
sequenceDiagram
    actor 사용자
    participant 상품API
    participant 상품

    사용자->>상품API: 상품 ID로 조회 요청
    activate 상품API

    상품API->>상품: ID로 상품 탐색 요청
    activate 상품

    상품->>상품: 조회

    상품-->>상품API: 상품 응답 반환
    상품API-->>사용자: 상품 상세 응답

    deactivate 상품
    deactivate 상품API

```

# 3. 주문/결제 API
```mermaid
sequenceDiagram
    actor 사용자
    participant 주문
    participant 쿠폰
    participant 상품
    participant 포인트
    participant 대기열
    participant 통계플랫폼

    사용자->>주문: 주문 요청

    activate 주문
    note right of 주문: 트랜잭션 시작

    주문->>쿠폰: 사용 요청
    activate 쿠폰
    쿠폰->>쿠폰: 유효성 검증 및 사용 처리
    쿠폰-->>주문: 사용 완료
    deactivate 쿠폰

    주문->>상품: 수량 확인 및 차감 요청
    activate 상품
    상품->>상품: 재고 수량 확인 및 차감
    상품-->>주문: 차감 완료
    deactivate 상품

    주문->>포인트: 포인트 차감 요청
    activate 포인트
    포인트->>포인트: 유효성 검증 및 차감 처리
    포인트-->>주문: 차감 완료
    deactivate 포인트

    note right of 주문:: 트랜잭션 끝

    주문->>대기열: 주문 통계 이벤트 발행
    주문-->>사용자: 주문 성공 응답

    deactivate 주문

    대기열-->>통계플랫폼: 주문 통계 이벤트 수신
```
## 3.1. 쿠폰 유효성 검증 실패 예외
```mermaid
sequenceDiagram
    actor 사용자
    participant 주문
    participant 쿠폰

    사용자->>주문: 주문 요청
    activate 주문

    주문->>쿠폰: 사용 요청
    쿠폰->>쿠폰: 유효성 검사

    쿠폰-->>주문: 예외 발생(쿠폰 사용자/쿠폰 사용 기간 불일치)

    주문-->>사용자: 400 Bad Request (유효성 검증 실패)
    deactivate 주문
```

## 3.2. 상품 유효성 검증 실패
```mermaid
sequenceDiagram
    actor 사용자
    participant 주문
    participant 상품

    사용자->>주문: 주문 요청 (상품ID, 수량, 쿠폰ID 등)
    activate 주문


    주문->>상품: 수량 확인 및 차감 요청
    상품->>상품: 유효성 검사 및 차감
    상품-->>주문: 예외 발생 (재고 부족)

    note right of 주문: 트랜잭션 롤백됨<br/>→ 쿠폰사용/상품차감 취소됨

    주문-->>사용자: 400 Bad Request (재고 부족)
    deactivate 주문
```

## 3.3. 포인트 잔액 부족
```mermaid
sequenceDiagram
    actor 사용자
    participant 주문
    participant 포인트

    사용자->>주문: 주문 요청
    주문->>포인트: 포인트 차감 요청
    포인트-->>포인트: 유효성 검증
    포인트-->>주문: 예외 발생 (잔액 부족)
    note right of 주문: 전체 트랜잭션 롤백<br/>→ 쿠폰사용/상품차감/포인트차감 취소됨
    주문-->>사용자: 400 Bad Request (포인트 부족)
```

# 4. 선착순 쿠폰 발급 API
```mermaid
sequenceDiagram
    actor 사용자
    participant API서버
    participant 인메모리DB
    participant 대기열
    participant 쿠폰
    participant 데이터 플랫폼

    사용자->>API서버: 쿠폰 발급 요청
    activate API서버
    
    API서버->>인메모리DB: 쿠폰 수량 차감
    인메모리DB-->>API서버: 남은 수량 반환
    
    API서버-->>사용자: 발급 성공 응답
    API서버->>대기열: 발급 이벤트 전송
    deactivate API서버
    
    activate 대기열
    대기열-->>쿠폰: 발급 이벤트 전달
    deactivate 대기열
    
    activate 쿠폰
    쿠폰->>데이터 플랫폼: 발급 이력 저장
    deactivate 쿠폰

```

# 5. 인기 판매 상품 조회 API
```mermaid
sequenceDiagram
    actor 사용자
    participant 인기상품API
    participant 인메모리DB
    participant 주문상품
    participant 인기상품스케줄러

    Note over 인기상품스케줄러: 1시간마다 실행

    activate 인기상품스케줄러
    인기상품스케줄러->>주문상품: 최근 3일간 주문 항목 조회
    주문상품-->>인기상품스케줄러: 상품별 판매 수량 집계
    인기상품스케줄러->>인메모리DB: 인기 상품 Top 5 캐시 저장
    deactivate 인기상품스케줄러

    사용자->>인기상품API: 인기 상품 조회 요청
    activate 인기상품API
    인기상품API->>인메모리DB: 인기 상품 캐시 조회
    인메모리DB-->>인기상품API: Top 5 목록 반환
    인기상품API-->>사용자: 인기 상품 응답
    deactivate 인기상품API

```