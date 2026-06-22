# Myblob 项目优化计划

> 目标：2核4G 服务器部署 | 工程化结构 | 前后端对齐 | 方便扩展新功能

---

## 总览

| 阶段 | 主题 | 优先级 | 工时 | 状态 |
|------|------|--------|------|------|
| **Phase 1** | 项目结构对齐 | 🔴 高 | 6-8h | 待执行 |
| **Phase 2** | 2核4G 性能适配 | 🔴 高 | 2-3h | 待执行 |
| **Phase 3** | 日志体系建设 | 🟡 中 | 4-6h | 待执行 |
| **Phase 4** | 后端代码拆分 | 🟡 中 | 8-10h | 待执行 |
| **Phase 5** | 前端组件拆分 | 🟢 低 | 6-8h | 待执行 |

---

## Phase 1: 项目结构对齐

> 前后端模块 1:1 对齐，新增功能只需"加一个模块目录"

### 1.1 前端 API 重命名（与后端 module 对齐）

| 当前文件 | 改为 | 对应后端模块 |
|----------|------|-------------|
| `api/auth.ts` + `api/user.ts` | `api/accounts.ts` | `module/accounts/` |
| `api/post.ts` | `api/blog.ts` | `module/blog/` |
| `api/comment.ts` | `api/comments.ts` | `module/comments/` |
| `api/notification.ts` | `api/interactions.ts` | `module/interactions/` |
| `api/news.ts` + `api/newsAdmin.ts` | `api/news.ts`（合并） | `module/news/` |
| `api/core.ts` | `api/core.ts` ✅ | `module/core/` |
| `api/knowledge.ts` | `api/knowledge.ts` ✅ | `module/knowledge/` |
| `api/membership.ts` | `api/membership.ts` ✅ | `module/membership/` |
| `api/payments.ts` | `api/payments.ts` ✅ | `module/payments/` |
| ❌ 缺失 | `api/filemanager.ts` | `module/filemanager/` |
| ❌ 缺失 | `api/flight.ts` | `module/flight/` |
| ❌ 缺失 | `api/apigateway.ts` | `module/apigateway/` |
| ❌ 缺失 | `api/media.ts` | `module/media/` |
| ❌ 缺失 | `api/security.ts` | `module/security/` |

### 1.2 前端 pages 目录规范化

**规则**：所有页面统一为目录形式，名称与后端 module 一致

```
pages/
├── accounts/          ← 对应 module/accounts
│   ├── LoginPage.vue
│   ├── RegisterPage.vue
│   ├── ForgotPasswordPage.vue
│   ├── ResetPasswordPage.vue
│   ├── ProfilePage.vue
│   └── ProfileEditPage.vue
├── blog/              ← 对应 module/blog
│   ├── PostListPage.vue
│   ├── PostDetailPage.vue
│   ├── PostEditorPage.vue
│   ├── CategoryPage.vue
│   └── TagPage.vue
├── comments/          ← 对应 module/comments（组件，嵌入文章详情）
├── news/              ← 对应 module/news
│   ├── NewsListPage.vue
│   └── NewsDetailPage.vue
├── knowledge/         ← 对应 module/knowledge
├── interactions/      ← 对应 module/interactions
│   ├── BoardPage.vue
│   ├── FavoritesPage.vue
│   └── NotificationsPage.vue
├── payments/          ← 对应 module/payments
├── membership/        ← 对应 module/membership
├── filemanager/       ← 对应 module/filemanager
├── flight/            ← 对应 module/flight
├── apigateway/        ← 对应 module/apigateway
├── media/             ← 对应 module/media
├── tools/             ← 无对应后端（纯前端工具）
├── admin/             ← 管理后台（聚合各模块管理页面）
├── search/            ← 搜索（跨模块）
└── legal/             ← 法律页面
```

**需要移动的文件**：

| 当前位置 | 目标位置 |
|----------|----------|
| `pages/auth/*` | `pages/accounts/*` |
| `pages/user/*` | `pages/accounts/*` |
| `pages/post/*` | `pages/blog/*` |
| `pages/note/*` | 合并到 `pages/blog/*` |
| `pages/CategoryPage.vue` | `pages/blog/CategoryPage.vue` |
| `pages/TagPage.vue` | `pages/blog/TagPage.vue` |
| `pages/SearchPage.vue` | `pages/search/SearchPage.vue` |
| `pages/PhotoWallPage.vue` | `pages/media/PhotoWallPage.vue` |
| `pages/interaction/*` | `pages/interactions/*` |
| `pages/flights/*` | `pages/flight/*` |
| `pages/settings/ThemeSettings.vue` | `pages/accounts/ThemeSettings.vue` |
| `pages/admin/SecurityIPBlocksPage.vue` | `pages/admin/security/IPBlocksPage.vue` |
| `pages/admin/SecurityRequestLogsPage.vue` | `pages/admin/security/RequestLogsPage.vue` |
| `pages/admin/UserListPage.vue` | `pages/admin/accounts/UserListPage.vue` |
| `pages/admin/SiteConfigPage.vue` | `pages/admin/core/SiteConfigPage.vue` |
| `pages/admin/news/*` | `pages/admin/news/*` ✅ |

