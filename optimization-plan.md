# Myblob 全栈项目全面分析与优化方案

## Context

用户要求对 Myblob 全栈博客项目进行全面分析，仔细审查每一个文件并给出优化方案。项目是一个 AI 辅助生成的全栈博客系统，前端 Vue 3 + Vite + TypeScript，后端 Spring Boot 3.4 + JPA + PostgreSQL + Redis，包含博客、新闻聚合、知识中心、工具箱等 15 个业务模块。

通过逐文件审查，发现了安全漏洞、内存泄漏、N+1 查询、软删除不一致、Redis 未充分利用等问题。

---

## 阶段总览

| 阶段 | 主题 | 优先级 | 预计工时 | 风险等级 |
|------|------|--------|----------|----------|
| P0 | 安全加固 | 紧急 | 4-6h | 低 |
| P1 | 稳定性修复 | 高 | 3-4h | 低 |
| P2 | Redis 迁移与缓存 | 中 | 6-8h | 中 |
| P3 | 数据库查询优化 | 中 | 4-6h | 低 |
| P4 | 代码质量与架构 | 低 | 8-12h | 中 |
| P5 | 前端优化 | 低 | 4-6h | 低 |
| P6 | 测试与工程化 | 低 | 持续 | 低 |
| **P7** | **日志体系建设** | **高** | **6-8h** | **低** |

---

## P0: 安全加固（紧急）

### P0-1: 敏感信息外置到环境变量

**问题**: `application.yml` 中硬编码了数据库密码、邮件密码、JWT secret。

**文件**: `myblob-backend/src/main/resources/application.yml`

**修改**:
- 第 12 行 `password: 123456` → `password: ${DB_PASSWORD:123456}`
- 第 53-54 行邮件 → `username: ${MAIL_USERNAME}` / `password: ${MAIL_PASSWORD}`
- 第 68 行 JWT secret → `secret: ${JWT_SECRET}`
- 第 92 行 Twitter API key → `api-key: ${TWITTER_API_KEY}`（移除默认值回退）

**附加**: 创建 `application-dev.yml` 和 `application-prod.yml` 多环境配置文件。

**验证**: 不设置环境变量时 dev 环境使用默认值，设置后 prod 环境使用环境变量值。

---

### P0-2: CORS 配置收紧

**问题**: `SecurityConfig.java` 第 48-52 行允许 12 个 localhost 端口（开发遗留）。

**文件**: `myblob-backend/src/main/java/com/myblob/security/SecurityConfig.java`

**修改**: 将 CORS origins 改为通过 `@Value("${myblob.cors.allowed-origins}")` 注入配置，开发环境仅保留 `http://localhost:3001`。

**验证**: 非白名单 origin 请求返回 CORS 错误，白名单 origin 正常通过。

---

### P0-3: JWT Token 刷新机制

**问题**: 前端 `request.ts` 第 78-89 行，401 直接 logout，用户每 7 天被迫重新登录。

**文件**:
1. `myblob-backend/src/main/java/com/myblob/security/JwtUtil.java` - 增加 refresh token
2. `myblob-backend/src/main/java/com/myblob/module/accounts/controller/AuthController.java` - 增加 `/api/auth/refresh/` 端点
3. `blog-frontend/src/utils/request.ts` - 401 处理改为先 refresh 再重放

**验证**: 登录后 token 过期时自动刷新成功，refresh token 过期后正确跳转登录页。

---

## P1: 稳定性修复（高优先级）

### P1-1: ViewCountService 内存泄漏修复

**问题**: `ViewCountService.java` 第 9 行 `ConcurrentHashMap` 无限增长，`cleanExpiredEntries()` 存在但从未被调用。

**文件**: `myblob-backend/src/main/java/com/myblob/module/blog/service/ViewCountService.java`

**修改**: 给 `cleanExpiredEntries()` 添加 `@Scheduled(fixedRate = 600_000)` 注解（每 10 分钟清理），确保主启动类有 `@EnableScheduling`。

