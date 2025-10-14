/* =====================================================================
   Project  : Minimal Marketplace (Swing + MySQL)
   Database : mp1
   Charset  : utf8mb4 / utf8mb4_0900_ai_ci
   Engine   : InnoDB
   MySQL    : 8.0+
   ===================================================================== */

-- 안전장치: 외래키 검사 잠시 비활성화(생성/재생성 시 편의)
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- (선택) 타임존/문자셋 환경 고정
-- SET time_zone = '+00:00';
-- SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 데이터베이스 생성 및 선택
CREATE DATABASE IF NOT EXISTS mp1
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE mp1;

-- 재생성 시 순서대로 DROP (자식 → 부모 역순)
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS product_metrics;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;

START TRANSACTION;

-- =========================
-- 1) 카테고리
-- =========================
CREATE TABLE IF NOT EXISTS categories
(
    category_id BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    parent_id   BIGINT       NULL,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT unq_category_siblings UNIQUE (parent_id, name),
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES categories (category_id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 2) 사용자
-- =========================
CREATE TABLE IF NOT EXISTS users
(
    user_id    BIGINT                 NOT NULL AUTO_INCREMENT,
    email      VARCHAR(255)           NOT NULL,
    name       VARCHAR(100)           NOT NULL,
    password   VARCHAR(255)           NOT NULL,
    role       ENUM ('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP              NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT unq_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 3) 상품
-- =========================
CREATE TABLE IF NOT EXISTS products
(
    product_id  BIGINT                                       NOT NULL AUTO_INCREMENT,
    seller_id   BIGINT                                       NOT NULL,
    category_id BIGINT                                       NULL,
    name        VARCHAR(200)                                 NOT NULL,
    price       INT                                          NOT NULL,
    status      ENUM ('ON_SALE','RESERVED','SOLD','DELETED') NOT NULL DEFAULT 'ON_SALE',
    created_at  TIMESTAMP                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP                                    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP                                    NULL,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id) REFERENCES users (user_id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 4) 찜(토글 방식)
-- =========================
CREATE TABLE IF NOT EXISTS favorites
(
    user_id    BIGINT     NOT NULL,
    product_id BIGINT     NOT NULL,
    active     TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP  NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_favorites PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_fav_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_fav_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 5) 주문
-- =========================
CREATE TABLE IF NOT EXISTS orders
(
    order_id       BIGINT                            NOT NULL AUTO_INCREMENT,
    product_id     BIGINT                            NOT NULL,
    buyer_id       BIGINT                            NOT NULL,
    price_at_order INT                               NOT NULL,

    -- 주문 진행 상태
    order_state    ENUM (
        'REQUESTED',           -- 구매자 예약 요청
        'ACCEPTED',            -- 판매자 승인(상품 예약 상태)
        'REJECTED',            -- 판매자 거절
        'CANCELLED_BY_BUYER',  -- 구매자 취소
        'CANCELLED_BY_SELLER', -- 판매자 취소
        'COMPLETED'            -- 거래 완료
        )                                            NOT NULL DEFAULT 'REQUESTED',

    -- 리뷰 상태
    review_status  ENUM ('BEFORE_REVIEW','REVIEWED') NOT NULL DEFAULT 'BEFORE_REVIEW',

    created_at     TIMESTAMP                         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP                         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_orders PRIMARY KEY (order_id),

    -- 중고 1회성 거래: 한 상품에 활성 주문 하나만 허용(정책에 따라 유지)
    CONSTRAINT unq_orders_product UNIQUE (product_id),

    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
            ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_orders_buyer
        FOREIGN KEY (buyer_id) REFERENCES users (user_id)
            ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 6) 상품 메트릭(카운터 분리)
-- =========================
CREATE TABLE IF NOT EXISTS product_metrics
(
    product_id BIGINT    NOT NULL,
    like_count INT       NOT NULL DEFAULT 0,
    view_count INT       NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_product_metrics PRIMARY KEY (product_id),
    CONSTRAINT fk_metrics_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =========================
-- 7) 리뷰
-- =========================
CREATE TABLE IF NOT EXISTS reviews
(
    review_id   BIGINT        NOT NULL AUTO_INCREMENT,
    order_id    BIGINT        NOT NULL,
    reviewer_id BIGINT        NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    content     TEXT          NOT NULL,
    rating      DECIMAL(2, 1) NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT unq_reviews_order_reviewer UNIQUE (order_id, reviewer_id),
    CONSTRAINT fk_reviews_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_reviews_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users (user_id),
    CONSTRAINT chk_reviews_rating
        CHECK (
            rating >= 0.5
                AND rating <= 5.0
                AND MOD(rating * 10, 5) = 0
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

COMMIT;

-- 원복: 외래키 검사 재활성화
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
