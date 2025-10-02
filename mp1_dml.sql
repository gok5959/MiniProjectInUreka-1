USE mp1;

-- ========== USERS ==========
INSERT INTO users (email, name, password, role)
VALUES
    ('admin@market.com', '관리자', 'admin123', 'ADMIN'),
    ('alice@example.com', 'Alice', 'alice123', 'USER'),
    ('bob@example.com', 'Bob', 'bob123', 'USER'),
    ('carol@example.com', 'Carol', 'carol123', 'USER'),
    ('dave@example.com', 'Dave', 'dave123', 'USER');

-- ========== CATEGORIES ==========
-- 대분류 + 의류 하위분류 강화
INSERT INTO categories (name, parent_id) VALUES
                                             ('전자기기', NULL),             -- 1
                                             ('가전제품', 1),                -- 2
                                             ('모바일', 1),                  -- 3
                                             ('가구', NULL),                 -- 4
                                             ('의류', NULL),                 -- 5
                                             ('상의', 5),                    -- 6
                                             ('하의', 5),                    -- 7
                                             ('신발', 5),                    -- 8
                                             ('아우터', 5),                  -- 9
                                             ('악세사리', 5);                -- 10

-- ========== PRODUCTS ==========
-- seller_id: Alice=2, Bob=3, Carol=4, Dave=5
-- 전자기기
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (2, 3, '아이폰 13 Pro', 800000, 'ON_SALE'),
                                                                       (2, 2, '삼성 세탁기', 400000, 'ON_SALE'),
                                                                       (5, 3, '갤럭시 S22', 700000, 'ON_SALE');

-- 가구
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (3, 4, '원목 책상', 150000, 'RESERVED'),
                                                                       (3, 4, '책장 세트', 100000, 'ON_SALE');

-- 의류 - 상의
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (4, 6, '나이키 반팔 티셔츠', 25000, 'ON_SALE'),
                                                                       (2, 6, '아디다스 후드티', 45000, 'ON_SALE'),
                                                                       (5, 6, '유니클로 셔츠', 20000, 'ON_SALE');

-- 의류 - 하의
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (3, 7, '리바이스 청바지', 55000, 'ON_SALE'),
                                                                       (4, 7, '나이키 조거 팬츠', 40000, 'ON_SALE');

-- 의류 - 신발
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (2, 8, '나이키 에어포스 1', 90000, 'ON_SALE'),
                                                                       (5, 8, '아디다스 울트라부스트', 110000, 'ON_SALE'),
                                                                       (4, 8, '뉴발란스 530', 85000, 'ON_SALE');

-- 의류 - 아우터
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (4, 9, '겨울 패딩', 90000, 'SOLD'),
                                                                       (3, 9, '가죽 자켓', 120000, 'ON_SALE');

-- 의류 - 악세사리
INSERT INTO products (seller_id, category_id, name, price, status) VALUES
                                                                       (2, 10, '지갑 (구찌)', 180000, 'ON_SALE'),
                                                                       (5, 10, '카시오 시계', 60000, 'ON_SALE');

-- ========== FAVORITES ==========
INSERT INTO favorites (user_id, product_id, active)
VALUES
    (3, 1, 1),   -- Bob likes 아이폰
    (4, 1, 1),   -- Carol likes 아이폰
    (2, 4, 1),   -- Alice likes 원목 책상
    (5, 13, 1),  -- Dave likes 겨울 패딩
    (2, 8, 1),   -- Alice likes 청바지
    (3, 9, 1);   -- Bob likes 조거 팬츠

-- ========== ORDERS ==========
-- 원목 책상 예약
-- 겨울 패딩은 판매완료
INSERT INTO orders (product_id, buyer_id, price_at_order, status)
VALUES
    (4, 2, 150000, 'BEFORE_REVIEW'),  -- Alice가 Bob 책상 예약
    (13, 5, 90000, 'REVIEWED');       -- Dave가 Carol 패딩 구매 완료

-- ========== REVIEWS ==========
INSERT INTO reviews (order_id, reviewer_id, title, content, rating)
VALUES
    (2, 5, '좋은 거래였습니다', '패딩 상태가 깨끗하고 만족합니다.', 4.5);

-- ========== PRODUCT METRICS ==========
INSERT INTO product_metrics (product_id, like_count, view_count)
VALUES
    (1, 2, 150),   -- 아이폰
    (2, 0, 50),    -- 세탁기
    (3, 0, 20),    -- 갤럭시
    (4, 1, 70),    -- 책상
    (5, 0, 30),    -- 책장
    (6, 0, 25),    -- 반팔
    (7, 0, 40),    -- 후드티
    (8, 0, 15),    -- 셔츠
    (9, 1, 55),    -- 청바지
    (10, 1, 45),   -- 조거 팬츠
    (11, 0, 65),   -- 에어포스
    (12, 0, 75),   -- 울트라부스트
    (13, 1, 85),   -- 뉴발란스
    (14, 1, 95),   -- 패딩
    (15, 0, 33),   -- 가죽 자켓
    (16, 0, 22),   -- 지갑
    (17, 0, 19);   -- 시계