**验证**: 监控堆内存，确认 viewCache 不再无限增长。

---

### P1-2: RateLimitInterceptor 内存泄漏修复

**问题**: `RateLimitInterceptor.java` 第 21 行 `buckets` ConcurrentHashMap 中的 TokenBucket 永不被清除。

**文件**: `myblob-backend/src/main/java/com/myblob/common/RateLimitInterceptor.java`

**修改**: 添加定期清理逻辑，在 `preHandle` 中每 5 分钟清理超过两个窗口周期未使用的 bucket。

**验证**: 压测后等待清理周期，确认 buckets map 大小缩减。

---

## P2: Redis 迁移与缓存（中优先级）

### P2-1: ViewCountService 迁移到 Redis

**问题**: 内存去重，应用重启后丢失状态。

**文件**: `myblob-backend/src/main/java/com/myblob/module/blog/service/ViewCountService.java`

**修改**: 使用 `StringRedisTemplate.opsForValue().setIfAbsent(key, "1", 30min)`，Redis TTL 自动过期，移除 `cleanExpiredEntries()`。

---

### P2-2: RateLimitInterceptor 迁移到 Redis

**问题**: 内存限流不支持分布式部署。

**文件**: `myblob-backend/src/main/java/com/myblob/common/RateLimitInterceptor.java`

**修改**: 使用 Redis `INCR + EXPIRE` 实现滑动窗口限流。

---

### P2-3: 高频查询缓存

**建议缓存策略**:

| 数据 | TTL | 缓存键模式 | 失效策略 |
|------|-----|-----------|---------|
| 新闻列表 | 5 分钟 | `news:list:{page}:{category}` | 新抓取完成后清除 |
| 文章列表 | 10 分钟 | `blog:list:{page}:{category}` | 文章发布/更新/删除时清除 |
| 站点配置 | 30 分钟 | `site:config` | 后台修改时清除 |

---

## P3: 数据库查询优化（中优先级）

### P3-1: UserService.toDTO() N+1 修复

**问题**: `UserService.java` 第 235-244 行，对每个用户执行 3 次独立查询。

**文件**: `myblob-backend/src/main/java/com/myblob/module/accounts/service/UserService.java`

**修改**: 使用已有的批量查询方法 `countFollowersGrouped`、`countFollowingGrouped`、`findFollowingIds`。

---

### P3-2: CommentService 递归 N+1 修复

**问题**: `CommentService.java` 第 231-238 行，递归查询子评论，3 层嵌套每层 5 条 = 155 次查询。

**文件**: `myblob-backend/src/main/java/com/myblob/module/comments/service/CommentService.java`

**修改**: 一次性加载该文章全部评论，在内存中构建树结构。在 `CommentRepository` 新增 `findAllByPostId()` 方法。

---

### P3-3: NewsFetchService 批量保存优化

**问题**: `NewsFetchService.java` 第 973-981 行，逐条 `existsBySourceUrl()` 再逐条 `save()`。

**文件**: `myblob-backend/src/main/java/com/myblob/module/news/service/NewsFetchService.java`

**修改**: 批量查询已存在 URL → 过滤新条目 → `saveAll()` 批量保存。在 `NewsItemRepository` 新增 `findExistingSourceUrls()` 方法。

---

### P3-4: BlogService 软删除一致性修复

**问题**: `BaseEntity.java` 定义了 `deleted` 字段，`CommentService` 正确使用软删除，但 `BlogService.java` 第 270 行使用 `postRepository.delete(post)` 物理删除。

**文件**:
1. `myblob-backend/src/main/java/com/myblob/module/blog/service/BlogService.java` - `deletePost()` 改为 `post.setDeleted(true)`
2. `myblob-backend/src/main/java/com/myblob/module/blog/repository/PostRepository.java` - 8 个查询方法全部添加 `AND p.deleted = false`

**验证**: 删除文章后数据库中 `is_deleted = true`，已删除文章不出现在列表/搜索中。

