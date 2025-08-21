## ERD 다이어그램
이해를 위해 FK에 대한 표기를 하였으나 실제로 FK는 사용하지 않습니다.

```mermaid
erDiagram
    USERS {
        BIGINT id PK
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    WALLETS {
        BIGINT id PK
        BIGINT user_id FK "유저 식별자"
        BIGINT balance "잔액"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    USER_POINT_HISTORY {
        BIGINT id PK
        BIGINT user_id FK "유저 식별자"
        BIGINT amount "거래 금액"
        BIGINT balance "잔액"
        STRING type "CHARGE, USE"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    PRODUCTS {
        BIGINT id PK
        STRING name "상품이름"
        BIGINT price "금액"
        INT stock_quantity "수량"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    ORDERS {
        BIGINT id PK
        BIGINT user_id FK "유저식별자"
        BIGINT total_amount "주문금액"
        BIGINT discount_amount "할인 금액"
        BIGINT paid_amount "지불 금액"
        BIGINT issued_coupon_id FK "발급된 쿠폰 식별자"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    ORDER_ITEMS {
        BIGINT id PK
        BIGINT order_id FK "주문식별자"
        BIGINT product_id FK "상품식별자"
        BIGINT unit_price "상품 가격"
        INT quantity "주문수량"
        BIGINT total_price "주문금액"
        DATE reg_date "등록일"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    COUPONS {
        BIGINT id PK
        STRING name "쿠폰이름"
        BIGINT discount_amount "할인금액"
        STRING discount_type "PERCENT, AMOUNT"
        BIGINT dates "발급후 사용 가능 일자"
        STRING state "ACTIVE, INACTIVE"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    COUPONS_QUANTITY {
        BIGINT id PK
        BIGINT coupon_id FK "쿠폰식별자"
        INT total_quantity "총 수량"
        INT issued_quantity "발급 수량"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

    ISSUED_COUPONS {
        BIGINT id PK
        BIGINT user_id FK "유저식별자"
        BIGINT coupon_id FK "쿠폰식별자"
        DATETIME started_at "사용시작"
        DATETIME end_at "사용마감"
        STRING state "ACTIVE, USED"
        BIGINT order_id FK "주문식별자"
        BIGINT version "버전"
        DATETIME create_at "생성일시"
        DATETIME update_at "수정일시"
    }

%% 관계 정의
    USERS ||--o{ USER_POINT_HISTORY: "충전/사용한다"
    WALLETS ||--o{ USER_POINT_HISTORY: "충전/사용한다"
    USERS ||--|| WALLETS: "보유한다"
    USERS ||--o{ ORDERS: "주문한다"
    USERS ||--o{ ISSUED_COUPONS: "쿠폰보유"
    ORDERS ||--o{ ORDER_ITEMS: "포함한다"
    PRODUCTS ||--o{ ORDER_ITEMS: "구성된다"
    COUPONS ||--o{ ISSUED_COUPONS: "발급된다"
    COUPONS ||--|| COUPONS_QUANTITY: "보유하다"
    ISSUED_COUPONS ||--|| ORDERS: "적용된다"
```
