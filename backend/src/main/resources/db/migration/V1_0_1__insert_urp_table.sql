-- 插入用户权限相关的初始数据
-- 注意：此应用使用基于文件的用户认证系统 (userInfo.json)
-- 此迁移文件主要用于保持Flyway迁移序列的完整性

-- 更新系统信息表，记录用户权限系统的配置
INSERT INTO system_info (version, description) 
VALUES ('1.0.1', 'User authentication system configured (file-based)');

-- 创建一个配置表来存储系统级别的配置信息
CREATE TABLE IF NOT EXISTS system_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入默认配置
INSERT INTO system_config (config_key, config_value, description) 
VALUES 
('auth_type', 'file_based', '认证类型：基于文件的用户认证'),
('user_info_path', './data/config/userInfo.json', '用户信息文件路径'),
('app_version', '1.0.0', '应用程序版本');

-- 创建触发器，自动更新updated_at字段
CREATE TRIGGER update_system_config_updated_at
    AFTER UPDATE ON system_config
    FOR EACH ROW
BEGIN
    UPDATE system_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;