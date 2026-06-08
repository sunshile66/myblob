-- PostgreSQL 全文搜索迁移脚本
-- 为 knowledge_item 表添加 tsvector 列和索引

-- 1. 添加全文搜索向量列
ALTER TABLE knowledge_item ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- 2. 创建 GIN 索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_knowledge_search ON knowledge_item USING GIN(search_vector);

-- 3. 更新现有数据的搜索向量
UPDATE knowledge_item SET search_vector = 
  to_tsvector('simple', 
    coalesce(title, '') || ' ' || 
    coalesce(content, '') || ' ' || 
    coalesce(tags, '')
  );

-- 4. 创建触发器自动更新
CREATE OR REPLACE FUNCTION knowledge_item_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('simple', 
    coalesce(NEW.title, '') || ' ' || 
    coalesce(NEW.content, '') || ' ' || 
    coalesce(NEW.tags, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 删除旧触发器（如果存在）
DROP TRIGGER IF EXISTS knowledge_item_search_update ON knowledge_item;

-- 创建新触发器
CREATE TRIGGER knowledge_item_search_update 
  BEFORE INSERT OR UPDATE ON knowledge_item
  FOR EACH ROW EXECUTE FUNCTION knowledge_item_search_vector_update();

-- 5. 创建学习进度表
CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES accounts_user(id),
    item_type VARCHAR(50) NOT NULL,
    item_id BIGINT NOT NULL,
    repetitions INTEGER NOT NULL DEFAULT 0,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval INTEGER NOT NULL DEFAULT 0,
    lapses INTEGER NOT NULL DEFAULT 0,
    next_review TIMESTAMP,
    last_review TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_learning_progress_user_id ON learning_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_learning_progress_next_review ON learning_progress(next_review);
CREATE INDEX IF NOT EXISTS idx_learning_progress_item ON learning_progress(item_type, item_id);

-- 添加唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS idx_learning_progress_unique 
ON learning_progress(user_id, item_type, item_id) 
WHERE is_deleted = FALSE;