---

## P4: 代码质量与架构（低优先级）

### P4-1: NewsFetchService 拆分

**问题**: `NewsFetchService.java` 984 行，职责过多。

**拆分方案**:

| 新类 | 职责 |
|------|------|
| `NewsFetchService` | 协调器：遍历源、调度策略 |
| `RssFetchStrategy` | RSS 解析、连接管理 |
| `ApiFetchStrategy` | REST API 调用 |
| `JsoupFetchStrategy` | HTML 页面解析 |
| `TranslationCircuitBreaker` | 翻译熔断器 |
| `NewsItemSaver` | 批量去重保存 |

---

### P4-2: ddl-auto 生产安全

**文件**: `application.yml` 第 22 行 `ddl-auto: update`

**修改**: 新建 `application-prod.yml` 设置 `ddl-auto: validate`，长期引入 Flyway。

---

## P5: 前端优化（低优先级）

### P5-1: 大组件拆分

| 组件 | 行数 | 拆分建议 |
|------|------|---------|
| SimpleNavbar | 690 行 | Logo + NavLinks + UserMenu + MobileDrawer |
| NewsList | 463 行 | NewsCard + NewsFilters + NewsPagination |
| ToolsHome | 528 行 | ToolCard + ToolCategories + ToolSearch |

### P5-2: 构建优化

- `@element-plus/icons-vue` 按需导入
- `marked` 和 `markdown-it` 评估是否只保留一个
- 路由组件使用 `defineAsyncComponent` 懒加载

---

## P6: 测试与工程化（持续）

优先编写测试的模块:
1. `SecurityConfig` / `JwtUtil` - 认证鉴权核心
2. `ViewCountService` - 内存泄漏修复回归
3. `CommentService` - 递归评论树
4. `NewsFetchService.saveNewItems` - 批量去重
5. `BlogService.deletePost` - 软删除行为

---

## P7: 日志体系建设（高优先级）

### 现状问题

项目有 118 处日志调用，但**仅覆盖后台定时任务模块**。10 个核心业务 Service（共 ~2000 行）**完全没有日志**：

| 模块 | 行数 | 日志状态 |
|------|------|---------|
| `BlogService` | 376 | ❌ 零日志 |
| `UserService` | 255 | ❌ 零日志 |
| `CommentService` | 268 | ❌ 零日志 |
| `PostInteractionService` | 101 | ❌ 零日志 |
| `InteractionService` | 115 | ❌ 零日志 |
| `FileManagerService` | 226 | ❌ 零日志 |
| `PaymentService` | 260 | ❌ 零日志 |
| `APIGatewayService` | 212 | ❌ 零日志 |
| `SecurityModuleService` | 124 | ❌ 零日志 |
| `MembershipService` | - | ❌ 零日志 |

缺失的基础设施：`logback-spring.xml`、MDC traceId、API 访问日志 AOP、审计日志。

---

### P7-1: 创建 logback-spring.xml

**文件**: `myblob-backend/src/main/resources/logback-spring.xml`

