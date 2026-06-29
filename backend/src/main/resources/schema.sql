-- Petty H2 Schema (MySQL-compatible mode)

CREATE TABLE IF NOT EXISTS owner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(500),
    gender TINYINT,
    address VARCHAR(500) DEFAULT '',
    address_detail VARCHAR(200) DEFAULT '',
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    lock_info VARCHAR(500),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    member_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_orders INT NOT NULL DEFAULT 0,
    total_spent DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    openid VARCHAR(128),
    unionid VARCHAR(128),
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(30) NOT NULL,
    breed VARCHAR(100),
    gender VARCHAR(30) DEFAULT 'UNKNOWN',
    birth_date DATE,
    weight DECIMAL(5, 2),
    avatar_url VARCHAR(500),
    personality VARCHAR(500),
    diet_info TEXT,
    health_notes TEXT,
    vaccine_info VARCHAR(500),
    last_vaccine_date DATE,
    special_instructions TEXT,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sitter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(500),
    id_card VARCHAR(20),
    gender TINYINT,
    bio TEXT,
    experience_years INT DEFAULT 0,
    certifications TEXT,
    service_area VARCHAR(500),
    service_radius_km DECIMAL(5, 2) DEFAULT 5.00,
    home_latitude DECIMAL(10, 7),
    home_longitude DECIMAL(10, 7),
    accepted_species TEXT,
    max_daily_orders INT NOT NULL DEFAULT 5,
    base_price DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    rating DECIMAL(3, 2) NOT NULL DEFAULT 5.00,
    total_orders INT NOT NULL DEFAULT 0,
    total_reviews INT NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5, 2) NOT NULL DEFAULT 100.00,
    response_time_min INT,
    background_check_status VARCHAR(20) DEFAULT 'PENDING',
    background_check_date DATE,
    insurance_status VARCHAR(20) DEFAULT 'NONE',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    openid VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    base_duration_min INT NOT NULL DEFAULT 30,
    base_price DECIMAL(8, 2) NOT NULL,
    extra_pet_price DECIMAL(8, 2) DEFAULT 0.00,
    applicable_species TEXT,
    checklist_template TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    owner_id BIGINT NOT NULL,
    sitter_id BIGINT,
    service_type_id BIGINT NOT NULL,
    service_address VARCHAR(500) NOT NULL,
    service_latitude DECIMAL(10, 7),
    service_longitude DECIMAL(10, 7),
    scheduled_date DATE NOT NULL,
    scheduled_start_time TIME NOT NULL,
    scheduled_end_time TIME NOT NULL,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    pet_count INT NOT NULL DEFAULT 1,
    service_amount DECIMAL(10, 2) NOT NULL,
    extra_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    platform_commission DECIMAL(10, 2),
    sitter_income DECIMAL(10, 2),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_MATCH',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    cancel_reason VARCHAR(500),
    cancel_by VARCHAR(20),
    remark VARCHAR(500),
    lock_password VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    special_notes VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS service_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sitter_id BIGINT NOT NULL,
    log_type VARCHAR(30) NOT NULL,
    description TEXT,
    photo_urls TEXT,
    video_url VARCHAR(500),
    gps_latitude DECIMAL(10, 7),
    gps_longitude DECIMAL(10, 7),
    pet_status VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
