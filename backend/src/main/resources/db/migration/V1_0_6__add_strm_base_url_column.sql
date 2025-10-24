-- 添加strm_base_url字段用于STRM文件生成时的baseUrl替换
ALTER TABLE openlist_config ADD COLUMN strm_base_url VARCHAR(500);

-- 添加注释说明字段用途
-- strm_base_url: 用于STRM文件生成时替换原始URL的baseUrl，可为空，为空时则不进行替换