**配置要点**:
- 统一日志格式：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] [%X{userId}] %-5level %logger{36} - %msg%n`
- MDC 默认值：无 traceId 时输出 `--`，无 userId 时输出 `anonymous`
- 按模块分文件：`logs/myblob-blog.log`、`logs/myblob-news.log`、`logs/myblob-payment.log`
- 错误日志独立文件：`logs/myblob-error.log`（只记录 ERROR 级别）
- 日志滚动：单文件 100MB，保留 30 天，总上限 3GB
- 生产环境 root=WARN，com.myblob=INFO；开发环境 root=INFO，com.myblob=DEBUG

**验证**: 启动后检查 `logs/` 目录下是否生成对应日志文件，日志格式是否包含 traceId。

---

### P7-2: MDC traceId 过滤器

**新建文件**: `myblob-backend/src/main/java/com/myblob/common/logging/TraceFilter.java`

**职责**:
- 从请求头 `X-Request-Id` 读取 traceId，无则生成 UUID
- 注入 MDC：`traceId`、`userId`（从 SecurityContext 获取）
- 请求结束后清理 MDC（防止线程复用泄漏）
- 将 traceId 写入响应头 `X-Request-Id`

**注册**: 在 `WebConfig` 中注册为最高优先级 Filter。

**验证**: 发送 API 请求，检查响应头包含 `X-Request-Id`，日志中同一请求的所有行共享同一 traceId。

---

### P7-3: API 访问日志 AOP

**新建文件**: `myblob-backend/src/main/java/com/myblob/common/logging/ApiAccessLog.java`（注解）
**新建文件**: `myblob-backend/src/main/java/com/myblob/common/logging/ApiAccessLogAspect.java`（切面）

**记录内容**:
```
[API] POST /api/blog/posts/ | user=testuser | ip=1.2.3.4 | status=200 | duration=45ms
```

**实现要点**:
- 使用 `@Around` 切面，记录方法名、路径、HTTP 方法、用户、IP、状态码、耗时
- 耗时 > 1000ms 自动升级为 WARN 级别（慢请求告警）
- 请求体过大时不记录 body（文件上传等），只记录 Content-Length
- 在 `GlobalExceptionHandler` 的 `handleGeneric` 中记录完整异常堆栈时附带 traceId

**覆盖范围**: 所有 `@RestController` 的公开方法自动生效（通过 `@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")` ）。

**验证**: 调用任意 API，检查日志输出包含完整的访问记录。

---

### P7-4: 核心业务 Service 补日志

为 10 个无日志的 Service 添加 `@Slf4j` 和关键日志点：

#### BlogService（376行）
```java
@Slf4j  // 添加
// createPost: log.info("文章创建成功: id={}, title={}, author={}", post.getId(), post.getTitle(), userId);
// updatePost: log.info("文章更新: slug={}, editor={}", slug, userId);
// deletePost: log.info("文章软删除: slug={}, operator={}", slug, userId);
// publishPost: log.info("文章发布: slug={}", slug);
```

#### UserService（255行）
```java
@Slf4j  // 添加
// register: log.info("新用户注册: username={}, email={}", request.getUsername(), request.getEmail());
// login: log.info("用户登录: username={}", request.getUsername());
// login失败: log.warn("登录失败: username={}, reason=密码错误", request.getUsername());
// forgotPassword: log.info("密码重置申请: email={}", email);
// resetPassword: log.info("密码重置完成: userId={}", user.getId());
// followUser: log.info("用户关注: follower={}, following={}", currentUserId, userId);
```

#### CommentService（268行）
```java
@Slf4j  // 添加
// createComment: log.info("评论创建: postId={}, userId={}, parentId={}", request.getPostId(), userId, request.getParentId());
// deleteComment: log.info("评论删除: commentId={}, operator={}", commentId, userId);
// likeComment: log.info("评论点赞: commentId={}, userId={}", commentId, userId);
```

#### PostInteractionService（101行）
```java
@Slf4j  // 添加
// likePost: log.debug("文章点赞: slug={}, userId={}", slug, userId);
// favoritePost: log.debug("文章收藏: slug={}, userId={}", slug, userId);
```

#### FileManagerService（226行）
```java
@Slf4j  // 添加
// uploadFile: log.info("文件上传: userId={}, filename={}, size={}KB", userId, originalFilename, multipartFile.getSize() / 1024);
// deleteFile: log.info("文件删除: userId={}, fileId={}", userId, fileId);
// createShareLink: log.info("分享链接创建: userId={}, fileId={}, expires={}", userId, fileId, expiry);
```

#### PaymentService（260行）
```java
@Slf4j  // 添加
// createOrder: log.info("订单创建: userId={}, planId={}, amount={}", userId, planId, plan.getPrice());
// payOrder: log.info("订单支付: orderId={}, method={}, amount={}", orderId, paymentMethod, order.getAmount());
// payOrder失败: log.warn("订单支付失败: orderId={}, reason={}", orderId, e.getMessage());
// recharge: log.info("钱包充值: userId={}, amount={}", userId, amount);
```

