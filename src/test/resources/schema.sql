DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS wallets;

CREATE TABLE wallets
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    balance   BIGINT    DEFAULT 0,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS products;

CREATE TABLE products
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    price          BIGINT       NOT NULL,
    stock_quantity INTEGER      NOT NULL,
    create_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS coupons;

CREATE TABLE coupons
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL UNIQUE,
    discount_amount BIGINT       NOT NULL,
    discount_type   VARCHAR(50)  NOT NULL,
    dates           INTEGER      NOT NULL,
    total_quantity  INTEGER      NOT NULL,
    create_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    order_id   BIGINT    DEFAULT NULL,
    create_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (user_id, coupon_id)
);

DROP TABLE IF EXISTS orders;

CREATE TABLE orders
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT NOT NULL,
    total_amount     BIGINT NOT NULL,
    discount_amount  BIGINT NOT NULL,
    paid_amount      BIGINT NOT NULL,
    issued_coupon_id BIGINT    DEFAULT NULL,
    create_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS order_items;

CREATE TABLE order_items
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT  NOT NULL,
    product_id  BIGINT  NOT NULL,
    unit_price  BIGINT  NOT NULL,
    quantity    INTEGER NOT NULL,
    total_price BIGINT  NOT NULL,
    reg_date    DATE    NOT NULL,
    create_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
