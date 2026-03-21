
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(255) UNIQUE COMMENT '邮箱地址',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    avatar VARCHAR(500) COMMENT '头像URL',
    real_name VARCHAR(100) COMMENT '真实姓名',
    gender VARCHAR(10) COMMENT '性别',
    birth_date VARCHAR(50) COMMENT '出生日期',
    enabled BOOLEAN DEFAULT TRUE COMMENT '账户是否启用'
);

CREATE TABLE user_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_filename VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_type VARCHAR(20) NOT NULL COMMENT '文件类型(image/video)',
    file_extension VARCHAR(10) NOT NULL COMMENT '文件扩展名',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    description VARCHAR(500) COMMENT '文件描述',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    last_access_time DATETIME COMMENT '最后访问时间',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除',
    delete_time DATETIME COMMENT '删除时间',

    -- 添加外键约束（如果需要）
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_files_user_id ON user_files(user_id);
CREATE INDEX idx_user_files_file_type ON user_files(file_type);
CREATE INDEX idx_user_files_upload_time ON user_files(upload_time);
CREATE INDEX idx_user_files_is_deleted ON user_files(is_deleted);

CREATE TABLE IF NOT EXISTS ai_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分析结果ID',
    user_file_id BIGINT NOT NULL COMMENT '关联的用户文件ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    external_model_result LONGTEXT COMMENT '外部模型（火山引擎）返回的文字描述',
    python_service_result LONGTEXT COMMENT 'Python微服务返回的完整结果JSON',
    annotated_media_url VARCHAR(500) COMMENT '标注后的媒体文件URL',
    annotated_filename VARCHAR(255) COMMENT '标注媒体文件的存储文件名',
    analysis_status VARCHAR(50) NOT NULL DEFAULT 'pending' COMMENT '分析状态：pending-进行中，completed-完成，failed-失败',
    analysis_time DATETIME COMMENT '分析完成时间',
    error_message VARCHAR(1000) COMMENT '错误消息（如果分析失败）',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    media_type VARCHAR(20) NOT NULL COMMENT '媒体类型（image/video）',
    prompt VARCHAR(1000) COMMENT '分析提示词',

    INDEX idx_user_file_id (user_file_id) COMMENT '用户文件ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_media_type (media_type) COMMENT '媒体类型索引',
    INDEX idx_analysis_status (analysis_status) COMMENT '分析状态索引',
    INDEX idx_analysis_time (analysis_time) COMMENT '分析时间索引',
    INDEX idx_created_time (created_time) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果表';
  -- 添加 external_model_result_url 列
ALTER TABLE `ai_analysis`
ADD COLUMN `external_model_result_url` VARCHAR(500);
  -- 添加 external_model_result_filename 列
  ALTER TABLE ai_analysis
  ADD COLUMN external_model_result_filename VARCHAR(255);