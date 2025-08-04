INSERT INTO users(id, create_at)
VALUES(1, '2025-07-22 10:00:00');

INSERT INTO wallets(user_id, balance, create_at, update_at)
VALUES (1, 100000, '2025-07-22 10:00:00', '2025-07-22 10:00:00');


INSERT INTO products (id, name, price, stock_quantity)
VALUES (1, 'Product A', 1000, 10),
       (2, 'Product B', 2000, 20),
       (3, 'Product C', 3000, 30);

INSERT INTO coupons(id, name, discount_amount, discount_type, dates, create_at, update_at)
VALUES (1, '10% OFF', 10, 'PERCENT', 30, '2025-07-22 10:00:00', '2025-07-22 10:00:00');

INSERT INTO coupons_quantity(id, coupon_id, total_quantity, quantity, create_at, update_at)
VALUES (1, 1, 100, 100, '2025-07-22 10:00:00', '2025-07-22 10:00:00');

INSERT INTO issued_coupons(id, user_id, coupon_id, start_date, end_date, status, create_at, update_at)
VALUES (1, 1, 1, '2025-07-22', '2999-12-12', 'ACTIVE', '2025-07-22 10:00:00', '2025-07-22 10:00:00');