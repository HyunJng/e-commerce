INSERT INTO products (id, name, price, stock_quantity)
VALUES (1, 'Product A', 1000, 10),
       (2, 'Product B', 2000, 20),
       (3, 'Product C', 3000, 30),
       (4, 'Product D', 4000, 40),
       (5, 'Product E', 5000, 50),
       (6, 'Product F', 6000, 60),
       (7, 'Product G', 7000, 70),
       (8, 'Product H', 8000, 80),
       (9, 'Product I', 9000, 90),
       (10, 'Product J', 10000, 100),
       (11, 'Product K', 11000, 1100),
       (12, 'Product L', 12000, 1200);

INSERT INTO order_items (id, order_id, product_id, unit_price, quantity, reg_date)
VALUES (1, 101, 1, 1000, 2, '2025-07-31'),
       (2, 102, 1, 1000, 1, '2025-07-30'),
       (3, 103, 1, 1000, 3, '2025-07-29'),
       (4, 108, 1, 1000, 3, '2025-07-29');