### 1.3 前端 router/modules 对齐

```
router/modules/
├── accounts.ts      ← 认证、用户资料
├── blog.ts          ← 文章、分类、标签
├── news.ts          ← 新闻
├── knowledge.ts     ← 知识中心
├── interactions.ts  ← 留言板、收藏、通知
├── payments.ts      ← 支付、钱包
├── membership.ts    ← 会员
├── filemanager.ts   ← 文件管理
├── flight.ts        ← 航班追踪
├── apigateway.ts    ← API 网关
├── media.ts         ← 媒体库
├── tools.ts         ← 工具箱
├── admin.ts         ← 管理后台
├── search.ts        ← 搜索
└── legal.ts         ← 法律页面
```

### 1.4 前端 components 按业务重组

```
components/
├── blog/            ← 文章相关
│   ├── PostCard.vue
│   ├── PostList.vue
│   └── PostMeta.vue
├── comments/        ← 评论相关
│   ├── CommentItem.vue
│   ├── CommentForm.vue
│   └── CommentTree.vue
├── news/            ← 新闻相关
│   ├── NewsCard.vue
│   └── NewsFilters.vue
├── common/          ← 通用组件
│   ├── AppHeader.vue
│   ├── AppFooter.vue
│   ├── ThemeSwitcher.vue
│   └── NotificationCenter.vue
└── ui/              ← 纯 UI 组件
    ├── BentoGrid.vue
    ├── BentoCard.vue
    └── AnnouncementBar.vue
```

### 1.5 后端微调

| 调整 | 说明 |
|------|------|
| `common/service/VerificationCodeService` → `module/accounts/service/` | 验证码与注册登录绑定 |
| `common/service/Email*` → `common/email/` | 邮件服务独立目录 |
| `security/`(根目录) → `common/security/` | JWT/SecurityConfig 移入 common |
| `seed/DataSeeder` → `common/seed/` | 种子数据统一管理 |

### 1.6 新增模块标准流程

```
后端（5 个文件）:
module/{name}/
├── controller/{Name}Controller.java
├── dto/{Name}DTO.java
├── entity/{Name}.java
├── repository/{Name}Repository.java
└── service/{Name}Service.java

前端（4 个文件）:
api/{name}.ts
pages/{name}/{Name}Page.vue
router/modules/{name}.ts
components/{name}/{Name}Card.vue (可选)
```

---

## Phase 2: 2核4G 性能适配

### 2.1 服务器参数配置

**文件**: `application.yml` + `application-prod.yml`

| 配置项 | 当前值 | 建议值 | 原因 |
|--------|--------|--------|------|
| `hikari.maximum-pool-size` | 20/30 | **10** | 2核无法并行处理30连接 |
| `hikari.minimum-idle` | 5/10 | **3** | 减少空闲连接内存 |
| `server.tomcat.threads.max` | 200 | **50** | 2核无法调度200线程 |
| `server.tomcat.threads.min-spare` | 20 | **10** | 减少空闲线程内存 |
| `server.tomcat.max-connections` | 8192 | **2000** | 匹配实际并发 |
| `news.fetch.thread-pool-size` | 20 | **5** | XML解析消耗CPU |

### 2.2 JVM 启动参数

```bash
java -Xms1g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=4m \
     -XX:ParallelGCThreads=2 \
     -XX:ConcGCThreads=1 \
     -XX:+UseStringDeduplication \
     -jar myblob-backend.jar
```

### 2.3 Nginx 优化

```nginx
# 分级缓存
location ~* \.(woff2?|ttf|eot)$ { expires 30d; }
location ~* \.(js|css)$ { expires 365d; }  # 带hash长期缓存
location ~* \.(png|jpg|jpeg|gif|webp)$ { expires 30d; }

# API 只读缓存（可选）
proxy_cache_path /tmp/nginx-cache levels=1:2 keys_zone=api_cache:10m max_size=100m;
```

### 2.4 RestTemplate 统一

| 文件 | 问题 | 修复 |
|------|------|------|
| `NewsFetchService` | `new RestTemplate()` 无超时 | 注入 Spring Bean |
| `FlightTrackingService` | `new RestTemplate()` 无超时 | 注入 Spring Bean |
| `NewsTranslationService` | `new RestTemplate()` + 自定义超时 | `@Qualifier` 注入 |

### 2.5 异步线程池配置

**新建**: `config/AsyncConfig.java`

```java
@Bean("newsFetchExecutor")
public Executor newsFetchExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("news-fetch-");
    return executor;
}
```

### 2.6 DataSeeder 生产隔离

**文件**: `seed/DataSeeder.java`

添加 `@Profile({"dev", "test"})` 注解，生产环境不加载。

---

## Phase 3: 日志体系建设

### 3.1 创建 logback-spring.xml

**文件**: `src/main/resources/logback-spring.xml`

