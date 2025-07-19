-- 创建任务配置表
-- 字段说明：
-- task_name: 任务名称
-- path: 任务路径
-- openlist_config_id: 关联的openlist_config表ID
-- need_scrap: 是否需要刮削，0-否，1-是
-- need_rename: 是否需要重命名，0-否，1-是
-- cron: 定时任务表达式
-- is_increment: 是否是增量更新，0-否，1-是
-- strm_path: 生成strm的基础路径
-- last_exec_time: 上次执行的时间戳
-- is_active: 是否启用，0-禁用，1-启用
CREATE TABLE task_config
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_name VARCHAR(200) NOT NULL,
    path VARCHAR(500) NOT NULL,
    openlist_config_id INTEGER NOT NULL,
    need_scrap INTEGER DEFAULT 0,
    need_rename INTEGER DEFAULT 0,
    cron VARCHAR(100) DEFAULT '',
    is_increment INTEGER DEFAULT 1,
    strm_path VARCHAR(500) DEFAULT '/strm/',
    last_exec_time BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1
);

-- 创建索引
CREATE INDEX idx_task_config_task_name ON task_config(task_name);
CREATE INDEX idx_task_config_path ON task_config(path);
CREATE INDEX idx_task_config_openlist_config_id ON task_config(openlist_config_id);
CREATE INDEX idx_task_config_active ON task_config(is_active);
CREATE INDEX idx_task_config_cron ON task_config(cron);

-- 创建触发器，自动更新updated_at字段
CREATE TRIGGER update_task_config_updated_at
    AFTER UPDATE ON task_config
    FOR EACH ROW
BEGIN
    UPDATE task_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;