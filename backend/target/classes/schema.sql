-- Ocean View Resort Database Schema

CREATE DATABASE IF NOT EXISTS oceanview;
USE oceanview;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL
);

-- Initial Users
INSERT IGNORE INTO users (username, password, role) VALUES 
('admin', 'admin123', 'ADMIN'),
('staff', 'staff123', 'STAFF');

-- Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_id VARCHAR(20) UNIQUE NOT NULL,
    guest_name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20) NOT NULL,
    room_type ENUM('STANDARD', 'DELUXE', 'SUITE') NOT NULL,
    board_type ENUM('BB', 'HB', 'FB') NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_bill DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
