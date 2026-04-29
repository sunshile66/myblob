# PostgreSQL 数据库连接数过多问题解决方案

## 问题原因
错误信息：`对不起，已经有太多的客户`
这表明 PostgreSQL 的连接数已达到上限（默认通常是 100 个连接）。

## 已完成的优化

### 1. 修改 Django 数据库配置
已更新 `Myblob/settings/base.py`：
- `CONN_MAX_AGE`: 从 60 改为 0（禁用持久连接）
- `CONN_HEALTH_CHECKS`: 设置为 True（启用健康检查）
- `connect_timeout`: 从 10 秒改为 5 秒

### 2. 这些更改的作用
- **禁用持久连接**：每个请求使用后立即关闭连接，不占用连接池
- **健康检查**：确保使用的连接是有效的
- **减少超时**：快速失败，避免长时间等待

## 手动解决方案（需要管理员权限）

### 方案 1：重启 PostgreSQL 服务（推荐）
以管理员身份运行 PowerShell：
```powershell
# 停止 PostgreSQL 服务
Stop-Service -Name "postgresql-x64-18" -Force

# 等待 3 秒
Start-Sleep -Seconds 3

# 启动 PostgreSQL 服务
Start-Service -Name "postgresql-x64-18"
```

### 方案 2：增加最大连接数
1. 找到 PostgreSQL 配置文件 `postgresql.conf`
   通常位于：`C:\Program Files\PostgreSQL\18\data\postgresql.conf`

2. 修改以下参数：
   ```
   max_connections = 200  # 默认通常是 100，可以增加到 200 或更高
   ```

3. 重启 PostgreSQL 服务

### 方案 3：查看并终止空闲连接
以 postgres 用户身份连接数据库：
```sql
-- 查看所有连接
SELECT pid, usename, application_name, client_addr, state, query_start
FROM pg_stat_activity;

-- 终止空闲连接
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE state = 'idle'
AND pid <> pg_backend_pid();
```

### 方案 4：使用连接池（长期解决方案）
在 production 环境中，建议使用 PgBouncer 等连接池工具。

## 开发环境建议

1. **定期重启服务**：开发时可以每天重启一次 PostgreSQL
2. **减少并发**：减少同时运行的开发服务器数量
3. **关闭不用的服务**：停止不用的 Django 实例

## 验证修复

重启服务后，运行以下命令测试：
```bash
# 进入项目目录
cd d:\code\Myblob

# 激活虚拟环境
.\.venv\Scripts\Activate.ps1

# 运行 Django 服务器
python manage.py runserver
```

然后访问网站，查看是否还有连接错误。

## 监控连接数

可以使用以下 SQL 监控连接使用情况：
```sql
SELECT count(*) as total_connections,
       state,
       application_name
FROM pg_stat_activity
GROUP BY state, application_name
ORDER BY total_connections DESC;
```

## 注意事项

⚠️ **重要**：
- 增加 `max_connections` 会消耗更多内存（每个连接约 10MB）
- 生产环境应该使用连接池（如 PgBouncer）
- 定期检查是否有连接泄漏
