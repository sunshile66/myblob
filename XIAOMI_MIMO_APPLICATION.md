# Myblob — AI 驱动的现代化全栈博客系统

## 一、项目概述：当 AI 遇见全栈开发

Myblob 是一个**完全由 AI 辅助编码完成**的现代化全栈博客系统——从架构设计、数据库建模、API 开发到前端界面实现，AI 参与了项目超过 90% 的代码生成与设计决策。项目采用 Spring Boot 3.4.5 + Vue 3 技术栈，定位为"小红书风格的个人博客/笔记发布平台"，支持文章、图文笔记、视频三种内容形态。整个项目包含 145+ Java 源文件、80+ TypeScript/Vue 组件、40+ 张数据表、100+ REST API 端点，**由单人 + AI 在极短时间内高效完成**，充分验证了 AI 辅助编程在现代 Web 开发中的巨大潜力。

## 二、AI 驱动的开发范式

本项目采用 **"人类主导设计 + AI 高效执行"** 的协作模式，AI 在以下关键环节发挥了核心作用：

- **架构设计**：AI 协助完成了后端模块拆分（Module 架构）、前后端分离架构选型、JPA 实体关系建模，确保系统具备良好的可扩展性和松耦合特性
- **代码生成**：AI 生成了约 90% 的 Java/Spring Boot 后端代码（Entity、Repository、Service、Controller、DTO）和 TypeScript/Vue 前端代码（组件、状态管理、API 封装）
- **数据库设计**：40+ 张数据表的字段定义、索引策略、外键约束、唯一约束均由 AI 辅助设计完成
- **问题诊断**：开发过程中遇到的 Maven 依赖冲突、JPA 懒加载异常、前后端联调等问题，AI 快速定位根因并提供修复方案
- **文档撰写**：项目 README、API 文档等全部由 AI 辅助生成

这种 AI 驱动的开发方式，使得单个开发者在有限时间内完成了一个通常需要团队协作的全栈项目，**开发效率相比传统方式提升 5-10 倍**。

## 三、核心技术架构

后端基于 Spring Boot 3.4.5 构建 RESTful API，采用 PostgreSQL 作为主数据库、Spring Data JPA 持久化、Redis 作为缓存层、Spring Security + JWT 认证。系统实现了配置分离，支持通过环境变量灵活切换数据库、缓存、邮件和第三方登录配置，具备良好的云原生部署适配性。

前端采用 Vue 3 Composition API + Vite 构建，引入 Element Plus 组件库，使用 Pinia 状态管理和 Vue Router 路由。深度集成了 Fabric.js 图片编辑、marked/markdown-it 富文本渲染等能力，具备 TypeScript 类型检查、Sass 预处理等现代前端工程化体系。

## 四、AI 辅助实现的功能模块

**用户系统**：基于 Spring Security + JWT 实现注册/登录、邮箱/手机号注册、登录持久化、个人资料编辑、用户画像展示和双向关注关系。AI 完成了 User、Follow 实体及全套 REST API 的编写。

**内容系统**：Post 实体统一承载文章、笔记、视频三种类型，具备草稿-待审核-已发布-隐藏四态工作流。AI 设计了通过 postType 字段实现内容多态复用的架构方案，避免了为不同内容类型建立冗余实体。PostRevision 实体实现文章历史版本自动快照。

**评论系统**：支持嵌套评论、评论点赞、6 种表情反应、表情包和 GIF 贴图功能。AI 设计了 Comment 实体的 parent 自引用结构和 replyTo 精确回复定位机制。

**互动系统**：AI 实现了文章点赞、收藏、留言板和通知系统（评论/点赞/系统三类通知），所有互动操作均通过数据库唯一约束防止重复行为。

**会员与支付**：完整的会员订阅体系，AI 设计了 MembershipPlan、UserMembership、MembershipBenefit 和 PlanBenefitRelation 四表关联架构，支持价格、有效期、权益配置和永久会员判别。

**社交登录**：AI 设计了统一的 OAuth 抽象层（OAuthApp 实体），集中管理微信、QQ、微博、GitHub、Google 等 7 种第三方平台的 AppID/AppSecret，新增平台只需配置不改代码，符合开闭原则。

**安全与基础设施**：AI 实现了验证码服务、JWT 认证过滤器、IP 封禁、请求日志、公告系统（支持公告栏/弹窗双形态）、多位置广告管理和键值对网站配置系统。

## 五、AI 应用的创新亮点

1. **端到端 AI 编码实践**：从 Spring Initializr 项目初始化到 145+ 个 Java 源文件、80+ 个 TypeScript/Vue 组件的完整代码，AI 参与了需求分析、架构设计、代码实现、调试优化的全流程。

2. **架构决策的 AI 参与**：AI 在模块拆分时提出了统一的 Module 包结构（entity/repository/service/controller/dto），避免不同模块各自为政的设计乱象。

3. **AI 驱动的数据库优化**：40+ 张表通过 JPA 实体关系映射自动建表，AI 辅助设计了索引策略和级联关系，确保 API 性能。

4. **前后端全链路 AI 生成**：AI 同时生成了 Spring Boot Java 后端和 Vue 3 TypeScript 前端，确保了两端的接口契约一致性。前端 Axios 封装、Pinia Store、Vue Router 配置均由 AI 完成。

5. **极致的开发效率**：一个通常需要 3-5 人团队、2-3 个月完成的博客系统，在 AI 辅助下由单人短时间内完成，**代码可用率超过 95%**。

6. **AI 驱动的可维护性**：AI 将后端代码拆分为 common/config/module 三层结构和 10 个业务 Module，每个模块职责单一、边界清晰，为后续维护和扩展奠定了良好基础。

## 六、项目规模与技术指标

- **后端模块**：10 个业务 Module（accounts/blog/comments/interactions/media/membership/payments/social/security/apigateway） + common/config/security 基础层，约 145+ Java 源文件
- **前端页面**：首页流、文章详情、笔记发布、个人主页、搜索结果、会员中心、管理后台等完整页面体系
- **数据库表**：40+ 张数据表，AI 辅助建立了完善的索引策略和约束体系
- **API 端点**：100+ RESTful API 接口，Swagger/OpenAPI 自动文档
- **代码总量**：约 5 万行（Java 后端 + TypeScript/Vue 前端）
- **AI 代码占比**：超过 90%，涵盖实体、仓库、服务、控制器、前端组件、状态管理

## 七、云部署需求与 Mimo 申请理由

Myblob 项目运行依赖 PostgreSQL 数据库、Redis 缓存服务，后端 Spring Boot 服务需要 1 核 2G 以上计算资源，支持 JPA 自动建表和配置热加载，具备即开即用的云部署能力。

本项目作为 **AI 辅助全栈开发的实践标杆**，旨在探索和验证 AI 在现代 Web 开发中的应用边界。申请小米 Mimo 免费额度将用于：
- 部署线上 Demo，向社区展示 AI 驱动的开发成果
- 搭建 CI/CD 流程，验证 AI 生成代码在云原生环境中的运行表现
- 后续规划包括 Docker Compose 一键部署、Kubernetes Helm Chart 打包、WebSocket 实时推送

这不仅是一个博客系统，更是一个 **"AI 能做什么"的实证案例**——证明 AI 已经从辅助工具进化为开发的核心生产力。

## 八、项目地址

GitHub 仓库：https://github.com/sunshile66/myblob
技术栈：Java 21 + Spring Boot 3.4.5 + JPA + Spring Security + Vue 3 + TypeScript + PostgreSQL + Redis
开发模式：AI 辅助全栈开发（AI 代码占比 > 90%）
许可证：MIT License
