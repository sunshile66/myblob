package com.myblob.seed;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.entity.UserRole;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.blog.entity.Category;
import com.myblob.module.blog.entity.Post;
import com.myblob.module.blog.entity.Tag;
import com.myblob.module.blog.repository.CategoryRepository;
import com.myblob.module.blog.repository.PostRepository;
import com.myblob.module.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 开发/测试数据种子。
 * 判断用户 18945223056@163.com 是否存在，若不存在则创建用户 + 分类 + 标签 + 50 篇测试笔记。
 * 幂等：已存在该邮箱用户时跳过全部数据创建。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    private static final String SEED_EMAIL = "18945223056@163.com";
    private static final String SEED_USERNAME = "testuser";
    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    @Override
    public void run(String... args) {
        // 检查用户是否已存在且为最新版本
        if (userRepository.existsByEmail(SEED_EMAIL) && userRepository.existsByUsername(SEED_USERNAME)) {
            log.info("📌 种子用户已存在，跳过数据填充。");
            return;
        }

        // 如果旧版用户（username匹配但email不同）存在，升级邮箱和密码
        if (userRepository.existsByUsername(SEED_USERNAME)) {
            User oldUser = userRepository.findByUsername(SEED_USERNAME).orElseThrow();
            log.warn("🔄 发现旧版用户(username={}, email={})，升级邮箱和密码...", oldUser.getUsername(), oldUser.getEmail());
            oldUser.setEmail(SEED_EMAIL);
            oldUser.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(oldUser);
            log.info("✅ 旧版用户已升级");
            return;
        }

        log.info("🌱 开始填充测试数据……");
        seedAll();
        log.info("✅ 测试数据填充完成：1 用户 + 50 篇笔记。");
    }

    @Transactional
    void seedAll() {
        // 1. 创建用户
        User user = createUser();

        // 2. 创建默认分类
        Category category = ensureCategory("分享", "share");
        ensureCategory("技术", "tech");
        ensureCategory("生活", "life");

        // 3. 创建常用标签
        List<Tag> allTags = new ArrayList<>();
        allTags.add(ensureTag("前端", "frontend"));
        allTags.add(ensureTag("后端", "backend"));
        allTags.add(ensureTag("设计", "design"));
        allTags.add(ensureTag("AI", "ai"));
        allTags.add(ensureTag("效率", "efficiency"));
        allTags.add(ensureTag("美食", "food"));
        allTags.add(ensureTag("旅行", "travel"));
        allTags.add(ensureTag("摄影", "photography"));
        allTags.add(ensureTag("阅读", "reading"));
        allTags.add(ensureTag("健身", "fitness"));

        // 4. 创建 50 篇笔记
        for (int i = 1; i <= 50; i++) {
            Post post = buildPost(i, user, category, allTags);
            postRepository.save(post);
            if (i % 10 == 0) {
                postRepository.flush();
                log.info("   已创建 {}/50 篇笔记", i);
            }
        }
    }

    private User createUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail(SEED_EMAIL);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname("测试用户");
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    private Category ensureCategory(String name, String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name).slug(slug).active(true).sort(0).build()));
    }

    private Tag ensureTag(String name, String slug) {
        return tagRepository.findBySlug(slug)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .name(name).slug(slug).build()));
    }

    private Post buildPost(int index, User author, Category category, List<Tag> allTags) {
        String title = TITLES[(index - 1) % TITLES.length];
        String content = CONTENTS[(index - 1) % CONTENTS.length];
        String summary = title.length() > 60 ? title.substring(0, 57) + "..." : title + "——阅读全文了解更多。";

        // 每篇随机挂 1~3 个标签
        Set<Tag> tags = new HashSet<>();
        int tagCount = R.nextInt(1, 4);
        List<Tag> shuffled = new ArrayList<>(allTags);
        Collections.shuffle(shuffled);
        for (int i = 0; i < tagCount; i++) {
            tags.add(shuffled.get(i));
        }

        long ts = System.currentTimeMillis() + index;
        String slug = "post-seed-" + index + "-" + ts;

        Post post = new Post();
        post.setTitle(title);
        post.setSlug(slug);
        post.setSummary(summary);
        post.setContent(content);
        post.setAuthor(author);
        post.setCategory(category);
        post.setTags(tags);
        post.setPostType(Post.PostType.NOTE);
        post.setStatus(Post.Status.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now().minusDays(R.nextInt(0, 90)));
        post.setViewCount(R.nextInt(100, 8000));
        post.setLikeCount(R.nextInt(5, 600));
        post.setCommentCount(R.nextInt(0, 120));
        post.setAllowComment(true);
        post.setOriginal(true);
        post.setTop(index <= 3);
        return post;
    }

    // ==================== 50 组中文笔记标题与正文 ====================

    private static final String[] TITLES = {
            "2025 年前端技术趋势：从 React 到 AI 驱动开发",
            "Spring Boot 3.4 新特性一览及迁移指南",
            "如何用 Vue 3 + Vite 快速搭建个人博客",
            "PostgreSQL 索引优化实战：从慢查询到毫秒级",
            "CSS Grid 与 Flexbox 布局选择指南",
            "TypeScript 高级类型体操：你真的会用泛型吗？",
            "REST API 设计最佳实践 2025 版",
            "Java 21 虚拟线程：高并发应用的新未来",
            "Docker Compose 编排微服务全流程",
            "Redis 缓存最佳实践：击穿、穿透、雪崩一网打尽",
            "Git 工作流规范：从混乱到优雅的提交历史",
            "JWT 认证与 Spring Security 整合实战",
            "前端性能优化：从 Lighthouse 60 分到 95 分",
            "WebSocket 实时通信在 Spring Boot 中的应用",
            "Element Plus 组件库深度定制指南",
            "Nginx 反向代理与负载均衡配置详解",
            "Linux 常用命令速查：开发者的瑞士军刀",
            "MySQL vs PostgreSQL：2025 年该怎么选？",
            "响应式设计从入门到精通",
            "微服务架构中的分布式事务解决方案",
            "Elasticsearch 搜索引擎实战入门",
            "Go 语言与 Java 性能对比：谁更快？",
            "Kubernetes 入门：Pod、Service、Deployment 一网打尽",
            "JavaScript 异步编程：从回调地狱到 async/await",
            "GitHub Actions CI/CD 流水线搭建指南",
            "Python 数据可视化：用 Matplotlib 画出惊艳图表",
            "IntelliJ IDEA 高效快捷键完全手册",
            "MongoDB vs Redis：NoSQL 数据库选型对比",
            "Webpack 到 Vite：前端构建工具迁移之路",
            "设计模式在业务代码中的实际应用",
            "单元测试编写指南：JUnit 5 + Mockito 实战",
            "Git 高级技巧：rebase、cherry-pick、stash 详解",
            "SQL 查询优化 10 个实用技巧",
            "正则表达式完全指南：从入门到写出复杂模式",
            "消息队列 RocketMQ 入门与实践",
            "Python FastAPI vs Flask：轻量框架怎么选？",
            "OAuth 2.0 授权码流程图解",
            "前端工程化：ESLint + Prettier + Husky 配置",
            "MyBatis vs JPA：ORM 框架选型思考",
            "HTTPS 加密原理：SSL/TLS 握手机制详解",
            "微服务监控：Prometheus + Grafana 搭建指南",
            "Node.js 后端框架对比：Express、Koa、NestJS",
            "浏览器渲染原理：从 HTML 到像素",
            "高并发场景下的限流策略",
            "Markdown 写作技巧：让你的文档更专业",
            "GitHub Copilot 使用体验：AI 辅助编程的真实感受",
            "CSS 动画入门：transition 与 keyframes 详解",
            "REST vs GraphQL vs gRPC：API 风格对比",
            "自动化测试金字塔：单元、集成、端到端",
            "开发者如何建立个人技术品牌"
    };

    private static final String[] CONTENTS = {
            "💡 **前言**\n\n在快速迭代的技术世界里，保持对前沿技术的敏感度是每个开发者的必修课。\n\n本文总结了我在实际项目中积累的经验和思考，希望对你有所帮助。\n\n## 为什么关注这个话题\n\n技术选型和架构设计直接影响项目的长期维护成本和团队效率。一个好的开始可以让后续开发事半功倍。\n\n## 核心要点\n\n1. **理解底层原理**：不要停留在会用层面，深入理解实现机制\n2. **实践出真知**：亲手写代码验证，而不是只看文档\n3. **关注社区动态**：GitHub Issues、技术博客、开源项目都是很好的学习渠道\n\n## 实际案例\n\n最近在一个项目中，我们遇到了一个性能瓶颈。通过分析日志和监控数据，发现问题根源在于……\n\n经过优化后，响应时间从 800ms 降到了 50ms，效果非常显著。\n\n## 总结\n\n技术学习是一个持续的过程。保持好奇心，坚持实践，你会在不知不觉中成长。\n\n欢迎在评论区分享你的经验和想法！",

            "✍️ **开篇**\n\n最近花了些时间整理自己的技术笔记，发现很多知识点当时觉得记住了，但过一段时间再看又会感到陌生。\n\n于是决定把这些内容写成博客，既是对自己的梳理，也希望能帮助到遇到同样问题的朋友。\n\n## 从零开始\n\n刚开始接触这项技术时，我和大多数人一样感到困惑。官方的文档虽然全面，但对于新手来说信息密度太高。\n\n后来我找到了一个适合自己节奏的学习方法：\n- 先看快速入门，建立整体认知\n- 做一个 mini 项目，解决实际问题\n- 遇到困难再回到文档深究细节\n\n## 那些踩过的坑\n\n第一个坑：版本兼容性问题。不同版本之间的 API 变化很大，网上的教程可能已经过时。\n\n解决方案：优先查阅官方文档，查看项目的 CHANGELOG。\n\n第二个坑：配置项太多不知道如何选择。\n\n解决方案：从默认配置开始，按需调整。不要一开始就追求完美配置。\n\n## 学以致用\n\n掌握基础知识后，我在实际项目中应用了这些技术，效果很好。团队的其他成员也很快上手了。\n\n最重要的是形成自己的知识体系，而不是零散地堆积信息。\n\n---\n\n**如果你也在学习这个方向，欢迎一起交流！**",

            "🎯 **背景**\n\n最近参与了公司一个新项目的技术选型讨论，其中涉及到多个技术方案的对比和决策。\n\n我想把这些思考过程记录下来，对于面临类似选择的朋友可能会有参考价值。\n\n## 技术选型的考量维度\n\n1. **团队熟悉度**：团队对技术栈的掌握程度直接影响开发效率\n2. **社区活跃度**：活跃的社区意味着更好的支持和更多的资源\n3. **性能需求**：根据业务场景选择合适的技术方案\n4. **长期维护**：考虑技术的长期发展和维护成本\n\n## 方案对比\n\n| 维度 | 方案 A | 方案 B |\n|------|--------|--------|\n| 上手难度 | ⭐⭐ | ⭐⭐⭐⭐ |\n| 社区生态 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |\n| 性能 | ⭐⭐⭐ | ⭐⭐⭐⭐ |\n| 扩展性 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |\n\n## 最终决策\n\n综合考虑以上因素，我们最终选择了方案 A。虽然在某些方面不是最优，但它最符合团队当前的需求和能力。\n\n技术选型没有银弹，适合的才是最好的。",

            "📖 **前记**\n\n写代码多年，越来越觉得代码质量比代码数量重要得多。\n\n今天想聊聊我理解的好代码应该长什么样，以及一些实用的编码习惯。\n\n## 好代码的特征\n\n- **可读性强**：别人能快速理解你的意图\n- **命名规范**：变量和方法名应该自解释\n- **单一职责**：一个函数只做一件事\n- **合理的注释**：解释\"为什么\"而不是\"做什么\"\n\n## 我的一些编码习惯\n\n1. 写代码前先想清楚逻辑，用注释列出步骤\n2. 先让代码跑通，再做优化\n3. 每次提交前用 git diff 检查自己的改动\n4. 定期重构，保持代码整洁\n\n## 重构的勇气\n\n很多人害怕重构，担心改出 bug。其实只要做好测试覆盖，重构并没有那么可怕。\n\n我习惯在重构前先补充单元测试，这样重构后能快速验证功能是否正常。\n\n## 小结\n\n好的代码习惯需要刻意练习。每天进步一点点，日积月累会有很大的变化。",

            "🔥 **热点聚焦**\n\n这几天技术圈讨论最热烈的话题，莫过于最近发布的新版本带来的各种变化。\n\n我用了一个周末的时间把新版本过了一遍，整理了一些值得关注的点。\n\n## 重要变化\n\n这次更新带来了几个关键改进：\n- 性能大幅提升，官方数据显示在某些场景下提升了 40%\n- 新的 API 设计更加简洁直观\n- 修复了多个长期存在的 bug\n\n## 迁移注意事项\n\n如果你打算升级到新版本，需要注意以下几点：\n1. 先仔细阅读升级指南\n2. 废弃的 API 要尽早替换\n3. 做好回归测试\n4. 分阶段灰度发布\n\n## 个人评价\n\n整体来说，这次更新质量很高。虽然有一些 breaking changes，但带来的收益是值得的。\n\n推荐还在观望的同学尽快尝试。\n\n有升级遇到问题的朋友？评论区见！",

            "🌅 **序**\n\n不知不觉已经在这个行业做了好几年，从最开始的懵懂小白到现在的能够独立负责项目。\n\n想写一篇文章记录下这个成长过程中的一些重要经验和教训。\n\n## 新人阶段的三个关键习惯\n\n**1. 学会提问**\n\n一个好的问题包含：\n- 描述你在做什么\n- 你期望什么结果\n- 实际发生了什么\n- 你已经尝试了什么方案\n\n**2. 阅读源码**\n\n当你对某个功能的实现感到好奇时，不要停留在文档层面，直接去看源码。\n\n一开始可能看不懂，但坚持下去，你会发现自己的技术视野会大大开阔。\n\n**3. 写技术博客**\n\n输出是最好的输入。通过写作整理自己的思路，既能巩固知识，也能帮助他人。\n\n## 进阶阶段\n\n当基础扎实后，需要关注的点转向了：\n- 系统架构设计\n- 性能优化\n- 安全防护\n- 团队协作\n\n每个方向都值得深入学习。\n\n## 写在最后\n\n技术这条路很长，保持学习的热情和好奇心是最重要的。与大家共勉！",

            "⚡ **快速上手**\n\n今天用 30 分钟搭建了一个完整的 demo 项目，把过程记录下来分享给大家。\n\n## 环境准备\n\n- JDK 17+\n- Maven 3.8+\n- IDE（推荐 IntelliJ IDEA）\n\n## 步骤一：创建项目\n\n使用 Spring Initializr 快速生成项目骨架，选择以下依赖：\n- Spring Web\n- Spring Data JPA\n- PostgreSQL Driver\n- Lombok\n\n## 步骤二：配置数据源\n\n在 application.yml 中配置数据库连接信息。注意生产环境不要将密码提交到代码仓库。\n\n## 步骤三：编写实体和仓库\n\n定义 JPA 实体类，继承 BaseEntity 可以自动获得 id、创建时间等通用字段。\n\n## 步骤四：实现业务逻辑\n\n按照 Controller → Service → Repository 的分层结构编写代码。\n\n## 步骤五：测试验证\n\n启动项目后使用 Postman 或 curl 测试 API 接口。\n\n## 总结\n\n整个流程下来不到 30 行配置代码，Spring Boot 确实大大简化了开发。\n\n下一步可以在此基础上添加认证、缓存、消息队列等功能。",

            "🎨 **设计思考**\n\nUI 设计不仅是美观的问题，更关乎用户体验和转化率。\n\n## 设计原则\n\n1. **简洁至上**：删除不必要的元素\n2. **一致性**：保持组件和行为的一致性\n3. **反馈及时**：每个操作都应有明确的视觉反馈\n4. **容错设计**：考虑异常状态和边界情况\n\n## 颜色系统\n\n建立语义化的颜色变量：\n- Primary（主色）\n- Secondary（辅助色）\n- Success / Warning / Danger（状态色）\n- Text Primary / Secondary（文本层级）\n\n## 字体和间距\n\n推荐使用 8px 基础间距系统，确保视觉节奏统一。\n\n字体大小建议：\n- 标题：24-32px\n- 正文：14-16px\n- 辅助文字：12-13px\n\n## 响应式设计\n\n从移动端开始设计，逐步增强到桌面端。使用媒体查询和弹性布局实现适配。\n\n好的设计是隐形的——用户感觉不到它的存在，只觉得一切都很自然。",

            "💻 **开发日记**\n\n今天遇到了一个有趣的技术问题，排查过程很有启发，记录一下。\n\n## 问题描述\n\n生产环境偶尔出现接口响应超时，但不是每次都发生，排查起来比较困难。\n\n## 排查过程\n\n1. 查看日志：发现某些请求耗时异常长\n2. 分析 SQL：检查慢查询日志\n3. 监控指标：查看 CPU、内存、连接池使用情况\n4. 代码审查：检查是否存在 N+1 查询\n\n## 根本原因\n\n最终定位到一个不起眼的循环中的数据库查询。数据量小时不明显，当数据量增长后就暴露了。\n\n## 解决方案\n\n改用批量查询，将 N 次查询合并为 1 次。修改后响应时间恢复正常。\n\n## 经验总结\n\n- 代码 Review 要关注循环中的数据库操作\n- 定期检查慢查询日志\n- 在生产环境部署 APM 监控工具\n\n分享出来，希望大家少踩这个坑！",

            "🌟 **经验分享**\n\n梳理了这几年用过的各种开发工具和效率技巧，精选出最实用的分享给大家。\n\n## 开发工具推荐\n\n- **VS Code / IntelliJ IDEA**：两大主力编辑器\n- **Postman / Insomnia**：API 调试工具\n- **Docker Desktop**：本地容器化开发环境\n- **iTerm2 / Windows Terminal**：增强终端体验\n\n## 效率技巧\n\n1. 善用快捷键，减少鼠标操作\n2. 使用代码片段（Snippet）提高重复代码编写速度\n3. 搭建自己的 Cheat Sheet，减少搜索时间\n4. 番茄工作法保持专注\n\n## 知识管理\n\n- 用 Notion / Obsidian 记录学习笔记\n- 定期整理和回顾，温故知新\n- 将学到的东西写出来，教给他人是最好的学习方式\n\n## 最后\n\n工具只是手段，重要的是保持学习的心态和执行力。\n\n你有什么好用的效率工具？欢迎推荐！",

            "## 开篇\n\n最近在做项目的技术债务清理，感叹一个好的架构设计真的非常重要。\n\n## 什么是好的架构\n\n好的架构应该具备以下特征：\n- **模块化**：各个模块职责清晰，耦合度低\n- **可扩展**：新增功能时改动最小\n- **可测试**：方便编写自动化测试\n- **易理解**：新人能快速上手\n\n## 常见的架构模式\n\n### 分层架构\n\nController → Service → Repository → Database\n\n最经典的 Java Web 应用架构，适合大多数中小型项目。\n\n### 六边形架构\n\n将业务逻辑与外部依赖隔离，通过端口和适配器实现松耦合。\n\n### DDD 领域驱动设计\n\n围绕业务领域建模，适合复杂业务场景。\n\n## 如何选择\n\n不要为了架构而架构。根据项目的实际复杂度和团队能力选择合适的方式。\n\n- 小型项目：分层架构足够\n- 中型项目：分层 + 模块化\n- 大型项目：考虑 DDD 或微服务\n\n## 实践建议\n\n不管选择哪种架构，保持代码整洁是最基本的要求。",

            "### 前言\n\n安全问题永远是软件开发中的重中之重。\n\n最近做了项目的安全审计，整理了一些常见的安全风险和防范措施。\n\n### OWASP Top 10\n\n1. **注入攻击（Injection）**：使用参数化查询防止 SQL 注入\n2. **身份认证失效**：使用强密码策略和多因素认证\n3. **敏感数据泄露**：加密存储密码，HTTPS 传输\n4. **XML 外部实体（XXE）**：禁用外部实体解析\n5. **访问控制失效**：实施最小权限原则\n\n### 开发中的安全实践\n\n- 所有输入都要验证和清理\n- 使用安全的密码哈希算法（bcrypt）\n- 定期更新依赖库版本\n- 不在代码中硬编码密钥\n- 配置 CORS 白名单\n\n### 工具推荐\n\n- OWASP Dependency Check：依赖安全扫描\n- SonarQube：代码质量与安全分析\n- Snyk：开源漏洞检测\n\n安全不是一次性的工作，需要贯穿整个开发生命周期。",

            "### 背景\n\nDevOps 文化在团队中越来越受到重视。\n\n我们团队最近从手动部署迁移到了自动化 CI/CD 流程，效率提升非常明显。\n\n### CI/CD 流水线设计\n\n**持续集成（CI）：**\n1. 代码提交触发自动构建\n2. 运行单元测试和代码检查\n3. 生成构建产物\n\n**持续交付（CD）：**\n1. 自动部署到测试环境\n2. 运行集成测试\n3. 审批后部署到生产环境\n\n### 工具链\n\n- **代码托管**：GitHub / GitLab\n- **CI/CD**：GitHub Actions / Jenkins\n- **容器化**：Docker\n- **编排**：Kubernetes\n- **监控**：Prometheus + Grafana\n\n### 实践效果\n\n部署频率从每周 1 次提升到每天多次，回滚时间从小时级降到分钟级。\n\n自动化是提升效率的关键。如果你的团队还在手动部署，强烈建议尝试 CI/CD。",

            "### 读书笔记\n\n最近读了一本关于系统设计的好书，收获很大。\n\n### 核心观点\n\n1. **从需求出发**：好的设计来源于对需求的深刻理解\n2. **权衡取舍**：没有完美的方案，只有合适的折中\n3. **渐进式演进**：不要试图一步到位，小步迭代\n\n### 印象深刻的设计案例\n\n书中用了很多大公司的真实案例来说明设计决策背后的思考。\n\n比如 Twitter 从单体到微服务的演进路径，面对海量用户的扩展策略，这些实战经验比纯理论更有价值。\n\n### 我的感悟\n\n- 架构设计要平衡当前需求和未来扩展\n- 关注非功能性需求（性能、可用性、安全性）\n- 好的架构是演化出来的，不是设计出来的\n\n### 推荐理由\n\n这本书不仅讲了技术，更重要的是讲了设计思维。适合有 3 年以上经验的开发者阅读。",

            "🎵 **心情随笔**\n\n写代码累了，今天想聊聊工作之外的事情。\n\n## 程序员的业余生活\n\n很多人以为程序员的生活就是对着屏幕敲代码，其实我们也有丰富的业余爱好。\n\n我身边有：\n- 喜欢摄影的架构师\n- 热爱烹饪的前端工程师\n- 周末爬山的数据科学家\n- 业余乐队的后端开发\n\n## 为什么要培养业余爱好\n\n1. 缓解工作压力\n2. 拓展社交圈\n3. 激发创造力\n4. 保持生活平衡\n\n## 我的推荐\n\n如果你还没找到合适的爱好，可以试试：\n- 阅读非技术书籍\n- 学习一门乐器\n- 尝试手工制作\n- 参加户外运动\n\n生活不只是代码，还有诗和远方。\n\n你有什么特别的爱好吗？分享一下吧！",

            "### 优化心得\n\n最近花了大量时间优化系统的性能，这里总结一些实用的经验。\n\n### 优化思路\n\n1. **测量先行**：不要猜测性能瓶颈，用数据说话\n2. **80/20 原则**：优化最影响性能的 20% 代码\n3. **缓存为王**：合理使用缓存是性价比最高的优化手段\n\n### 数据库优化\n\n- 分析慢查询，添加合适的索引\n- 避免在循环中进行数据库操作\n- 使用批量操作代替逐条操作\n- 合理使用连接池\n\n### 应用层优化\n\n- 使用异步处理非关键路径\n- 合理设置超时时间\n- 接口数据按需返回，避免过度查询\n- 前端做防抖和节流\n\n### 效果\n\n经过一系列优化后，首页加载时间从 3s 降到 800ms，P99 延迟从 2s 降到 200ms。\n\n性能优化永无止境，但要在投入产出之间找到平衡。",

            "先问大家一个问题：你有多久没有整理过自己的代码了？\n\n## 为什么要定期整理代码\n\n代码就像房间，不定期整理就会越来越乱。技术债务积压过多，最终会影响整个项目的健康。\n\n## 从哪些方面入手\n\n1. **删除无用代码**：注释掉的代码、不再使用的功能\n2. **统一代码风格**：使用工具自动格式化\n3. **重构复杂方法**：拆分过长的方法\n4. **优化导入**：清理未使用的 import\n\n## 工具辅助\n\n- ESLint / Checkstyle：代码规范检查\n- SonarQube：代码质量分析\n- IDE 重构功能：安全地重构代码\n\n## 整理带来的好处\n\n- 代码可读性大幅提升\n- Bug 更容易被发现\n- 新成员上手更快\n- 开发效率提高\n\n建议每个迭代结束前留出一些时间做代码整理。",

            "分享一套我用了很久的高效工作流程。\n\n## 上午（深度工作时间）\n\n- 9:00-9:30 查看邮件和消息，规划当天任务\n- 9:30-12:00 专注编码，关闭通知\n\n## 下午（协作与学习）\n\n- 14:00-15:00 代码 Review\n- 15:00-17:00 开会、协作、处理紧急问题\n- 17:00-18:00 学习新技术或写文档\n\n## 高效秘诀\n\n1. **番茄工作法**：25 分钟专注 + 5 分钟休息\n2. **重要-紧急矩阵**：先做重要且紧急的事\n3. **批量处理**：集中处理邮件和消息，不要随时响应\n4. **保持桌面整洁**：物理和虚拟桌面都保持清爽\n\n## 使用工具\n\n- Todoist / Microsoft To Do：任务管理\n- Notion：知识管理\n- RescueTime：时间追踪\n\n找到适合自己的节奏最重要，不要盲目照搬别人的方法。",

            "### 引言\n\n有了 AI 辅助编程工具后，开发效率确实提高了很多。但写单元测试这件看似枯燥的事情，依然需要开发者亲自操刀。\n\n### 为什么要写测试\n\n1. 保证代码质量\n2. 防止回归 bug\n3. 作为代码的文档\n4. 提升重构信心\n\n### 测试金字塔\n\n- **单元测试**：占比最大，快速执行\n- **集成测试**：验证模块间协作\n- **端到端测试**：验证完整业务流程\n\n### 写测试的技巧\n\n1. 用 Given-When-Then 模式组织测试\n2. 一个测试只验证一个行为\n3. 测试应该独立，不依赖执行顺序\n4. 给测试起有意义的名字\n\n### 常见误区\n\n- 追求 100% 覆盖率（质量比覆盖率重要）\n- 测试实现细节而非行为\n- 测试之间共享可变状态\n\n好的测试是投资，不是成本。",

            "### 概述\n\n领域驱动设计（DDD）在最近几年越来越受到关注。\n\n我在一个实际项目中尝试了 DDD 的一些核心概念，分享一下心得体会。\n\n### 核心概念\n\n**实体（Entity）**：有唯一标识的对象\n**值对象（Value Object）**：没有标识，通过属性来定义\n**聚合（Aggregate）**：一组相关对象的集合\n**领域服务（Domain Service）**：不适合放在实体中的业务逻辑\n**仓储（Repository）**：聚合的持久化接口\n\n### 实际应用\n\n在我们的订单系统中：\n- Order 是聚合根\n- OrderItem 是实体\n- Money 是值对象\n- OrderRepository 负责持久化\n\n### DDD 的好处\n\n- 代码结构与业务概念对齐\n- 更容易理解和维护\n- 适合复杂业务场景\n\n### DDD 的挑战\n\n- 学习曲线陡峭\n- 需要深入理解业务\n- 简单场景可能过度设计\n\n不是所有项目都适合 DDD，简单 CRUD 应用不需要。",

            "### 新兴趋势\n\n2025 年已经过半，让我们回顾一下今年技术圈最有影响力的几个变化。\n\n### AI 技术全面渗透\n\nAI 不再只是一个 buzzword，已经深入到了开发的各个环节：\n- 代码补全和生成\n- 自动化测试\n- 智能运维\n- 自然语言交互\n\n### 平台工程兴起\n\n越来越多的公司建立内部开发者平台（IDP），为开发者提供自助服务能力。\n\n### WebAssembly 持续发展\n\nWASM 在浏览器之外的应用场景越来越多，尤其是在边缘计算和插件系统中。\n\n### Rust 进入主流\n\nRust 在系统编程领域的应用越来越广，甚至开始渗透到 Web 开发领域。\n\n### 总结\n\n技术发展日新月异，保持学习是最好的应对方式。\n\n你觉得今年最值得关注的技术趋势是什么？",

            "### 经验分享\n\n做了 5 年以上的 CRUD 开发，总结了一套快速开发模板。\n\n### 标准 CRUD 模板\n\n**Controller 层**\n```java\n@GetMapping(\"/\")\npublic PageResponse<DTO> list(QueryRequest query) { ... }\n\n@GetMapping(\"/{id}/\")\npublic DTO detail(@PathVariable Long id) { ... }\n\n@PostMapping(\"/\")\npublic DTO create(@Valid @RequestBody CreateRequest req) { ... }\n\n@PutMapping(\"/{id}/\")\npublic DTO update(@PathVariable Long id, @Valid @RequestBody UpdateRequest req) { ... }\n\n@DeleteMapping(\"/{id}/\")\npublic void delete(@PathVariable Long id) { ... }\n```\n\n### 提高效率的技巧\n\n1. 使用代码生成器生成基础代码\n2. 抽取公共的 BaseService\n3. 定义统一的异常处理\n4. 使用 MapStruct 做对象转换\n\n### 注意事项\n\nCRUD 开发看似简单，但要注意：\n- 参数校验不能省略\n- 权限控制要做充分\n- 日志记录要完善\n- 接口文档要保持最新",

            "技术选型时经常会陷入\"哪个更好\"的争论中。其实很多比较脱离了具体场景是没有意义的。\n\n## 比较框架\n\n| 框架 | 适合场景 | 不适合场景 |\n|------|----------|------------|\n| Spring Boot | 企业级应用 | 简单脚本 |\n| Flask | 快速原型 | 大型项目 |\n| Express | API 服务器 | CPU 密集型 |\n\n## 选择标准\n\n不是看谁热度高，而是看：\n1. 你的团队熟悉什么\n2. 项目规模和要求\n3. 社区和生态支持\n4. 长期维护成本\n\n## 我的建议\n\n- 不要追新技术（除非有充分理由）\n- 同一公司尽量统一技术栈\n- 做好封装，方便未来替换\n\n技术选型没有银弹，只有最适合的选择。\n\n你最近做了什么技术选型？是什么让你做出最终决定的？",

            "### 小技巧系列\n\n分享几个我每天都在用的编程小技巧，可能有些你已经知道，但总有一两个会有帮助。\n\n### 技巧一：活用 IDE 的 Live Template\n\n把常用的代码模板保存为快捷键，比如输入 `psvm` 生成 main 方法，输入 `sout` 生成打印语句。\n\n你还可以自定义模板，比如输入 `test` 生成完整的 JUnit 测试类骨架。\n\n### 技巧二：使用多光标编辑\n\n按住 Alt 键拖动鼠标可以创建多个光标，同时编辑多处代码。\n\n对于重命名变量、批量添加注释等操作非常高效。\n\n### 技巧三：Git 别名\n\n在 .gitconfig 中配置常用命令的别名：\n```\n[alias]\n  co = checkout\n  br = branch\n  ci = commit\n  st = status\n```\n\n### 技巧四：代码片段工具\n\n使用 Espanso 或 aText 等文本扩展工具，输入简短触发词自动展开为完整代码块。\n\n这些技巧看起来简单，但日积月累能节省大量时间。",

            "开源社区是技术发展的重要推动力。\n\n### 为什么要参与开源\n\n1. 提升技术能力\n2. 建立个人品牌\n3. 扩展人脉网络\n4. 为社区做贡献\n\n### 如何开始\n\n**第一步：找到合适的项目**\n关注你日常使用的开源项目，从熟悉的领域开始。\n\n**第二步：从简单任务开始**\n很多项目会标记 `good first issue`，这些都是适合新手的任务。\n\n**第三步：阅读贡献指南**\n每个项目都有自己的 CONTRIBUTING.md，仔细阅读后再提交代码。\n\n**第四步：提交第一个 PR**\n从小改动开始，比如修复文档错误、添加测试用例。\n\n### 注意事项\n\n- 尊重项目维护者的时间\n- 保持友善和专业的沟通\n- 不要为了 hacktoberfest 等活动的 T-shirt 而随意提交\n\n参与开源是一种很好的学习方式，推荐每个开发者都试试。",

            "### 学习路径\n\n经常有朋友问我如何入门编程，或者如何转到某个方向。\n\n这里整理了一份我认为比较合理的学习路径。\n\n### 基础阶段（0-3 个月）\n\n- HTML/CSS/JavaScript 基础\n- 至少掌握一门后端语言\n- 了解数据库基本操作\n- 会使用 Git\n\n### 进阶阶段（3-12 个月）\n\n- 深入理解框架原理\n- 学习设计模式和最佳实践\n- 参与实际项目开发\n- 开始阅读技术书籍和源码\n\n### 高级阶段（1-3 年）\n\n- 系统设计和架构能力\n- 性能分析和优化\n- 团队协作和项目管理\n- 培养技术影响力\n\n### 持续学习\n\n技术变化很快，保持学习的习惯比掌握某个具体技术更重要。\n\n推荐一些学习资源：官方文档、技术书籍、高质量的博客和视频教程。",

            "### 故障复盘\n\n上周线上出现了一次事故，幸好及时处理没有造成太大影响。\n\n今天做了一次完整的故障复盘，把过程和教训记录下来。\n\n### 故障时间线\n\n- 14:30 收到告警，部分接口响应超时\n- 14:35 确认数据库连接数异常升高\n- 14:40 发现新上线的代码中存在慢查询\n- 14:45 紧急回滚版本\n- 14:50 服务恢复正常\n\n### 根本原因\n\n新功能中有一个查询没有走索引，在数据量大的情况下导致全表扫描，阻塞了其他查询。\n\n### 改进措施\n\n1. Code Review 增加性能检查项\n2. 上线前进行压测\n3. 完善监控告警\n4. 建立慢查询自动预警机制\n\n### 总结\n\n每次事故都是一次学习机会。重要的是诚实地面对问题，系统地改进流程。\n\n你们团队是怎么做故障复盘的？",

            "### 写作初衷\n\n写这篇文章的目的很简单——帮助那些刚开始接触微服务的同学少走一些弯路。\n\n### 微服务是什么\n\n简单来说，微服务就是把一个大的应用拆分成多个小的、独立的服务。每个服务负责一个特定的业务功能，可以独立开发、部署和扩展。\n\n### 微服务的好处\n\n- 独立部署，发布更快\n- 技术栈灵活，不同服务可以用不同语言\n- 故障隔离，一个服务出问题不影响其他\n- 团队自治，小团队负责各自的服务\n\n### 微服务的挑战\n\n- 分布式系统的复杂性\n- 服务间通信的开销\n- 数据一致性问题\n- 运维和监控的难度\n\n### 要不要上微服务\n\n不是所有项目都需要微服务。如果你的项目：\n- 团队人数少于 10 人\n- 业务逻辑相对简单\n- 没有高可用需求\n\n那么单体架构可能是更好的选择。\n\n微服务是手段不是目的。",

            "### 引言\n\nAPI 设计是后端开发的基本功。一个好的 API 设计能让前端同学少很多抱怨。\n\n### URL 设计原则\n\n- 使用名词而不是动词：`/users` 而不是 `/getUsers`\n- 使用复数形式：`/users/123` 而不是 `/user/123`\n- 层级关系体现资源嵌套：`/users/123/orders`\n\n### HTTP 方法语义\n\n- GET：获取资源\n- POST：创建资源\n- PUT：完整更新\n- PATCH：部分更新\n- DELETE：删除\n\n### 状态码使用\n\n- 200：成功\n- 201：创建成功\n- 400：请求参数错误\n- 401：未认证\n- 403：无权限\n- 404：资源不存在\n- 500：服务器错误\n\n### 响应格式\n\n统一的响应格式非常重要：\n```json\n{\n  \"code\": 200,\n  \"message\": \"success\",\n  \"data\": { ... }\n}\n```\n\n好的 API 设计能大大提升团队协作效率。",

            "被问了很多次面试相关的问题，整理了一些心得。\n\n### 技术面试准备\n\n**算法题**\n- 每天刷 2-3 道题保持手感\n- 重点掌握数组、链表、树、动态规划\n- 不只求做出来，要能讲清楚思路\n\n**系统设计**\n- 了解常见的系统架构模式\n- 练习 \"设计一个短链接系统\" 这类经典题目\n- 关注 trade-off 的思考过程\n\n**项目经验**\n- 能清晰描述项目的技术架构\n- 准备几个\"解决了什么难题\"的案例\n- 展示你的技术深度和广度\n\n### 软技能\n\n- 沟通表达能力\n- 问题解决思路\n- 学习能力和热情\n- 团队协作意识\n\n### 面试心态\n\n- 把面试当成学习交流的机会\n- 遇到不会的问题诚实说不会\n- 面试结束及时复盘总结\n\n面试是双向选择，不仅是公司在面试你，你也在了解公司。",

            "### 避坑指南\n\n在多年的开发经历中踩过不少坑，挑几个有代表性的分享出来。\n\n### 坑一：时区问题\n\n服务器和数据库时区不一致，导致时间数据混乱。\n\n**解决方案**：统一使用 UTC 存储，展示时转换为用户时区。\n\n### 坑二：并发更新\n\n多个请求同时修改同一条数据，后提交的覆盖了先提交的。\n\n**解决方案**：使用乐观锁（version 字段）或悲观锁。\n\n### 坑三：内存泄漏\n\n静态集合类持有对象引用，导致 GC 无法回收。\n\n**解决方案**：谨慎使用静态集合，及时清理无用引用。\n\n### 坑四：大事务\n\n长时间持有数据库锁，影响其他请求。\n\n**解决方案**：尽量缩小事务范围，将非数据库操作移出事务。\n\n### 避坑心得\n\n- 多关注线上日志和监控\n- 定期做代码审查\n- 新人入职时做好知识传承\n\n每个坑都是一次成长的机会。你踩过哪些难忘的坑？",

            "### 关于加班\n\n这个话题有点敏感，但还是想说一说。\n\n### 加班的原因\n\n- 项目紧急上线\n- 线上故障处理\n- 需求评估不准\n- 团队文化问题\n\n### 加班的影响\n\n短期加班可以理解，但长期 996 会带来：\n- 身体健康受损\n- 创造力下降\n- 代码质量变差\n- 生活失去平衡\n\n### 如何减少无效加班\n\n1. 提高白天的工作效率\n2. 合理评估工作量\n3. 及时沟通风险和问题\n4. 学会说\"不\"\n\n### 一些想法\n\n加班不是努力的证明。高效的 8 小时比疲劳的 12 小时产出更高。\n\n希望行业能越来越健康。\n\n（仅代表个人观点，欢迎理性讨论）",

            "### 导读\n\n从单体到微服务的迁移是一个复杂的过程，需要充分的准备和周密的计划。\n\n### 迁移策略\n\n**1. 绞杀者模式（Strangler Fig）**\n\n逐步用新服务替换旧功能，最终完全替代旧系统。这是最安全的迁移方式。\n\n**2. 并行运行**\n\n新旧系统同时运行一段时间，确认新系统稳定后再关闭旧系统。\n\n**3. 数据迁移**\n\n- 先迁移数据结构\n- 双写保证数据一致\n- 验证后再关闭旧数据源\n\n### 常见问题\n\n- 数据库拆分：如何拆分现有的单体数据库\n- 事务处理：分布式事务如何保证一致性\n- 服务通信：同步还是异步\n\n### 我的建议\n\n1. 不要为了微服务而微服务\n2. 从非核心模块开始迁移\n3. 做好监控和回滚方案\n4. 团队要有足够的准备\n\n迁移过程中最重要的是保证业务不受影响。",

            "### 引子\n\n低代码平台这两年很火，不少人担心它会取代传统开发。\n\n理性分析一下。\n\n### 低代码的优势\n\n- 快速搭建简单应用\n- 降低开发门槛\n- 适合标准化场景\n- 减少重复劳动\n\n### 低代码的局限\n\n- 复杂逻辑难以实现\n- 性能和扩展性受限\n- 平台锁定风险\n- 定制化能力不足\n\n### 哪些场景适合低代码\n\n✅ 内部管理工具\n✅ 简单表单和数据展示\n✅ 审批工作流\n✅ 快速原型验证\n\n### 哪些场景不适合\n\n❌ 复杂的业务逻辑\n❌ 高并发高性能要求\n❌ 需要深度定制的产品\n❌ 核心业务系统\n\n### 总结\n\n低代码是工具，不是替代品。它会取代一部分简单开发工作，但不会取代专业开发者。\n\n对开发者来说，与其担心被替代，不如提升解决复杂问题的能力。",

            "又是一年毕业季，很多同学即将踏入职场。\n\n## 技术准备\n\n- 扎实的计算机基础（数据结构、算法、网络、操作系统）\n- 至少精通一门编程语言\n- 有 1-2 个完整的项目经验\n- 了解常用的开发工具和流程\n\n## 简历建议\n\n- 突出项目经验，描述你做了什么、用了什么技术\n- 不要罗列所有技术名词，写你真正掌握了的\n- 量化成果（如\"优化后性能提升 50%\"）\n- 保持一页以内\n\n## 面试准备\n\n- 复习基础知识\n- 准备项目介绍（STAR 法则：情境、任务、行动、结果）\n- 了解目标公司的技术栈\n- 准备几个有深度的问题反问面试官\n\n## 心态调整\n\n- 第一份工作不一定要完美\n- 关注成长空间大于薪资\n- 不要和别人比较\n- 保持学习的心态\n\n祝大家都能找到满意的工作！",

            "分享我在日常开发中使用的一些命令行工具，都是免费开源的。\n\n## 文件处理\n\n- `jq`：命令行 JSON 处理工具\n- `fzf`：模糊搜索工具\n- `bat`：带语法高亮的 cat 替代品\n- `fd`：更快的 find 替代品\n\n## 系统监控\n\n- `htop`：交互式进程查看器\n- `ncdu`：磁盘使用分析\n- `bandwhich`：网络带宽监控\n\n## 开发辅助\n\n- `tldr`：简化的命令手册\n- `httpie`：人性化的 HTTP 客户端\n- `lazygit`：Git 终端 UI\n\n## 安装\n\n大多数工具都可以通过包管理器安装：\n```bash\n# macOS\nbrew install jq fzf bat\n\n# Ubuntu\nsudo apt install jq\n```\n\n这些小工具虽然不起眼，但用熟了能大大提高工作效率。",

            "### 安全第一\n\n最近发现很多开发者在处理密码时存在不规范的做法。\n\n### 密码存储\n\n**❌ 错误做法**\n- 明文存储密码\n- 使用 MD5 或 SHA1 哈希\n- 不加盐的哈希\n\n**✅ 正确做法**\n- 使用 bcrypt、scrypt 或 Argon2\n- 自动包含盐值\n- 可以调节计算强度\n\n### 密码传输\n\n- 必须使用 HTTPS\n- 前端做一次哈希不完全可靠，后端仍需安全存储\n- 不要在 URL 中传递密码\n\n### 密码策略\n\n- 最小长度 8 位以上\n- 支持但不强制复杂字符\n- 检查是否在已知泄露密码库中\n- 提供密码强度指示器\n\n### 其他认证方式\n\n- 多因素认证（MFA）\n- 生物识别\n- 社交账号登录\n\n安全是永无止境的话题。如果你正在处理用户密码，请务必遵循最佳实践。",

            "### 前言\n\n今天不聊具体的技术，聊聊可迁移的思维能力。\n\n### 什么是解决问题的能力\n\n面对一个未知的问题，能够：\n1. 清晰地定义问题\n2. 拆解为可解决的子问题\n3. 提出假设并验证\n4. 找到最优解决方案\n\n### 如何培养\n\n- 遇到问题先自己思考，不要马上查答案\n- 多问\"为什么\"，直到找到根本原因\n- 总结归纳不同类型问题的解决模式\n- 做 side project，在实践中锻炼\n\n### 案例：一个线上 Bug 的排查过程\n\n用户反馈登录失败 → 检查日志发现空指针异常 → 追踪到数据库查询返回 null → 发现是缓存中的数据与数据库不一致 → 修复缓存更新逻辑。\n\n每一步都在缩小问题范围，最终定位到根本原因。\n\n### 总结\n\n具体的技术会过时，但解决问题的能力永远不会。\n\n多思考，多总结，多实践。",

            "### 背景\n\n工作中经常需要给非技术人员解释技术概念。\n\n### 为什么沟通很重要\n\n- 让产品经理理解技术限制\n- 让设计师知道实现难度\n- 让老板认可技术投入的价值\n- 让团队协同更顺畅\n\n### 沟通技巧\n\n1. **使用类比**：把复杂概念类比为日常事物\n2. **画图说明**：可视化比纯文字更容易理解\n3. **关注对方需求**：说对方关心的，而不是你想说的\n4. **避免专业术语**：除非对方也是技术人员\n\n### 实战案例\n\n向产品经理解释\"微服务\"可以说：\n\n\"就像一个大公司拆分成几个小团队，每个团队独立运作，有自己的节奏和资源，互相通过邮件（API）沟通。\"\n\n### 总结\n\n技术能力决定下限，沟通能力决定上限。\n\n花时间提升沟通能力绝对是值得的投资。",

            "作为开发者，配置一个高效舒适的开发环境非常重要。\n\n## 操作系统\n\n- **macOS**：对开发者友好，生态完善\n- **Linux**：服务器标配，自由度最高\n- **Windows**：WSL 2 让开发体验大幅提升\n\n## 终端\n\n- iTerm2 + zsh + oh-my-zsh（macOS）\n- Windows Terminal + PowerShell（Windows）\n\n## 编辑器\n\n- **VS Code**：轻量快速，插件丰富\n- **IntelliJ IDEA**：Java 开发首选\n- **Vim/Neovim**：键盘流玩家的最爱\n\n## 必备插件\n\n- Git 集成\n- 语法高亮\n- 代码格式化\n- AI 辅助（GitHub Copilot）\n\n## 字体\n\n推荐等宽编程字体：\n- JetBrains Mono\n- Fira Code（支持连字）\n- Cascadia Code\n\n好的开发环境让人心情愉悦，开发效率也会更高。\n\n你用什么编辑器和字体？",

            "### 远程工作\n\n远程工作已经成为越来越多开发者的常态。\n\n### 远程工作的好处\n\n- 省去通勤时间\n- 更灵活的工作安排\n- 可以选择自己喜欢的城市生活\n\n### 远程工作的挑战\n\n- 缺少面对面的社交互动\n- 工作和生活的边界模糊\n- 需要更强的自驱力\n- 跨时区协作的沟通成本\n\n### 高效远程工作技巧\n\n1. 保持规律的作息\n2. 打造专门的工作空间\n3. 每天写工作日志\n4. 定期和团队进行视频会议\n5. 主动沟通，减少信息不对称\n\n### 工具推荐\n\n- Slack / 飞书：即时沟通\n- Zoom / 腾讯会议：视频会议\n- Notion / Confluence：文档协作\n- Linear / Jira：项目管理\n\n远程工作不是适合所有人，但如果你能驾驭它，生活会更加自由。",

            "### 我的工具清单\n\n整理了一下我日常开发中使用频率最高的软件。\n\n### 开发工具\n\n- **IDE**：IntelliJ IDEA Ultimate（Java）+ VS Code（前端/脚本）\n- **数据库**：DataGrip / DBeaver\n- **API 测试**：Postman / HTTPie\n- **终端**：Windows Terminal\n\n### 效率工具\n\n- **笔记**：Notion\n- **待办**：Microsoft To Do\n- **截图**：Snipaste\n- **剪贴板**：Ditto\n\n### 浏览器\n\n- **主力**：Chrome\n- **开发**：Chrome DevTools\n- **插件**：JSON Viewer、React DevTools、Vue DevTools\n\n### 其他\n\n- **版本管理**：Git + GitHub Desktop\n- **终端记录**：asciinema\n- **代码片段**：GitHub Gist\n\n工具不求多，找到适合自己的就是最好的。",

            "### 技术写作\n\n写了几年博客，分享一些技术写作的心得。\n\n### 为什么写\n\n- 整理思路，加深理解\n- 建立个人品牌\n- 帮助他人\n- 记录成长轨迹\n\n### 怎么写\n\n1. **选题**：写你最近学到的东西，印象深刻的项目经验\n2. **结构**：引言 → 正文 → 总结，逻辑清晰\n3. **代码示例**：提供可运行的代码，而不是伪代码\n4. **排版**：善用标题、列表、加粗突出重点\n\n### 在哪写\n\n- 个人博客\n- 掘金 / 知乎\n- Medium / Dev.to\n- 公司内部技术博客\n\n### 坚持写\n\n写作是长期投资。我的建议是：\n- 设定合理的目标（如每月一篇）\n- 不要追求完美，先写出第一版\n- 多读优秀的技术文章\n\n开始写比什么都重要。你觉得最难的是什么？",

            "### 反馈文化\n\n好的 Code Review 是团队成长的重要方式。\n\n### Review 什么\n\n- 逻辑是否正确\n- 是否有潜在的性能问题\n- 安全性是否考虑充分\n- 代码是否可读可维护\n- 测试是否充分\n\n### 如何给出好的反馈\n\n- 对事不对人\n- 具体指出问题和建议\n- 区分\"必须修改\"和\"建议优化\"\n- 也指出写得好的地方\n\n### 如何接受反馈\n\n- 不要把 Review 当成批评\n- 认真考虑每条建议\n- 不确定时可以讨论\n- 感谢 Review 者的时间\n\n### 建立 Review 规范\n\n- PR 不要太大（建议 400 行以内）\n- 提供清晰的描述\n- 24 小时内完成 Review\n\n好的 Review 文化让整个团队变得更好。",

            "最近在搭建 CI/CD 流水线，记录下整个过程。\n\n## 为什么需要 CI/CD\n\n- 自动化测试，减少人为失误\n- 快速部署，缩短发布周期\n- 标准化流程，可重复执行\n\n## 工具选择\n\n最终选择了 GitHub Actions，原因：\n- 与 GitHub 深度集成\n- 社区 Action 丰富\n- 免费额度足够用\n\n## 流水线配置\n\n```yaml\nname: CI/CD\non:\n  push:\n    branches: [main]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - name: Build\n        run: mvn package\n      - name: Test\n        run: mvn test\n```\n\n## 踩过的坑\n\n1. 环境变量配置要仔细检查\n2. 注意敏感信息不要提交到仓库\n3. 构建缓存可以大大加快速度\n\nCI/CD 是现代化开发流程的标配。你用的是哪种 CI/CD 工具？",

            "### 前言\n\n多线程编程是进阶开发者的必备技能。\n\n### 基础概念\n\n- **进程**：资源分配的最小单位\n- **线程**：CPU 调度的最小单位\n- **并发**：多个任务交替执行\n- **并行**：多个任务同时执行\n\n### 线程安全问题\n\n1. **竞态条件**：多个线程同时修改共享数据\n2. **死锁**：线程相互等待对方释放资源\n3. **可见性**：一个线程的修改对另一个线程不可见\n\n### 解决方案\n\n- synchronized 关键字\n- Lock 接口\n- 原子类（AtomicInteger 等）\n- 线程安全集合（ConcurrentHashMap 等）\n- volatile 关键字\n\n### 实践建议\n\n1. 优先使用并发工具类而不是原始锁\n2. 使用线程池管理线程\n3. 避免在锁内做耗时操作\n4. 尽量使用不可变对象\n\n多线程编程要谨慎，出问题很难调试。",

            "这是一个很常见的问题，今天好好聊聊。\n\n## 内连接（INNER JOIN）\n\n只返回两个表中匹配的行。\n\n```sql\nSELECT * FROM users\nINNER JOIN orders ON users.id = orders.user_id;\n```\n\n## 左连接（LEFT JOIN）\n\n返回左表所有行，右表没有匹配的填 NULL。\n\n```sql\nSELECT * FROM users\nLEFT JOIN orders ON users.id = orders.user_id;\n```\n\n## 使用建议\n\n- 大部分情况下用 INNER JOIN 就够了\n- LEFT JOIN 适合\"主表数据必须全部展示\"的场景\n- 注意笛卡尔积：多表连接时条件要写全\n\n## 性能比较\n\nINNER JOIN 通常比 LEFT JOIN 快，因为不需要处理 NULL 值。\n\n但性能差异在大多数场景下可以忽略，更重要的是用对。",

            "### 关于焦虑\n\n最近跟几个朋友聊天，发现大家都有不同程度的职业焦虑。\n\n### 焦虑的来源\n\n- 技术更新太快\n- 担心被 AI 替代\n- 35 岁危机\n- 和同龄人的比较\n\n### 我的想法\n\n技术行业确实变化快，但核心能力是不变的：\n- 解决问题的能力\n- 学习的能力\n- 沟通协作的能力\n- 业务理解的能力\n\nAI 是工具，会用 AI 的开发者更有竞争力，而不是被替代。\n\n### 几点建议\n\n1. 专注当下，做好手头的工作\n2. 持续学习，但不要贪多\n3. 培养技术之外的兴趣\n4. 多和人交流，不要封闭自己\n\n焦虑是正常的，重要的是如何应对它。\n\n一起加油 💪",

            "### 为什么需要这些\n\n无论你是前端、后端还是全栈开发者，Linux 基础都是必备技能。\n\n### 文件操作\n\n```bash\nls -la          # 列出所有文件\ncd /path        # 切换目录\ncp src dest     # 复制\nmv src dest     # 移动/重命名\nrm -rf dir      # 删除（慎用！）\n```\n\n### 文本处理\n\n```bash\ngrep \"pattern\" file    # 搜索文本\ncat file | wc -l       # 统计行数\nhead -n 10 file        # 查看前 10 行\ntail -f log            # 实时查看日志\n```\n\n### 系统信息\n\n```bash\ntop             # 进程监控\ndf -h           # 磁盘使用\nfree -h         # 内存使用\nps aux          # 进程列表\n```\n\n### 网络\n\n```bash\ncurl url        # HTTP 请求\nping host       # 连通性测试\nnetstat -an     # 网络连接\n```\n\n这些命令覆盖了 80% 的日常使用场景。",

            "忙忙碌碌写了这么多年代码，偶尔停下来想一想也挺好的。\n\n## 关于成长\n\n技术成长不是线性的。有时候感觉原地踏步，但其实在积累；有时候突然开窍，进步飞快。\n\n重要的是不要放弃。\n\n## 关于选择\n\n选择比努力重要，但选择的能力来自于努力。\n\n多尝试不同的方向，找到自己真正感兴趣的事情。\n\n## 关于健康\n\n- 保护眼睛：定时休息，用 20-20-20 法则\n- 保护颈椎：调整显示器高度\n- 保护手腕：使用人体工学键盘\n- 多运动：久坐是健康杀手\n\n## 关于生活\n\n代码不是全部。\n\n家人、朋友、爱好，这些才是支撑我们走下去的力量。\n\n## 最后\n\n写代码是一件很有成就感的事情。\n\n创造东西，解决问题，帮助他人。\n\n做好每一件小事，时间会给出答案。",

            "### 数据的重要性\n\n你的数据就是你的资产。无论是个人数据还是公司数据，备份永远不嫌多。\n\n### 备份策略\n\n**3-2-1 原则**\n- 至少保留 **3** 份数据副本\n- 使用 **2** 种不同的存储介质\n- **1** 份异地备份\n\n### 开发者需要备份什么\n\n- 代码（已有 Git，但别忘了 push）\n- 配置文件（dotfiles）\n- 数据库数据\n- 笔记和文档\n\n### 工具推荐\n\n- Time Machine（macOS）\n- rsync：增量备份\n- restic：加密备份\n- GitHub：代码备份\n\n### 自动化\n\n设置定时备份任务，不要让备份成为需要\"记得做\"的事情。\n\n我在一次硬盘故障后深刻理解了备份的重要性。你最近备份了吗？",

            "分享几个我在日常开发中使用的小工具，都是免费开源的。\n\n## 1. DevToys（Windows）\n\n开发者的瑞士军刀，集成了 JSON 格式化、Base64 编解码、正则测试、UUID 生成等工具。\n\n## 2. Oh My Zsh\n\n让你的终端变得强大又好看。丰富的插件和主题，用了就回不去。\n\n## 3. asdf\n\n统一管理多个编程语言的版本。告别 nvm、pyenv、rbenv 各自为政的局面。\n\n## 4. tig\n\nGit 的文本模式界面，查看提交历史和 diff 比命令行更直观。\n\n## 5. ngrok\n\n将本地服务暴露到公网，非常适合调试 webhook。\n\n这些工具都不大，但各自解决了一个痛点。你有什么好用的小工具推荐吗？"
    };
}
