-- ============================================================
-- Petty - 宠物上门喂养服务平台 Database Schema
-- Version: V001
-- Database: MySQL 8.0+
-- Charset: utf8mb4
-- ============================================================

-- -----------------------------------------------------------
-- 1. 宠物主人表 (owner)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `owner` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主人ID',
    `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `gender` TINYINT DEFAULT NULL COMMENT '性别: 0-未知 1-男 2-女',
    `address` VARCHAR(500) DEFAULT '' COMMENT '默认服务地址',
    `address_detail` VARCHAR(200) DEFAULT '' COMMENT '门牌号/楼层',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `lock_info` VARCHAR(500) DEFAULT NULL COMMENT '门锁信息（加密存储）',
    `emergency_contact` VARCHAR(100) DEFAULT NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
    `member_level` ENUM('NORMAL', 'VIP', 'SVIP') NOT NULL DEFAULT 'NORMAL' COMMENT '会员等级',
    `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `total_orders` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计下单数',
    `total_spent` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
    `openid` VARCHAR(128) DEFAULT NULL COMMENT '微信OpenID',
    `unionid` VARCHAR(128) DEFAULT NULL COMMENT '微信UnionID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物主人表';

-- -----------------------------------------------------------
-- 2. 宠物档案表 (pet)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pet` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '主人ID',
    `name` VARCHAR(100) NOT NULL COMMENT '宠物名字',
    `species` ENUM('CAT', 'DOG', 'BIRD', 'FISH', 'REPTILE', 'SMALL_ANIMAL', 'OTHER') NOT NULL COMMENT '物种',
    `breed` VARCHAR(100) DEFAULT NULL COMMENT '品种',
    `gender` ENUM('MALE', 'FEMALE', 'NEUTERED_MALE', 'SPAYED_FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `weight` DECIMAL(5, 2) DEFAULT NULL COMMENT '体重(kg)',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '宠物照片',
    `personality` VARCHAR(500) DEFAULT NULL COMMENT '性格描述（如：胆小、亲人、攻击性等）',
    `diet_info` TEXT DEFAULT NULL COMMENT '饮食信息（喂食量、品牌、禁忌食物）',
    `health_notes` TEXT DEFAULT NULL COMMENT '健康备注（过敏、用药、慢性病）',
    `vaccine_info` VARCHAR(500) DEFAULT NULL COMMENT '疫苗接种情况',
    `last_vaccine_date` DATE DEFAULT NULL COMMENT '最近疫苗日期',
    `special_instructions` TEXT DEFAULT NULL COMMENT '特殊注意事项',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_species` (`species`),
    CONSTRAINT `fk_pet_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物档案表';

-- -----------------------------------------------------------
-- 3. 喂养师表 (sitter)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sitter` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '喂养师ID',
    `name` VARCHAR(100) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号（脱敏存储）',
    `gender` TINYINT DEFAULT NULL COMMENT '性别: 1-男 2-女',
    `bio` TEXT DEFAULT NULL COMMENT '个人简介',
    `experience_years` INT UNSIGNED DEFAULT 0 COMMENT '从业年限',
    `certifications` JSON DEFAULT NULL COMMENT '资质证书列表 [{name, imageUrl, verifiedAt}]',
    `service_area` VARCHAR(500) DEFAULT NULL COMMENT '服务区域描述',
    `service_radius_km` DECIMAL(5, 2) DEFAULT 5.00 COMMENT '服务半径(km)',
    `home_latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '常驻纬度',
    `home_longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '常驻经度',
    `accepted_species` JSON DEFAULT NULL COMMENT '可接受的宠物种类 ["CAT","DOG"]',
    `max_daily_orders` INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '每日最大接单量',
    `base_price` DECIMAL(8, 2) NOT NULL DEFAULT 0.00 COMMENT '基础服务单价',
    `rating` DECIMAL(3, 2) NOT NULL DEFAULT 5.00 COMMENT '综合评分',
    `total_orders` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计服务次数',
    `total_reviews` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计评价数',
    `completion_rate` DECIMAL(5, 2) NOT NULL DEFAULT 100.00 COMMENT '完成率(%)',
    `response_time_min` INT UNSIGNED DEFAULT NULL COMMENT '平均响应时间(分钟)',
    `background_check_status` ENUM('PENDING', 'PASSED', 'FAILED', 'EXPIRED') DEFAULT 'PENDING' COMMENT '背景调查状态',
    `background_check_date` DATE DEFAULT NULL COMMENT '背景调查日期',
    `insurance_status` ENUM('NONE', 'ACTIVE', 'EXPIRED') DEFAULT 'NONE' COMMENT '保险状态',
    `status` ENUM('PENDING_REVIEW', 'ACTIVE', 'SUSPENDED', 'OFFLINE', 'BANNED') NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '账户状态',
    `openid` VARCHAR(128) DEFAULT NULL COMMENT '微信OpenID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_rating` (`rating`),
    KEY `idx_location` (`home_latitude`, `home_longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养师表';

-- -----------------------------------------------------------
-- 4. 喂养师排班表 (sitter_schedule)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sitter_schedule` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '排班ID',
    `sitter_id` BIGINT UNSIGNED NOT NULL COMMENT '喂养师ID',
    `date` DATE NOT NULL COMMENT '日期',
    `time_slot_start` TIME NOT NULL COMMENT '可用开始时间',
    `time_slot_end` TIME NOT NULL COMMENT '可用结束时间',
    `max_orders` INT UNSIGNED NOT NULL DEFAULT 3 COMMENT '该时段最大接单数',
    `booked_orders` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已预订数',
    `status` ENUM('AVAILABLE', 'FULL', 'BLOCKED') NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sitter_date_slot` (`sitter_id`, `date`, `time_slot_start`),
    KEY `idx_date_status` (`date`, `status`),
    CONSTRAINT `fk_schedule_sitter` FOREIGN KEY (`sitter_id`) REFERENCES `sitter`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养师排班表';

-- -----------------------------------------------------------
-- 5. 服务类型表 (service_type)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `service_type` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '服务类型ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `code` VARCHAR(50) NOT NULL COMMENT '服务编码',
    `description` TEXT DEFAULT NULL COMMENT '服务描述',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标',
    `base_duration_min` INT UNSIGNED NOT NULL DEFAULT 30 COMMENT '基础服务时长(分钟)',
    `base_price` DECIMAL(8, 2) NOT NULL COMMENT '基础定价',
    `extra_pet_price` DECIMAL(8, 2) DEFAULT 0.00 COMMENT '多宠加价(每只)',
    `applicable_species` JSON DEFAULT NULL COMMENT '适用物种 ["CAT","DOG"]',
    `checklist_template` JSON DEFAULT NULL COMMENT '服务检查清单模板',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-停用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务类型表';

-- -----------------------------------------------------------
-- 6. 服务订单表 (service_order)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `service_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '宠物主人ID',
    `sitter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '喂养师ID',
    `service_type_id` BIGINT UNSIGNED NOT NULL COMMENT '服务类型ID',
    `service_address` VARCHAR(500) NOT NULL COMMENT '服务地址',
    `service_latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '服务地点纬度',
    `service_longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '服务地点经度',
    `scheduled_date` DATE NOT NULL COMMENT '预约日期',
    `scheduled_start_time` TIME NOT NULL COMMENT '预约开始时间',
    `scheduled_end_time` TIME NOT NULL COMMENT '预约结束时间',
    `actual_start_time` DATETIME DEFAULT NULL COMMENT '实际开始时间',
    `actual_end_time` DATETIME DEFAULT NULL COMMENT '实际结束时间',
    `pet_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '服务宠物数量',
    `service_amount` DECIMAL(10, 2) NOT NULL COMMENT '服务费',
    `extra_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '附加费用',
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠减免',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '实付总金额',
    `platform_commission` DECIMAL(10, 2) DEFAULT NULL COMMENT '平台佣金',
    `sitter_income` DECIMAL(10, 2) DEFAULT NULL COMMENT '喂养师收入',
    `status` ENUM('PENDING_MATCH', 'PENDING_ACCEPT', 'ACCEPTED', 'SITTER_EN_ROUTE', 'IN_SERVICE', 'SERVICE_COMPLETED', 'OWNER_CONFIRMED', 'CANCELLED', 'DISPUTED', 'REFUNDED') NOT NULL DEFAULT 'PENDING_MATCH' COMMENT '订单状态',
    `payment_status` ENUM('UNPAID', 'AUTHORIZED', 'PAID', 'REFUND_PENDING', 'REFUNDED') NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
    `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    `cancel_by` ENUM('OWNER', 'SITTER', 'SYSTEM') DEFAULT NULL COMMENT '取消方',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '主人备注',
    `lock_password` VARCHAR(100) DEFAULT NULL COMMENT '一次性门锁密码（加密）',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_sitter_id` (`sitter_id`),
    KEY `idx_status` (`status`),
    KEY `idx_scheduled_date` (`scheduled_date`),
    CONSTRAINT `fk_order_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner`(`id`),
    CONSTRAINT `fk_order_sitter` FOREIGN KEY (`sitter_id`) REFERENCES `sitter`(`id`),
    CONSTRAINT `fk_order_service_type` FOREIGN KEY (`service_type_id`) REFERENCES `service_type`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单表';

-- -----------------------------------------------------------
-- 7. 订单宠物关联表 (order_pet)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_pet` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `pet_id` BIGINT UNSIGNED NOT NULL COMMENT '宠物ID',
    `special_notes` VARCHAR(500) DEFAULT NULL COMMENT '本次特殊要求',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_pet_id` (`pet_id`),
    CONSTRAINT `fk_op_order` FOREIGN KEY (`order_id`) REFERENCES `service_order`(`id`),
    CONSTRAINT `fk_op_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单宠物关联表';

-- -----------------------------------------------------------
-- 8. 服务记录表 (service_log)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `service_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `sitter_id` BIGINT UNSIGNED NOT NULL COMMENT '喂养师ID',
    `log_type` ENUM('CHECK_IN', 'FEEDING', 'PLAYING', 'CLEANING', 'WALKING', 'MEDICATION', 'ABNORMAL', 'CHECK_OUT', 'OTHER') NOT NULL COMMENT '记录类型',
    `description` TEXT DEFAULT NULL COMMENT '文字描述',
    `photo_urls` JSON DEFAULT NULL COMMENT '照片URL列表',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频URL',
    `gps_latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT 'GPS纬度',
    `gps_longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT 'GPS经度',
    `pet_status` VARCHAR(200) DEFAULT NULL COMMENT '宠物状态描述（精神/食欲/排泄）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_sitter_id` (`sitter_id`),
    KEY `idx_log_type` (`log_type`),
    CONSTRAINT `fk_log_order` FOREIGN KEY (`order_id`) REFERENCES `service_order`(`id`),
    CONSTRAINT `fk_log_sitter` FOREIGN KEY (`sitter_id`) REFERENCES `sitter`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务记录表';

-- -----------------------------------------------------------
-- 9. 评价表 (review)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `reviewer_type` ENUM('OWNER', 'SITTER') NOT NULL COMMENT '评价方',
    `reviewer_id` BIGINT UNSIGNED NOT NULL COMMENT '评价人ID',
    `target_id` BIGINT UNSIGNED NOT NULL COMMENT '被评价人ID',
    `rating` DECIMAL(2, 1) NOT NULL COMMENT '评分 1.0-5.0',
    `content` TEXT DEFAULT NULL COMMENT '评价内容',
    `photo_urls` JSON DEFAULT NULL COMMENT '评价图片',
    `tags` JSON DEFAULT NULL COMMENT '评价标签 ["准时","细心","有爱"]',
    `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名',
    `reply_content` TEXT DEFAULT NULL COMMENT '回复内容',
    `reply_at` DATETIME DEFAULT NULL COMMENT '回复时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-显示 0-隐藏',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_reviewer` (`reviewer_type`, `reviewer_id`),
    CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `service_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- -----------------------------------------------------------
-- 10. 支付记录表 (payment)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '主人ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` ENUM('WECHAT', 'ALIPAY', 'BALANCE', 'COUPON') NOT NULL COMMENT '支付方式',
    `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '三方交易号',
    `status` ENUM('AUTHORIZED', 'CAPTURED', 'RELEASED', 'REFUND_PENDING', 'REFUNDED', 'FAILED') NOT NULL DEFAULT 'AUTHORIZED' COMMENT '支付状态',
    `authorized_at` DATETIME DEFAULT NULL COMMENT '授权时间',
    `captured_at` DATETIME DEFAULT NULL COMMENT '扣款时间',
    `released_at` DATETIME DEFAULT NULL COMMENT '释放给喂养师时间',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_pay_order` FOREIGN KEY (`order_id`) REFERENCES `service_order`(`id`),
    CONSTRAINT `fk_pay_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- -----------------------------------------------------------
-- 11. 喂养师提现表 (sitter_withdrawal)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sitter_withdrawal` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '提现ID',
    `sitter_id` BIGINT UNSIGNED NOT NULL COMMENT '喂养师ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '提现金额',
    `status` ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `bank_info` VARCHAR(200) DEFAULT NULL COMMENT '提现账户信息（脱敏）',
    `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '打款流水号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '到账时间',
    PRIMARY KEY (`id`),
    KEY `idx_sitter_id` (`sitter_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_withdraw_sitter` FOREIGN KEY (`sitter_id`) REFERENCES `sitter`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养师提现表';

-- -----------------------------------------------------------
-- 12. 系统通知表 (notification)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_type` ENUM('OWNER', 'SITTER') NOT NULL COMMENT '接收方类型',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收方ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `type` ENUM('ORDER', 'PAYMENT', 'REVIEW', 'SYSTEM', 'PROMOTION') NOT NULL COMMENT '通知类型',
    `reference_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联业务ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_type`, `user_id`, `is_read`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- -----------------------------------------------------------
-- 13. 系统配置表 (sys_config)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- -----------------------------------------------------------
-- 初始服务类型数据
-- -----------------------------------------------------------
INSERT INTO `service_type` (`name`, `code`, `description`, `base_duration_min`, `base_price`, `extra_pet_price`, `applicable_species`, `checklist_template`, `sort_order`) VALUES
('上门喂养', 'FEEDING', '上门喂食、换水、铲屎、基础陪伴', 30, 49.00, 20.00, '["CAT","DOG","BIRD","FISH","REPTILE","SMALL_ANIMAL"]', '["检查宠物状态","喂食换水","清理猫砂/排泄物","简单陪伴","拍照记录"]', 1),
('遛狗服务', 'DOG_WALKING', '专业遛狗，GPS轨迹实时可查', 45, 59.00, 25.00, '["DOG"]', '["佩戴牵引绳","检查狗狗状态","户外遛行","清理排泄物","安全送回"]', 2),
('陪玩互动', 'PLAY_SESSION', '深度陪伴、互动玩耍、情绪安抚', 60, 79.00, 30.00, '["CAT","DOG","SMALL_ANIMAL"]', '["检查宠物状态","互动玩耍","情绪观察","拍照录像","记录行为"]', 3),
('基础洗护', 'GROOMING', '基础清洁、梳毛、指甲修剪', 45, 89.00, 35.00, '["CAT","DOG"]', '["检查皮肤状态","梳理毛发","清洁耳朵","修剪指甲","拍照对比"]', 4),
('喂药护理', 'MEDICATION', '按医嘱喂药、伤口护理、术后看护', 30, 69.00, 25.00, '["CAT","DOG","BIRD","REPTILE","SMALL_ANIMAL"]', '["确认用药清单","按时喂药","观察反应","记录症状","异常及时上报"]', 5),
('长期寄养', 'BOARDING', '在喂养师家中寄养照顾(按天)', 1440, 129.00, 50.00, '["CAT","DOG","SMALL_ANIMAL"]', '["接收宠物","安排生活区域","按时喂食","日常陪伴","每日报告"]', 6);
