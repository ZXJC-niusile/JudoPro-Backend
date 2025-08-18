-- JudoPro 数据库表结构
-- 创建时间: 2025-01-20
-- 说明: 柔道专业系统数据库表结构定义
-- 重要说明: 除users表外，其他表结构仅为设计文档，实际系统中运动员数据存储在Lucene索引中

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱地址，唯一',
    `password` VARCHAR(255) COMMENT '密码（加密存储）',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `enabled` BOOLEAN DEFAULT TRUE COMMENT '账户是否启用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 用户文件表
CREATE TABLE IF NOT EXISTS `user_files` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID，主键自增',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_filename` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `file_type` ENUM('IMAGE', 'VIDEO', 'DOCUMENT', 'OTHER') NOT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `mime_type` VARCHAR(100) COMMENT 'MIME类型',
    `description` TEXT COMMENT '文件描述',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `last_access_time` TIMESTAMP COMMENT '最后访问时间',
    `is_deleted` BOOLEAN DEFAULT FALSE COMMENT '是否已删除（软删除）',
    `deleted_time` TIMESTAMP COMMENT '删除时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_file_type` (`file_type`),
    INDEX `idx_upload_time` (`upload_time`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_file_url` (`file_url`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户文件信息表';

-- 运动员表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `players` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '运动员ID，主键自增',
    `name` VARCHAR(100) NOT NULL COMMENT '运动员姓名',
    `age` INT COMMENT '年龄',
    `age_group` ENUM('CADET', 'JUNIOR', 'SENIOR', 'VETERAN') COMMENT '年龄组别',
    `weight` DECIMAL(5,2) COMMENT '体重（公斤）',
    `weight_class` VARCHAR(50) COMMENT '体重级别',
    `country` VARCHAR(100) COMMENT '国家',
    `continent` ENUM('ASIA', 'EUROPE', 'AFRICA', 'NORTH_AMERICA', 'SOUTH_AMERICA', 'OCEANIA') COMMENT '大洲',
    `image` VARCHAR(500) COMMENT '头像图片URL',
    `location` VARCHAR(200) COMMENT '位置信息',
    `location_icon` VARCHAR(500) COMMENT '位置图标URL',
    `photos` TEXT COMMENT '相关照片JSON数据',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_name` (`name`),
    INDEX `idx_age_group` (`age_group`),
    INDEX `idx_weight_class` (`weight_class`),
    INDEX `idx_country` (`country`),
    INDEX `idx_continent` (`continent`),
    INDEX `idx_age` (`age`),
    INDEX `idx_weight` (`weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运动员信息表';

-- 照片表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `photos` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片ID，主键自增',
    `title` VARCHAR(200) COMMENT '照片标题',
    `url` VARCHAR(500) NOT NULL COMMENT '照片URL',
    `thumbnail_url` VARCHAR(500) COMMENT '缩略图URL',
    `description` TEXT COMMENT '照片描述',
    `category` ENUM('SPOTLIGHT', 'EVENT', 'TRAINING', 'COMPETITION') DEFAULT 'EVENT' COMMENT '照片分类：聚光灯、赛事、训练、比赛',
    `player_id` BIGINT COMMENT '关联运动员ID',
    `event_name` VARCHAR(200) COMMENT '赛事名称',
    `event_date` DATE COMMENT '赛事日期',
    `location` VARCHAR(200) COMMENT '拍摄地点',
    `photographer` VARCHAR(100) COMMENT '摄影师',
    `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞次数',
    `is_public` BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category`),
    INDEX `idx_player_id` (`player_id`),
    INDEX `idx_event_name` (`event_name`),
    INDEX `idx_event_date` (`event_date`),
    INDEX `idx_is_public` (`is_public`),
    FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片信息表';

-- 赛事表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `events` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '赛事ID，主键自增',
    `name` VARCHAR(200) NOT NULL COMMENT '赛事名称',
    `description` TEXT COMMENT '赛事描述',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `location` VARCHAR(200) COMMENT '举办地点',
    `country` VARCHAR(100) COMMENT '举办国家',
    `continent` ENUM('ASIA', 'EUROPE', 'AFRICA', 'NORTH_AMERICA', 'SOUTH_AMERICA', 'OCEANIA') COMMENT '举办大洲',
    `event_type` ENUM('WORLD_CHAMPIONSHIP', 'CONTINENTAL_CHAMPIONSHIP', 'NATIONAL_CHAMPIONSHIP', 'INTERNATIONAL_TOURNAMENT', 'LOCAL_TOURNAMENT') COMMENT '赛事类型',
    `age_groups` VARCHAR(200) COMMENT '参赛年龄组别（逗号分隔）',
    `weight_classes` VARCHAR(500) COMMENT '参赛体重级别（逗号分隔）',
    `max_participants` INT COMMENT '最大参赛人数',
    `registration_deadline` DATE COMMENT '报名截止日期',
    `entry_fee` DECIMAL(10,2) COMMENT '报名费用',
    `prize_money` DECIMAL(15,2) COMMENT '奖金总额',
    `status` ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'UPCOMING' COMMENT '赛事状态',
    `organizer` VARCHAR(200) COMMENT '主办方',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `contact_phone` VARCHAR(50) COMMENT '联系电话',
    `website` VARCHAR(500) COMMENT '官方网站',
    `poster_url` VARCHAR(500) COMMENT '海报图片URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_name` (`name`),
    INDEX `idx_start_date` (`start_date`),
    INDEX `idx_end_date` (`end_date`),
    INDEX `idx_location` (`location`),
    INDEX `idx_country` (`country`),
    INDEX `idx_continent` (`continent`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛事信息表';

-- 参赛记录表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `participations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '参赛记录ID，主键自增',
    `player_id` BIGINT NOT NULL COMMENT '运动员ID',
    `event_id` BIGINT NOT NULL COMMENT '赛事ID',
    `weight_class` VARCHAR(50) COMMENT '参赛体重级别',
    `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `result` ENUM('GOLD', 'SILVER', 'BRONZE', 'PARTICIPATED', 'DISQUALIFIED', 'WITHDREW') COMMENT '比赛结果',
    `final_rank` INT COMMENT '最终排名',
    `points_earned` INT DEFAULT 0 COMMENT '获得积分',
    `notes` TEXT COMMENT '备注信息',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_player_id` (`player_id`),
    INDEX `idx_event_id` (`event_id`),
    INDEX `idx_weight_class` (`weight_class`),
    INDEX `idx_result` (`result`),
    INDEX `idx_final_rank` (`final_rank`),
    UNIQUE KEY `uk_player_event` (`player_id`, `event_id`),
    FOREIGN KEY (`player_id`) REFERENCES `players`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参赛记录表';

-- 用户收藏表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `user_favorites` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏记录ID，主键自增',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` ENUM('PLAYER', 'EVENT', 'PHOTO') NOT NULL COMMENT '收藏目标类型',
    `target_id` BIGINT NOT NULL COMMENT '收藏目标ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_target_type` (`target_type`),
    INDEX `idx_target_id` (`target_id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 系统配置表 (仅设计文档，实际未在代码中实现)
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID，主键自增',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(500) COMMENT '配置描述',
    `config_type` ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '配置类型',
    `is_editable` BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `config_type`) VALUES
('site_name', 'JudoPro', '网站名称', 'STRING'),
('site_description', '专业柔道信息管理系统', '网站描述', 'STRING'),
('max_upload_size', '10485760', '最大上传文件大小（字节）', 'NUMBER'),
('enable_registration', 'true', '是否允许用户注册', 'BOOLEAN'),
('default_page_size', '10', '默认分页大小', 'NUMBER'),
('max_page_size', '100', '最大分页大小', 'NUMBER')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);


-- 删除QQ和微信相关字段的索引
ALTER TABLE `users` DROP INDEX `idx_qq_open_id`;
ALTER TABLE `users` DROP INDEX `idx_wechat_open_id`;

-- 删除QQ和微信相关字段
ALTER TABLE `users` DROP COLUMN `qq_open_id`;
ALTER TABLE `users` DROP COLUMN `wechat_open_id`;