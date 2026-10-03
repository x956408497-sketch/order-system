USE order_system;

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
