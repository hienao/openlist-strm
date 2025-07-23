-- 初始化数据库架构
-- 这是一个占位符迁移文件，用于确保Flyway迁移序列的完整性
-- 实际的用户认证使用基于文件的系统 (userInfo.json)

-- 创建一个简单的系统信息表来记录数据库初始化
CREATE TABLE IF NOT EXISTS system_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(50) NOT NULL,
    initialized_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- 插入初始化记录
INSERT INTO system_info (version, description) 
VALUES ('1.0.0', 'Database schema initialized');