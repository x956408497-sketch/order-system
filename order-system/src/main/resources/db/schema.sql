CREATE DATABASE IF NOT EXISTS order_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE order_system;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    image VARCHAR(512),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    description VARCHAR(1000),
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0下架 1上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    KEY idx_product_category_status (category_id, status),
    KEY idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO category (name, sort)
SELECT '默认分类', 0
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '默认分类');

INSERT INTO category (name, sort)
SELECT '水果蔬菜', 1
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '水果蔬菜');

INSERT INTO category (name, sort)
SELECT '日用百货', 2
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '日用百货');

INSERT INTO product (category_id, name, image, price, stock, description, status)
SELECT c.id, '红富士苹果', NULL, 12.80, 100, '新鲜红富士，适合日常订货测试。', 1
FROM category c
WHERE c.name = '水果蔬菜'
  AND NOT EXISTS (SELECT 1 FROM product WHERE name = '红富士苹果');

INSERT INTO product (category_id, name, image, price, stock, description, status)
SELECT c.id, '香蕉', NULL, 6.50, 80, '香甜软糯，上架示例商品。', 1
FROM category c
WHERE c.name = '水果蔬菜'
  AND NOT EXISTS (SELECT 1 FROM product WHERE name = '香蕉');

INSERT INTO product (category_id, name, image, price, stock, description, status)
SELECT c.id, '抽纸', NULL, 18.90, 50, '日用百货示例商品，默认上架。', 1
FROM category c
WHERE c.name = '日用百货'
  AND NOT EXISTS (SELECT 1 FROM product WHERE name = '抽纸');

INSERT INTO product (category_id, name, image, price, stock, description, status)
SELECT c.id, '下架测试商品', NULL, 9.90, 20, '用于验证用户端不展示下架商品。', 0
FROM category c
WHERE c.name = '默认分类'
  AND NOT EXISTS (SELECT 1 FROM product WHERE name = '下架测试商品');


-- Spark 销售分析结果表
CREATE TABLE IF NOT EXISTS sales_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    order_count INT NOT NULL DEFAULT 0,
    product_count INT NOT NULL DEFAULT 0,
    sales_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sales_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_sales_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    sales_count INT NOT NULL DEFAULT 0,
    sales_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_stat_date (stat_date, product_id),
    KEY idx_product_sales_count (sales_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS category_sales_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    sales_count INT NOT NULL DEFAULT 0,
    sales_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_stat_date (stat_date, category_id),
    KEY idx_category_sales_amount (sales_amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
