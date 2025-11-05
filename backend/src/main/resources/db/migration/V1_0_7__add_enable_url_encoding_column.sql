-- 添加enable_url_encoding字段用于控制STRM链接编码
ALTER TABLE openlist_config ADD COLUMN enable_url_encoding TINYINT(1) DEFAULT 1;

-- 添加注释说明字段用途
-- enable_url_encoding: 控制是否对STRM文件中的链接进行URL编码，默认为1（启用编码）
-- 1: 启用URL编码（默认行为，保持向后兼容）
-- 0: 禁用URL编码，使用原始URL