```xml
<!-- 格式: 时间 [线程] [traceId] [userId] 级别 类名 - 消息 -->
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-}] [%X{userId:-}] %-5level %logger{36} - %msg%n</pattern>
```

**文件输出**:
- `logs/myblob-app.log` - 全量日志
- `logs/myblob-error.log` - 仅 ERROR
- 滚动策略: 100MB/文件，保留30天，总上限3GB

### 3.2 MDC traceId 过滤器

**新建**: `common/logging/TraceFilter.java`

- 从请求头 `X-Request-Id` 读取或生成 UUID
- 注入 MDC: `traceId`、`userId`
- 请求结束后清理 MDC
- 响应头回传 `X-Request-Id`

### 3.3 API 访问日志 AOP

**新建**: `common/logging/ApiAccessLogAspect.java`

```
[API] POST /api/blog/posts/ | user=testuser | ip=1.2.3.4 | 200 | 45ms
```

- 耗时 > 1000ms 自动 WARN（慢请求告警）
- 自动覆盖所有 `@RestController`

### 3.4 核心 Service 补日志（10 个文件）

| 文件 | 需添加的日志点 |
|------|---------------|
| `BlogService` | 文章创建/更新/删除/发布 |
| `UserService` | 注册/登录/关注/密码重置 |
| `CommentService` | 评论创建/删除/点赞 |
| `PostInteractionService` | 点赞/收藏（debug级） |
| `InteractionService` | 留言板/通知 |
| `FileManagerService` | 文件上传/删除/分享 |
| `PaymentService` | 订单创建/支付/充值 |
| `APIGatewayService` | API Key 创建/撤销 |
| `SecurityModuleService` | 会话创建/注销 |
| `MembershipService` | 会员开通/续费 |

### 3.5 审计日志（可选，长期）

**新建**: `common/logging/AuditLog.java`（注解） + `AuditLogAspect.java`（切面）

关键操作持久化到 `audit_log` 表：

| 字段 | 说明 |
|------|------|
| `user_id` | 操作人 |
| `action` | LOGIN / CREATE / DELETE / PAY |
| `resource_type` | POST / COMMENT / USER / ORDER |
| `resource_id` | 资源 ID |
| `ip_address` | 客户端 IP |
| `trace_id` | 关联 traceId |

### 3.6 yml 日志配置统一

移除 `application.yml` 中的 `logging.level` 配置，统一由 `logback-spring.xml` 管理。

---

## Phase 4: 后端代码拆分

### 4.1 NewsFetchService 拆分（1139行 → 6 个文件）

| 新类 | 职责 |
|------|------|
| `NewsFetchService` | 协调器（~100行） |
| `RssFetchStrategy` | RSS 连接/解析/映射 |
| `JsoupFetchStrategy` | 微博/知乎/头条/百度/抖音/GitHub |
| `ApiFetchStrategy` | HackerNews/Reddit API |
| `TranslationCircuitBreaker` | 翻译熔断器 |
| `NewsItemSaver` | 批量去重保存 |

### 4.2 DataSeeder 拆分（322行）

- `AccountSeeder` - 用户种子数据
- `BlogSeeder` - 文章/分类/标签种子数据

### 4.3 NewsSourceSeeder 拆分（393行）

- `GeneralNewsSourceSeeder` - 通用新闻源
- `AirlineNewsSourceSeeder` - 航空公司源
- `ChineseNewsSourceSeeder` - 中文热榜源

---

## Phase 5: 前端组件拆分

### 5.1 超大组件拆分

| 组件 | 行数 | 拆分为 |
|------|------|--------|
| `JsonFormatter.vue` | 1217 | EditorPanel + PreviewPanel + Toolbar |
| `NewsList.vue` | 934 | NewsCard + NewsFilters + NewsPagination |
| `CurlConverter.vue` | 908 | CurlInput + OutputPanel + HistoryList |
| `AuthPage.vue` | 876 | LoginForm + RegisterForm + SocialLogin |
| `NoteEditorPage.vue` | 870 | EditorCore + Toolbar + Preview |

### 5.2 依赖优化

| 依赖 | 操作 |
|------|------|
| `marked` + `markdown-it` | 只保留 `marked` |
| `curlconverter` | 动态 `import()` 懒加载 |
| `fabric` | 动态 `import()` 懒加载 |

---

## 执行顺序

```
Week 1: Phase 1 (结构对齐) + Phase 2 (性能适配)
Week 2: Phase 3 (日志体系) + Phase 4 (代码拆分)
Week 3: Phase 5 (前端组件拆分)
持续:   测试 + 文档
```

---

## 验证清单

每个 Phase 完成后：

- [ ] 后端 `mvn spring-boot:run` 启动无报错
- [ ] 前端 `pnpm typecheck` 类型检查通过
- [ ] 前端 `pnpm dev` 页面正常加载
- [ ] 关键 API 手动测试通过
- [ ] 日志文件正常生成（Phase 3）
- [ ] 新增模块流程验证（Phase 1）
