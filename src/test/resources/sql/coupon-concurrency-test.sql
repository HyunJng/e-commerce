INSERT INTO coupons(id, name, discount_amount, discount_type, dates, create_at, update_at)
VALUES (1, '10% OFF', 10, 'PERCENT', 30, '2025-07-22 10:00:00', '2025-07-22 10:00:00');

INSERT INTO coupons_quantity(id, coupon_id, total_quantity, issued_quantity)
VALUES (1, 1, 100, 0);