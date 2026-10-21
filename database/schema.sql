CREATE DATABASE IF NOT EXISTS online_auction
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_auction;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  category VARCHAR(30) NOT NULL DEFAULT 'OTHER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_items_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS auctions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_id BIGINT NOT NULL UNIQUE,
  seller_id BIGINT NOT NULL,
  starting_price DECIMAL(15,2) NOT NULL,
  current_price DECIMAL(15,2) NOT NULL,
  min_increment DECIMAL(15,2) NOT NULL DEFAULT 1.00,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL,
  leading_bidder_id BIGINT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_auctions_item FOREIGN KEY (item_id) REFERENCES items(id),
  CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES users(id),
  CONSTRAINT fk_auctions_leader FOREIGN KEY (leading_bidder_id) REFERENCES users(id),
  CONSTRAINT chk_starting_price CHECK (starting_price > 0),
  CONSTRAINT chk_current_price CHECK (current_price >= starting_price),
  CONSTRAINT chk_min_increment CHECK (min_increment > 0),
  CONSTRAINT chk_auction_times CHECK (end_time > start_time),
  INDEX idx_auctions_status_time (status, start_time, end_time)
);

CREATE TABLE IF NOT EXISTS bid_transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  auction_id BIGINT NOT NULL,
  bidder_id BIGINT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id),
  CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id) REFERENCES users(id),
  INDEX idx_bids_auction_created (auction_id, created_at),
  INDEX idx_bids_amount (auction_id, amount)
);

-- Dữ liệu demo: mật khẩu hash phải được tạo bởi ứng dụng, không chèn mật khẩu thô.
