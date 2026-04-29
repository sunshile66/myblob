# Myblob - 小米 Mimo 免费额度申请项目摘要

## 一、项目概述

Myblob 是一个面向个人创作者和中小型内容团队的现代化全栈博客系统，采用 Django 6.0（Python 后端）+ Vue 3（TypeScript 前端）技术栈构建。项目定位为"小红书风格的个人博客/笔记发布平台"，支持文章、图文笔记、视频三种内容形态的发布与管理，具备完整的用户体系、内容生态、互动社交和商业化模块。项目代码规模约 7.4 万行，包含 17 个 Django 应用模块和一个独立的前端工程，架构清晰、模块解耦，可直接用于生产环境部署。

## 二、核心技术架构

后端基于 Django REST Framework 构建 RESTful API，采用 PostgreSQL 作为主数据库、Redis 作为缓存层、Celery 处理异步任务队列。系统实现了 dev/prod 双环境配置分离，支持通过环境变量灵活切换数据库、缓存、邮件和第三方登录配置，具备良好的云原生部署适配性——可无缝迁移至 Docker 容器化部署或 Kubernetes 集群管理。

前端采用 Vue 3 Composition API + Vite 构建，引入 Element Plus 组件库保障 UI 一致性，使用 Pinia 进行状态管理、Vue Router 实现前端路由。深度集成了 Fabric.js 实现图片在线编辑功能，支持 marked/markdown-it 实现 Markdown 实时渲染。前端工程具备 TypeScript 类型检查、Sass 预处理、Vite 代理转发等现代前端工程化能力。

## 三、功能模块详解

**用户系统（accounts）**：扩展 Django AbstractUser 模型，新增昵称、头像、邮箱验证字段，支持邮箱/手机号注册、登录状态持久化、个人资料编辑、用户画像展示。通过 Follow 模型实现关注关系，支持双向关注和关注列表查询。

**内容系统（blog）**：核心 Post 模型支持 article（文章）、note（笔记）、video（视频）三种类型，具备草稿-待审核-已发布-隐藏四态工作流。支持分类（Category）和标签（Tag）体系，通过多对多关联实现灵活的内容归类。PostRevision 模型实现了文章历史版本管理，每次编辑自动保存版本快照。封面图片和视频资源通过关联 mediaapp 模块统一管理。数据库层面建立了 status+published_at、author+status、category+status 等复合索引优化查询性能。

**评论系统（comments）**：支持嵌套评论（parent 自引用）、评论点赞（CommentLike）、表情反应（CommentReaction，含 6 种预设表情）、表情包（Emoji）和 GIF 贴图（Sticker）功能。通过 reply_to 字段实现评论的精确回复定位。系统支持匿名评论，nickname 和 email 字段为未登录用户提供评论能力。

**互动系统（interactions）**：实现文章点赞（PostLike）、收藏（Favorite）、留言板（BoardMessage）和通知系统（Notification）。通知系统涵盖评论通知、点赞通知和系统通知三类，支持已读/未读状态管理。所有互动操作均通过唯一约束防止重复行为。

**会员与支付（membership + payments）**：完整的会员订阅体系，包含 MembershipPlan（套餐管理，支持价格、有效期、功能特性 JSON 配置）、UserMembership（用户会员关联，支持永久会员判别）、MembershipBenefit（权益定义）和 PlanBenefitRelation（套餐-权益多对多关联）。支付模块预留了标准化的支付流程接口。

**社交登录（social）**：SocialAccount 模型支持微信、QQ、微博、GitHub、Google、Facebook、Twitter 七种第三方平台绑定。OAuthApp 模型集中管理各平台的 AppID/AppSecret 配置。SocialLoginLog 记录每次登录成败、IP 和 User-Agent，用于安全审计。

**安全与基础设施（security + core）**：security 模块提供验证码功能（captcha.py）和自定义中间件（middleware.py）。core 模块实现 BaseModel 抽象基类、SiteConfig（键值对网站配置）、Announcement（支持公告栏和弹窗两种形态，可配置延迟关闭和自动关闭时间）、Ad（多位置广告管理，支持时间范围投放和点击计数）。apigateway 模块预留了 API 网关层，便于后续引入限流、鉴权等网关功能。

## 四、技术创新点

1. **前后端分离架构**：RESTful API 设计规范，前端通过 Axios 统一封装请求拦截与错误处理，Vite 代理实现开发环境前后端联调零配置。
2. **图片编辑能力**：基于 Fabric.js 实现浏览器端图片编辑功能，支持裁剪、滤镜、标注等操作，提升了内容创作体验。
3. **内容多形态支持**：统一的 Post 模型通过 post_type 区分文章/笔记/视频，避免了为不同内容类型建立冗余模型的设计反模式。
4. **可配置化管理后台**：SiteConfig 键值对配置 + Announcement 公告系统 + Ad 广告系统，使运营人员无需修改代码即可完成站点配置。
5. **第三方登录集成**：统一的 OAuth 抽象层（OAuthApp 模型），新增社交平台只需配置而不改代码，符合开闭原则。

## 五、项目规模与技术指标

- **后端模块**：17 个 Django App，涵盖用户、内容、评论、互动、媒体、会员、支付、社交、安全、网关等完整业务域
- **前端页面**：首页流、文章详情、笔记发布、个人主页、搜索结果、会员中心、管理后台等完整页面体系
- **数据库表**：40+ 张数据表，建立了完善的索引策略和约束体系
- **API 端点**：100+ RESTful API 接口，覆盖全部业务功能
- **代码总量**：约 7.4 万行（含前后端），TypeScript + Python 双语言
- **测试数据**：内置 8 个测试用户和完整的种子数据脚本

## 六、云部署需求与 Mimo 申请理由

Myblob 项目当前运行依赖 PostgreSQL 数据库、Redis 缓存服务和 Celery 异步任务队列，后端 Django 服务需要 1 核 2G 以上的计算资源，前端静态资源可通过 Nginx 反向代理或 CDN 分发。项目已完成了从 SQLite 到 PostgreSQL 的数据库升级，配置文件支持通过环境变量注入数据库连接信息，具备即开即用的云部署能力。

本项目作为个人开源的完整博客解决方案，旨在为中文开发者社区提供一个功能完善、架构现代的博客系统参考实现。申请小米 Mimo 免费额度将用于项目的持续开发与演示环境搭建，包括：部署线上 Demo 供社区体验、运行自动化测试 CI/CD 流程、验证云原生部署方案的可行性。项目后续规划包括 Docker Compose 一键部署方案、Kubernetes Helm Chart 打包，以及基于 WebSocket 的实时通知推送功能。

## 七、项目地址

GitHub 仓库：待推送后更新（当前已提交 494 个文件，73,915 行代码）
技术栈：Python 3.14 + Django 6.0 + Vue 3 + TypeScript + PostgreSQL + Redis + Celery
许可证：MIT License
