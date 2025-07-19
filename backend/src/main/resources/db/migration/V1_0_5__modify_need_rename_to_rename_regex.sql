-- 修改 need_rename 字段为 rename_regex 字符串类型
-- rename_regex: 重命名正则表达式，为空时表示不需要重命名

-- SQLite 不支持直接修改列类型，需要重建表
-- 1. 创建新表结构
CREATE TABLE task_config_new
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_name VARCHAR(200) NOT NULL,
    path VARCHAR(500) NOT NULL,
    openlist_config_id INTEGER NOT NULL,
    need_scrap INTEGER DEFAULT 0,
    rename_regex VARCHAR(500) DEFAULT '',
    cron VARCHAR(100) DEFAULT '',
    is_increment INTEGER DEFAULT 1,
    strm_path VARCHAR(500) DEFAULT '/strm/',
    last_exec_time BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1
);

-- 2. 迁移数据，将 need_rename 转换为 rename_regex
-- need_rename=1 时设置为默认正则表达式，need_rename=0 时设置为空字符串
INSERT INTO task_config_new (
    id, task_name, path, openlist_config_id, need_scrap, rename_regex, 
    cron, is_increment, strm_path, last_exec_time, created_at, updated_at, is_active
)
SELECT 
    id, task_name, path, openlist_config_id, need_scrap,
    CASE 
        WHEN need_rename = 1 THEN '.*'
        ELSE ''
    END as rename_regex,
    cron, is_increment, strm_path, last_exec_time, created_at, updated_at, is_active
FROM task_config;

-- 3. 删除旧表
DROP TABLE task_config;

-- 4. 重命名新表
ALTER TABLE task_config_new RENAME TO task_config;

-- 5. 重新创建索引
CREATE INDEX idx_task_config_task_name ON task_config(task_name);
CREATE INDEX idx_task_config_path ON task_config(path);
CREATE INDEX idx_task_config_openlist_config_id ON task_config(openlist_config_id);
CREATE INDEX idx_task_config_active ON task_config(is_active);
CREATE INDEX idx_task_config_cron ON task_config(cron);

-- 6. 重新创建触发器
CREATE TRIGGER update_task_config_updated_at
    AFTER UPDATE ON task_config
    FOR EACH ROW
BEGIN
    UPDATE task_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;