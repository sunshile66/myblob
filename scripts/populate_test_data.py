#!/usr/bin/env python
"""
测试数据填充脚本
往数据库中添加模拟的测试数据
"""
import os
import sys
import django
from datetime import datetime, timedelta

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "myblob.settings")
django.setup()

from accounts.models import User, UserProfile, Follow
from blog.models import Category, Tag, Post
from comments.models import Comment
from interactions.models import PostLike, Favorite, Notification
from mediaapp.models import MediaAsset


def create_test_data():
    """
    创建完整的测试数据
    """
    print("=" * 50)
    print("开始创建测试数据...")
    print("=" * 50)

    # 创建用户
    print("\n--- 创建用户 ---")
    users_data = [
        {"username": "admin", "email": "admin@example.com", "nickname": "管理员", "is_superuser": True, "is_staff": True, "avatar": "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop", "bio": "热爱技术，热爱生活～", "location": "北京", "profession": "全栈开发工程师"},
        {"username": "zhangsan", "email": "zhangsan@example.com", "nickname": "张三", "avatar": "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop", "bio": "前端开发工程师，专注于 Vue 和 React 技术栈", "location": "上海", "profession": "前端开发", "wechat": "zhangsan_dev"},
        {"username": "lisi", "email": "lisi@example.com", "nickname": "李四", "avatar": "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop", "bio": "后端开发，Python 爱好者", "location": "广州", "profession": "后端开发", "company": "某某科技公司"},
        {"username": "wangwu", "email": "wangwu@example.com", "nickname": "王五", "avatar": "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop", "bio": "产品经理，关注用户体验", "location": "深圳", "profession": "产品经理"},
        {"username": "zhaoliu", "email": "zhaoliu@example.com", "nickname": "赵六", "avatar": "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop", "bio": "UI 设计师，追求极致的视觉体验", "location": "杭州", "profession": "UI 设计师"},
        {"username": "sunqi", "email": "sunqi@example.com", "nickname": "孙七", "avatar": "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop", "bio": "数据分析师，用数据说话", "location": "成都", "profession": "数据分析师"},
        {"username": "zhouba", "email": "zhouba@example.com", "nickname": "周八", "avatar": "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100&h=100&fit=crop", "bio": "运维工程师，保障系统稳定", "location": "武汉", "profession": "运维工程师"},
        {"username": "wujiu", "email": "wujiu@example.com", "nickname": "吴九", "avatar": "https://images.unsplash.com/photo-1480429370139-e0132c086e2a?w=100&h=100&fit=crop", "bio": "测试工程师，质量守护者", "location": "南京", "profession": "测试工程师"},
    ]

    users = []
    for user_data in users_data:
        user, created = User.objects.get_or_create(username=user_data["username"], defaults={k: v for k, v in user_data.items() if k not in ['bio', 'location', 'profession', 'wechat', 'company']})
        if created:
            user.set_password("test123456")
            user.save()
            profile_data = {k: v for k, v in user_data.items() if k in ['bio', 'location', 'profession', 'wechat', 'company']}
            UserProfile.objects.get_or_create(user=user, defaults=profile_data)
            print(f"✓ 创建用户: {user.username} ({user.nickname})")
        else:
            print(f"→ 用户已存在: {user.username}")
        users.append(user)

    # 创建分类
    print("\n--- 创建分类 ---")
    categories_data = [
        {"name": "技术", "slug": "tech", "description": "技术相关的文章", "sort": 1},
        {"name": "生活", "slug": "life", "description": "生活随笔", "sort": 2},
        {"name": "教程", "slug": "tutorial", "description": "编程教程", "sort": 3},
        {"name": "分享", "slug": "share", "description": "资源分享", "sort": 4},
        {"name": "美食", "slug": "food", "description": "美食探店", "sort": 5},
        {"name": "旅行", "slug": "travel", "description": "旅行日记", "sort": 6},
    ]

    categories = []
    for cat_data in categories_data:
        category, created = Category.objects.get_or_create(slug=cat_data["slug"], defaults=cat_data)
        if created:
            print(f"✓ 创建分类: {category.name}")
        else:
            print(f"→ 分类已存在: {category.name}")
        categories.append(category)

    # 创建标签
    print("\n--- 创建标签 ---")
    tags_data = [
        {"name": "Python", "slug": "python"},
        {"name": "Django", "slug": "django"},
        {"name": "Vue.js", "slug": "vue-js"},
        {"name": "JavaScript", "slug": "javascript"},
        {"name": "TypeScript", "slug": "typescript"},
        {"name": "React", "slug": "react"},
        {"name": "MySQL", "slug": "mysql"},
        {"name": "PostgreSQL", "slug": "postgresql"},
        {"name": "Redis", "slug": "redis"},
        {"name": "Docker", "slug": "docker"},
        {"name": "分享日常", "slug": "daily-share"},
        {"name": "生活记录", "slug": "life-record"},
        {"name": "美食探店", "slug": "food-review"},
        {"name": "吃货日常", "slug": "foodie-daily"},
        {"name": "旅行攻略", "slug": "travel-guide"},
        {"name": "旅游记录", "slug": "travel-diary"},
        {"name": "学习笔记", "slug": "study-notes"},
        {"name": "知识分享", "slug": "knowledge-share"},
    ]

    tags = []
    for tag_data in tags_data:
        tag, created = Tag.objects.get_or_create(slug=tag_data["slug"], defaults=tag_data)
        if created:
            print(f"✓ 创建标签: {tag.name}")
        else:
            print(f"→ 标签已存在: {tag.name}")
        tags.append(tag)

    # 创建文章
    print("\n--- 创建文章 ---")
    posts_data = [
        {
            "title": "Django 6.0 新特性介绍",
            "slug": "django-6-new-features",
            "summary": "本文详细介绍 Django 6.0 带来的新特性和改进，包括异步视图、性能优化、数据库改进等。",
            "content": """# Django 6.0 新特性介绍

## 概述

Django 6.0 带来了许多激动人心的新特性，让我们一起来看看！

## 主要新特性

### 1. 异步视图支持

现在可以直接使用异步函数作为视图：

```python
async def my_view(request):
    return JsonResponse({"message": "Hello async!"})
```

### 2. 性能优化

- 数据库查询优化
- 缓存改进
- 响应时间缩短

## 总结

Django 6.0 是一个值得升级的版本！""",
            "author": users[0],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 1256,
            "like_count": 42,
            "comment_count": 18,
            "cover": "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=600&h=400&fit=crop",
        },
        {
            "title": "Vue 3 组合式 API 完全指南",
            "slug": "vue3-composition-api-guide",
            "summary": "深入学习 Vue 3 的组合式 API，包括 ref、reactive、computed、watch 等核心概念的使用方法。",
            "content": """# Vue 3 组合式 API 完全指南

## 为什么需要组合式 API？

组合式 API 提供了更好的代码组织方式...

## 核心概念

### ref

```typescript
import { ref } from 'vue'

const count = ref(0)
```

### reactive

```typescript
import { reactive } from 'vue'

const state = reactive({
  name: 'Vue',
  version: 3
})
```

## 生命周期钩子

- onMounted
- onUpdated
- onUnmounted
""",
            "author": users[1],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 2341,
            "like_count": 89,
            "comment_count": 32,
            "cover": "https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=600&h=400&fit=crop",
        },
        {
            "title": "使用 Docker 部署 Django 应用",
            "slug": "docker-deploy-django",
            "summary": "从零开始学习如何使用 Docker 和 Docker Compose 部署 Django 应用程序。",
            "content": """# 使用 Docker 部署 Django 应用

## 准备工作

首先，确保安装了 Docker...

## Dockerfile

```dockerfile
FROM python:3.12
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
```

## Docker Compose

```yaml
version: '3.8'
services:
  web:
    build: .
    ports:
      - "8000:8000"
  db:
    image: postgres
```
""",
            "author": users[2],
            "category": categories[2],
            "status": "published",
            "post_type": "note",
            "view_count": 876,
            "like_count": 28,
            "comment_count": 12,
            "cover": "https://images.unsplash.com/photo-1605745341112-85968b19335b?w=600&h=400&fit=crop",
        },
        {
            "title": "我的 2024 年度总结",
            "slug": "my-2024-summary",
            "summary": "回顾过去一年的成长、收获和感悟，展望新的一年。",
            "content": """# 我的 2024 年度总结

## 技术成长

今年学习了很多新技术...

## 工作收获

项目经验增长...

## 生活感悟

健康很重要...

## 明年计划

继续加油！
""",
            "author": users[0],
            "category": categories[1],
            "status": "published",
            "post_type": "note",
            "view_count": 456,
            "like_count": 15,
            "comment_count": 8,
            "cover": "https://images.unsplash.com/photo-1488190211105-8b0e65b80b4e?w=600&h=400&fit=crop",
        },
        {
            "title": "Python 数据分析入门教程",
            "slug": "python-data-analysis-tutorial",
            "summary": "使用 Pandas、NumPy 和 Matplotlib 进行数据分析的入门教程。",
            "content": """# Python 数据分析入门教程

## 环境准备

```bash
pip install pandas numpy matplotlib
```

## Pandas 基础

```python
import pandas as pd

df = pd.read_csv('data.csv')
print(df.head())
```

## 数据可视化

```python
import matplotlib.pyplot as plt

plt.plot([1, 2, 3, 4])
plt.show()
```
""",
            "author": users[5],
            "category": categories[2],
            "status": "published",
            "post_type": "article",
            "view_count": 1567,
            "like_count": 67,
            "comment_count": 25,
            "cover": "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600&h=400&fit=crop",
        },
        {
            "title": "TypeScript 高级类型技巧",
            "slug": "typescript-advanced-types",
            "summary": "学习 TypeScript 的高级类型系统，包括泛型、条件类型、映射类型等。",
            "content": """# TypeScript 高级类型技巧

## 泛型

```typescript
function identity<T>(arg: T): T {
  return arg
}
```

## 条件类型

```typescript
type IsString<T> = T extends string ? true : false
```

## 映射类型

```typescript
type Readonly<T> = {
  readonly [P in keyof T]: T[P]
}
```
""",
            "author": users[1],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 987,
            "like_count": 34,
            "comment_count": 14,
            "cover": "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=600&h=400&fit=crop",
        },
        {
            "title": "美食探店｜这家店超好吃！",
            "slug": "food-review-awesome-restaurant",
            "summary": "今天发现了一家超级好吃的店！位置、推荐菜品、人均消费都给大家整理好了～",
            "content": """# 美食探店｜这家店超好吃！

今天发现了一家超级好吃的店！

📍位置：市中心商圈

🍜推荐菜品：
- 招牌牛肉面
- 糖醋排骨
- 清炒时蔬

💰人均消费：80元

强烈推荐大家来试试！

#美食探店 #吃货日常
""",
            "author": users[3],
            "category": categories[4],
            "status": "published",
            "post_type": "note",
            "view_count": 3241,
            "like_count": 156,
            "comment_count": 48,
            "cover": "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=600&h=400&fit=crop",
        },
        {
            "title": "旅行｜杭州周末游攻略",
            "slug": "travel-hangzhou-weekend-guide",
            "summary": "终于来到了心心念念的杭州！行程安排、拍照打卡点、实用Tips都整理好了～",
            "content": """# 旅行｜杭州周末游攻略

终于来到了心心念念的杭州！

📍行程安排：
- 周六：西湖游船 → 雷峰塔 → 河坊街
- 周日：灵隐寺 → 龙井茶园

📸拍照打卡点：
- 西湖断桥
- 雷峰塔夜景
- 茶园小道

💡实用Tips：
- 周末人多，建议早出发
- 做好防晒

#旅行攻略 #旅游记录
""",
            "author": users[4],
            "category": categories[5],
            "status": "published",
            "post_type": "note",
            "view_count": 4521,
            "like_count": 234,
            "comment_count": 67,
            "cover": "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600&h=400&fit=crop",
        },
        {
            "title": "学习笔记｜React Hooks 知识点整理",
            "slug": "study-notes-react-hooks",
            "summary": "今天学习了 React Hooks，整理一下重要的知识点～",
            "content": """# 学习笔记｜React Hooks 知识点整理

今天学习了这些内容，整理一下～

📖 useState：状态管理
📖 useEffect：副作用处理
📖 useContext：上下文使用
📖 useReducer：复杂状态管理
📖 useCallback：性能优化
📖 useMemo：性能优化

📝 总结：Hooks 让函数组件更强大！

#学习笔记 #知识分享
""",
            "author": users[1],
            "category": categories[2],
            "status": "published",
            "post_type": "note",
            "view_count": 1876,
            "like_count": 98,
            "comment_count": 31,
            "cover": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600&h=400&fit=crop",
        },
        {
            "title": "分享日常｜今天的咖啡时光",
            "slug": "daily-share-coffee-time",
            "summary": "今天下午在咖啡馆度过了美好的时光～",
            "content": """# 分享日常｜今天的咖啡时光

今天下午在咖啡馆度过了美好的时光～

阳光正好，咖啡很香，心情很美丽！

#分享日常 #生活记录
""",
            "author": users[3],
            "category": categories[1],
            "status": "published",
            "post_type": "note",
            "view_count": 543,
            "like_count": 28,
            "comment_count": 9,
            "cover": "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=600&h=400&fit=crop",
        },
        {
            "title": "前端性能优化实战指南",
            "slug": "frontend-performance-optimization",
            "summary": "全面介绍前端性能优化的各种技巧和最佳实践。",
            "content": """# 前端性能优化实战指南

## 网络优化

- 资源压缩
- CDN 加速
- 缓存策略

## 渲染优化

- 懒加载
- 虚拟列表
- 防抖节流

## 构建优化

- Tree Shaking
- 代码分割
- 懒加载路由
""",
            "author": users[1],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 2654,
            "like_count": 123,
            "comment_count": 42,
            "cover": "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600&h=400&fit=crop",
        },
        {
            "title": "CSS Grid 布局完全指南",
            "slug": "css-grid-complete-guide",
            "summary": "CSS Grid 是现代 CSS 布局的利器，本文详细介绍其使用方法。",
            "content": """# CSS Grid 布局完全指南

## 基本概念

Grid Container 和 Grid Items...

## 常用属性

- grid-template-columns
- grid-template-rows
- gap
- grid-column
- grid-row

## 实战案例

各种布局实现...
""",
            "author": users[4],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 1987,
            "like_count": 87,
            "comment_count": 29,
            "cover": "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&h=400&fit=crop",
        },
        {
            "title": "摄影作品｜城市风光",
            "slug": "photography-city-scenery",
            "summary": "分享一组城市风光摄影作品，记录城市的美好瞬间～",
            "content": """# 摄影作品｜城市风光

分享一组城市风光摄影作品！

📸 照片1：城市夜景
📸 照片2：街景
📸 照片3：建筑
📸 照片4：日落

#摄影 #城市风光
""",
            "author": users[4],
            "category": categories[3],
            "status": "published",
            "post_type": "note",
            "view_count": 2156,
            "like_count": 134,
            "comment_count": 23,
            "cover": "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=600&h=400&fit=crop",
        },
        {
            "title": "视频教程｜React 入门实战",
            "slug": "video-tutorial-react-basics",
            "summary": "通过视频学习 React 入门知识，包含实战项目演示！",
            "content": """# 视频教程｜React 入门实战

## 课程简介

本视频教程带你从零开始学习 React！

## 课程内容

1. React 基础概念
2. 组件开发
3. 状态管理
4. 实战项目

#React #视频教程 #前端开发
""",
            "author": users[1],
            "category": categories[2],
            "status": "published",
            "post_type": "video",
            "view_count": 3421,
            "like_count": 189,
            "comment_count": 56,
            "cover": "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=600&h=400&fit=crop",
        },
    ]

    posts = []
    for i, post_data in enumerate(posts_data):
        post_data["published_at"] = datetime.now() - timedelta(days=i * 2)
        cover_url = post_data.pop("cover", None)
        post, created = Post.objects.get_or_create(slug=post_data["slug"], defaults=post_data)
        if created:
            post_tags = tags[: (i + 2) % len(tags) + 1]
            post.tags.add(*post_tags)
            print(f"✓ 创建文章: {post.title}")
        else:
            print(f"→ 文章已存在: {post.title}")
        posts.append(post)
    
    # 创建关注关系
    print("\n--- 创建关注关系 ---")
    follow_count = 0
    for i, follower in enumerate(users):
        for j, following in enumerate(users):
            if i != j and (i + j) % 2 == 0:
                follow, created = Follow.objects.get_or_create(follower=follower, following=following)
                if created:
                    follow_count += 1
    print(f"✓ 创建 {follow_count} 个关注关系")
    
    # 创建通知
    print("\n--- 创建通知 ---")
    notification_count = 0
    for user in users[1:]:
        notification_types = ["like", "comment", "system"]
        for i, post in enumerate(posts[:3]):
            if post.author != user:
                notif_type = notification_types[i % len(notification_types)]
                notif_content = ""
                if notif_type == "like":
                    notif_content = f"{user.nickname} 赞了你的文章《{post.title}》"
                elif notif_type == "comment":
                    notif_content = f"{user.nickname} 评论了你的文章《{post.title}》"
                else:
                    notif_content = "系统通知：欢迎使用博客平台！"
                notification, created = Notification.objects.get_or_create(
                    user=post.author,
                    type=notif_type,
                    content=notif_content,
                    defaults={"is_read": False}
                )
                if created:
                    notification_count += 1
    print(f"✓ 创建 {notification_count} 个通知")

    # 创建评论
    print("\n--- 创建评论 ---")
    comment_count = 0
    comment_contents = [
        "写得太好了！学到了很多～",
        "感谢分享，正好需要这个！",
        "收藏了，以后慢慢看",
        "有个地方不太理解，能再详细说说吗？",
        "已点赞支持！期待更多内容～",
        "这篇文章太实用了！",
        "博主棒棒的，加油！",
        "正好在找这个内容，谢谢！",
    ]

    for post in posts:
        for i in range(3):
            comment_user = users[(i + post.id) % len(users)]
            comment, created = Comment.objects.get_or_create(
                post=post,
                user=comment_user,
                content=comment_contents[i % len(comment_contents)],
                is_approved=True,
            )
            if created:
                comment_count += 1
                if i == 0:
                    print(f"✓ 为文章 '{post.title}' 创建评论")

    # 创建点赞和收藏
    print("\n--- 创建互动数据 ---")
    like_count = 0
    favorite_count = 0
    for user in users:
        for post in posts[:5]:
            if user.id != post.author.id:
                like, created = PostLike.objects.get_or_create(user=user, post=post)
                if created:
                    like_count += 1
                favorite, created = Favorite.objects.get_or_create(user=user, post=post)
                if created:
                    favorite_count += 1

    print(f"✓ 创建 {like_count} 个点赞")
    print(f"✓ 创建 {favorite_count} 个收藏")

    print("\n" + "=" * 50)
    print("测试数据创建完成！")
    print("=" * 50)
    print(f"\n统计:")
    print(f"  - 用户: {len(users)} 个")
    print(f"  - 分类: {len(categories)} 个")
    print(f"  - 标签: {len(tags)} 个")
    print(f"  - 文章: {len(posts)} 篇")
    print(f"  - 评论: {comment_count} 条")
    print(f"  - 点赞: {like_count} 个")
    print(f"  - 收藏: {favorite_count} 个")
    print(f"  - 关注关系: {follow_count} 个")
    print(f"  - 通知: {notification_count} 个")
    print(f"\n可以使用以下账号登录（密码均为: test123456）:")
    for user in users:
        print(f"  - {user.username} ({user.nickname})")


if __name__ == "__main__":
    create_test_data()
