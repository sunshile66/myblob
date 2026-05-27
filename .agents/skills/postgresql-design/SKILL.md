---
name: postgresql-design
description: PostgreSQL 数据库设计规范。当设计 PostgreSQL 表结构、索引、查询优化、数据类型选择时使用此 skill。
---

# PostgreSQL 数据库设计规范

## 触发条件

- 设计 PostgreSQL 数据库表结构
- 创建索引和约束
- 优化查询性能
- 选择合适的数据类型

---

## Part 1: 命名规范

| 类型     | 规范                    | 示例                         |
| -------- | ----------------------- | ---------------------------- |
| 表名     | 小写 + 下划线，模块前缀 | `accounts_user`, `blog_post` |
| 主键     | `id` BIGSERIAL          | `id BIGSERIAL PRIMARY KEY`   |
| 外键     | `{表名}_id`             | `user_id`, `post_id`         |
| 索引     | `idx_{表}_{列}`         | `idx_blog_post_status`       |
| 唯一约束 | `uk_{表}_{列}`          | `uk_accounts_user_username`  |
| 布尔列   | `is_` 前缀              | `is_deleted`, `is_active`    |
| 时间列   | `_at` 后缀              | `created_at`, `updated_at`   |

---

## Part 2: 数据类型选择

| 场景      | PostgreSQL 类型             | 说明                         |
| --------- | --------------------------- | ---------------------------- |
| 主键/外键 | `BIGINT`（JPA: `Long`）     | 雪花算法ID                   |
| 短文本    | `VARCHAR(n)`                | 如 VARCHAR(30), VARCHAR(255) |
| 长文本    | `TEXT`                      | 文章内容、JSON               |
| 布尔值    | `BOOLEAN`                   | is_deleted, is_active        |
| 整数      | `INTEGER`                   | 计数、排序                   |
| 时间戳    | `TIMESTAMP` / `TIMESTAMPTZ` | 推荐 TIMESTAMPTZ             |
| JSON      | `JSONB`                     | 配置、元数据                 |
| 数组      | `TEXT[]` / `INTEGER[]`      | 标签列表                     |
| 自增ID    | `BIGSERIAL`                 | 简单主键（可选）             |

---

## Part 3: JPA 实体映射

```java
@Entity
@Table(name = "blog_post", indexes = {
    @Index(name = "idx_post_status_created", columnList = "status, created_at"),
    @Index(name = "idx_post_author", columnList = "author_id")
})
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Column(length = 200, nullable = false)
    private String title;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean deleted = false;
}
```

---

## Part 4: 索引策略

```sql
-- 组合索引：高频查询条件
CREATE INDEX idx_post_status_created ON blog_post(status, created_at DESC);

-- 唯一索引
CREATE UNIQUE INDEX uk_post_slug ON blog_post(slug) WHERE is_deleted = false;

-- 部分索引：仅活跃数据
CREATE INDEX idx_user_active ON accounts_user(id) WHERE is_active = true;

-- GIN索引：JSONB/全文搜索
CREATE INDEX idx_post_content_gin ON blog_post USING GIN(to_tsvector('chinese', content));
```

---

## Part 5: 常见问题

- **NOT NULL 约束**：Django 迁移到 Spring Boot 时，`ddl-auto: update` 不修改已有约束，需手动 `ALTER COLUMN ... DROP NOT NULL`
- **排序规则**：中文排序使用 `COLLATE "zh_CN.utf8"` 或应用层排序
- **时区**：始终使用 `TIMESTAMPTZ`，应用层统一 Asia/Shanghai