#### APIGatewayService（212行）
```java
@Slf4j  // 添加
// createAPIKey: log.info("API Key 创建: userId={}, name={}", userId, name);
// revokeAPIKey: log.info("API Key 撤销: userId={}, keyId={}", userId, keyId);
// callAPI: log.info("API 调用: keyId={}, endpoint={}, status={}", keyId, endpoint, statusCode);
```

#### InteractionService（115行）
```java
@Slf4j  // 添加
// createBoardMessage: log.info("留言板消息: userId={}, isPublic={}", userId, request.getIsPublic());
// markAllNotificationsRead: log.info("通知全部已读: userId={}", userId);
```

#### SecurityModuleService（124行）
```java
@Slf4j  // 添加
// createSession: log.info("会话创建: userId={}, ip={}", userId, ipAddress);
// deactivateSession: log.info("会话注销: userId={}, sessionId={}", userId, sessionId);
```

**验证**: 执行上述操作后，检查日志文件中是否有对应的 INFO/WARN 记录。

---

### P7-5: 审计日志（可选，长期）

**新建文件**: `myblob-backend/src/main/java/com/myblob/common/logging/AuditLog.java`（注解）
**新建文件**: `myblob-backend/src/main/java/com/myblob/common/logging/AuditLogAspect.java`（切面）

**记录到数据库**（审计日志需要持久化，不能只写文件）:

| 字段 | 说明 |
|------|------|
| `user_id` | 操作人 |
| `action` | 操作类型：LOGIN / LOGOUT / CREATE / UPDATE / DELETE / PAY |
| `resource_type` | 资源类型：POST / COMMENT / USER / ORDER / FILE |
| `resource_id` | 资源 ID |
| `detail` | 操作详情 JSON |
| `ip_address` | 客户端 IP |
| `trace_id` | 关联 traceId |
| `created_at` | 操作时间 |

**使用方式**: 在关键方法上添加 `@AuditLog(action = "DELETE", resourceType = "POST")`

**新建表**: `audit_log` 表 + `AuditLogEntity` + `AuditLogRepository`

**验证**: 执行文章删除、用户登录等操作后，查询 `audit_log` 表有对应记录。

---

### P7-6: 日志配置集成

**修改文件**: `application.yml` 和 `application-prod.yml`

移除 yml 中的 `logging.level` 配置，统一由 `logback-spring.xml` 管理：

```yaml
# application.yml - 只保留文件路径
logging:
  config: classpath:logback-spring.xml
```

```yaml
# application-prod.yml - 生产环境覆盖
logging:
  config: classpath:logback-spring.xml
  file:
    name: ./logs/myblob.log
```

---

## 执行计划

```
Week 1: P0 (安全加固) + P1 (稳定性修复)
Week 2: P7 (日志体系建设) + P3 (数据库查询优化)
Week 3: P2 (Redis 迁移)
Week 4: P4 (代码重构) + P5 (前端优化)
持续:   P6 (测试)
```

## 风险提示

1. **P3-4 软删除修复** 影响 8 个 Repository 查询，建议全面回归测试
2. **P2 Redis 迁移** 需确认生产环境 Redis 可用性
3. **P0-3 Token 刷新** 涉及前后端联调，建议独立分支开发
4. **P7 日志体系建设** 日志量增加需监控磁盘空间，建议配合日志轮转
5. 所有改动建议在 Git 分支进行，每个阶段独立提交

## 验证方式

每个阶段完成后：
1. 运行后端 `mvn spring-boot:run` 确认启动无报错
2. 运行前端 `pnpm dev` 确认页面正常加载
3. 对关键 API 进行手动测试或编写测试用例
4. 检查 SQL 日志确认查询优化效果
5. **P7 完成后**: 检查 `logs/` 目录生成的日志文件，确认 traceId 贯穿请求链路
