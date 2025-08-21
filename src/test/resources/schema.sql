DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    create_at DATETIME,
    update_at DATETIME
);

DROP TABLE IF EXISTS wallets;

CREATE TABLE wallets
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    balance   BIGINT DEFAULT 0,
    create_at DATETIME,
    update_at DATETIME
);

DROP TABLE IF EXISTS products;

CREATE TABLE products
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    price          BIGINT       NOT NULL,
    stock_quantity INTEGER      NOT NULL,
    create_at      DATETIME,
    update_at      DATETIME
);

DROP TABLE IF EXISTS coupons;

CREATE TABLE coupons
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL UNIQUE,
    discount_amount BIGINT       NOT NULL,
    discount_type   VARCHAR(50)  NOT NULL,
    dates           INTEGER      NOT NULL,
    state           VARCHAR(50)  NOT NULL,
    create_at       DATETIME,
    update_at       DATETIME
);

DROP TABLE IF EXISTS issued_coupons;

CREATE TABLE issued_coupons
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    coupon_id  BIGINT      NOT NULL,
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    status     VARCHAR(50) NOT NULL,
    order_id   BIGINT DEFAULT NULL,
    version    BIGINT NOT NULL DEFAULT 0,
    create_at  DATETIME,
    update_at  DATETIME,
    UNIQUE (user_id, coupon_id)
);

DROP TABLE IF EXISTS coupons_quantity;

CREATE TABLE coupons_quantity
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id       BIGINT  NOT NULL,
    total_quantity  INTEGER NOT NULL,
    issued_quantity INTEGER NOT NULL,
    create_at       DATETIME,
    update_at       DATETIME,
    UNIQUE (coupon_id)
);

DROP TABLE IF EXISTS orders;

CREATE TABLE orders
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT NOT NULL,
    total_amount     BIGINT NOT NULL,
    discount_amount  BIGINT NOT NULL,
    paid_amount      BIGINT NOT NULL,
    issued_coupon_id BIGINT DEFAULT NULL,
    create_at        DATETIME,
    update_at        DATETIME
);

DROP TABLE IF EXISTS order_items;

CREATE TABLE order_items
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT  NOT NULL,
    product_id BIGINT  NOT NULL,
    unit_price BIGINT  NOT NULL,
    quantity   INTEGER NOT NULL,
    reg_date   DATE    NOT NULL,
    create_at  DATETIME,
    update_at  DATETIME
);

/*
DROP INDEX IF EXISTS IDX_WALLETS_USER_ID;
DROP INDEX IF EXISTS IDX_ORDERS_USER_ID ON ORDERS;
DROP INDEX IF EXISTS IDX_ORDER_ITEMS_PRODUCT_ID_REG_DATE ON ORDER_ITEMS;

CREATE INDEX IDX_WALLETS_USER_ID ON WALLETS (USER_ID);
CREATE INDEX IDX_ORDERS_USER_ID ON ORDERS (USER_ID);
CREATE INDEX IDX_ORDER_ITEMS_PRODUCT_ID_REG_DATE ON ORDER_ITEMS (PRODUCT_ID, REG_DATE);
*/
