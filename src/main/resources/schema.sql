-- ============================================================
--  E-Commerce API — Database Schema & Seed Data
--  Compatible with MySQL 8.0+
-- ============================================================

-- Create and select database
CREATE DATABASE IF NOT EXISTS ecommerce_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

-- ============================================================
--  ROLES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(30)  NOT NULL UNIQUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    first_name   VARCHAR(50)  NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15),
    address      VARCHAR(255),
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  USER_ROLES JOIN TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  CATEGORIES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url   VARCHAR(500),
    created_at  DATETIME(6)  DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  PRODUCTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(200)   NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    image_url      VARCHAR(500),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id    BIGINT         NOT NULL,
    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  CARTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS carts (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL UNIQUE,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  CART_ITEMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_product (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts    (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  ORDERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    user_id          BIGINT         NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(500)   NOT NULL,
    payment_method   VARCHAR(50)             DEFAULT 'CASH_ON_DELIVERY',
    notes            VARCHAR(500),
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  ORDER_ITEMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    order_id           BIGINT         NOT NULL,
    product_id         BIGINT         NOT NULL,
    product_name       VARCHAR(200)   NOT NULL,
    price_at_purchase  DECIMAL(10, 2) NOT NULL,
    quantity           INT            NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders   (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
--  SEED DATA
-- ============================================================

-- Roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_CUSTOMER');

-- Categories
INSERT IGNORE INTO categories (name, description) VALUES
    ('Electronics',  'Phones, laptops, gadgets and accessories'),
    ('Clothing',     'Men and women fashion, sportswear'),
    ('Books',        'Fiction, non-fiction, textbooks'),
    ('Home & Kitchen','Furniture, cookware, decor'),
    ('Sports',       'Equipment and sportswear');

-- Admin user  (password = "Admin@1234" BCrypt-hashed)
INSERT IGNORE INTO users (first_name, last_name, username, email, password, phone_number, address, enabled)
VALUES ('Admin', 'User', 'admin', 'admin@ecommerce.com',
        '$2a$10$Dl1FNjGqW5fxbW3fZiGnveRdSJiF4AaJE9mBCZk2/4kpfCMgJ6NiW',
        '9999999999', '123 Admin Street, Delhi', TRUE);

-- Customer user  (password = "Customer@1234" BCrypt-hashed)
INSERT IGNORE INTO users (first_name, last_name, username, email, password, phone_number, address, enabled)
VALUES ('John', 'Doe', 'johndoe', 'john@example.com',
        '$2a$10$rXlnFSbh2V1Zp8Z5tlQJH.yN8DjhFiNvivHDqV9bLMt7XNxHmNfki',
        '9876543210', '456 Customer Lane, Mumbai', TRUE);

-- Assign roles
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'johndoe' AND r.name = 'ROLE_CUSTOMER';

-- Sample products
INSERT IGNORE INTO products (name, description, price, stock_quantity, image_url, category_id)
SELECT 'iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB Space Black', 129999.00, 50,
       'https://example.com/iphone15pro.jpg', c.id
FROM categories c WHERE c.name = 'Electronics';

INSERT IGNORE INTO products (name, description, price, stock_quantity, image_url, category_id)
SELECT 'Samsung Galaxy S24', 'Samsung Galaxy S24 128GB Phantom Black', 79999.00, 75,
       'https://example.com/galaxys24.jpg', c.id
FROM categories c WHERE c.name = 'Electronics';

INSERT IGNORE INTO products (name, description, price, stock_quantity, image_url, category_id)
SELECT 'Nike Air Max 270', 'Nike Air Max 270 Running Shoes Size 10', 8999.00, 100,
       'https://example.com/nikeairmax.jpg', c.id
FROM categories c WHERE c.name = 'Sports';

INSERT IGNORE INTO products (name, description, price, stock_quantity, image_url, category_id)
SELECT 'Clean Code by Robert C. Martin', 'A Handbook of Agile Software Craftsmanship', 999.00, 200,
       'https://example.com/cleancode.jpg', c.id
FROM categories c WHERE c.name = 'Books';
