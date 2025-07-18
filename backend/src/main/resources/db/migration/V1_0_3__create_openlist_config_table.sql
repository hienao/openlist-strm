-- 创建openlist配置表
CREATE TABLE openlist_config
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_url VARCHAR(500) NOT NULL,
    token VARCHAR(1000) NOT NULL,
    path VARCHAR(500) DEFAULT '/',
    username VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1
);

-- 创建索引
CREATE INDEX idx_openlist_config_username ON openlist_config(username);
CREATE INDEX idx_openlist_config_active ON openlist_config(is_active);

-- 创建触发器，自动更新updated_at字段
CREATE TRIGGER update_openlist_config_updated_at
    AFTER UPDATE ON openlist_config
    FOR EACH ROW
BEGIN
    UPDATE openlist_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;