#!/usr/bin/env python
"""
简化版测试数据填充脚本
"""
import os
import sys
import django
from datetime import datetime, timedelta

sys.path.append(r"d:\code\Myblob")
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Myblob.settings")
django.setup()

from accounts.models import User, UserProfile, Follow
from blog.models import Category, Tag, Post
from comments.models import Comment
from interactions.models import PostLike, Favorite, Notification


def create_test_data():
    print("=" * 50)
    print("开始创建测试数据...")
    print("=" * 50)

    users = create_users()
    categories = create_categories()
    tags = create_tags()
    posts = create_posts(users, categories, tags)
    create_follows(users)
    create_notifications(users, posts)
    create_comments(users, posts)
    create_interactions(users, posts)

    print("\n" + "=" * 50)
    print("测试数据创建完成！")
    print("=" * 50)


def create_users():
    print("\n--- 创建用户 ---")
    users_data = [
        {"username": "admin", "email": "admin@example.com", "nickname": "管理员", "is_superuser": True, "is_staff": True, "bio": "热爱技术，热爱生活～", "location": "北京", "profession": "全栈开发工程师"},
        {"username": "zhangsan", "email": "zhangsan@example.com", "nickname": "张三", "bio": "前端开发工程师，专注于 Vue 和 React 技术栈", "location": "上海", "profession": "前端开发", "wechat": "zhangsan_dev"},
        {"username": "lisi", "email": "lisi@example.com", "nickname": "李四", "bio": "后端开发，Python 爱好者", "location": "广州", "profession": "后端开发", "company": "某某科技公司"},
        {"username": "wangwu", "email": "wangwu@example.com", "nickname": "王五", "bio": "产品经理，关注用户体验", "location": "深圳", "profession": "产品经理"},
        {"username": "zhaoliu", "email": "zhaoliu@example.com", "nickname": "赵六", "bio": "UI 设计师，追求极致的视觉体验", "location": "杭州", "profession": "UI 设计师"},
        {"username": "sunqi", "email": "sunqi@example.com", "nickname": "孙七", "bio": "数据分析师，用数据说话", "location": "成都", "profession": "数据分析师"},
        {"username": "zhouba", "email": "zhouba@example.com", "nickname": "周八", "bio": "运维工程师，保障系统稳定", "location": "武汉", "profession": "运维工程师"},
        {"username": "wujiu", "email": "wujiu@example.com", "nickname": "吴九", "bio": "测试工程师，质量守护者", "location": "南京", "profession": "测试工程师"},
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
    return users


def create_categories():
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
    return categories


def create_tags():
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
    return tags


def create_posts(users, categories, tags):
    print("\n--- 创建文章 ---")
    posts_data = [
        {
            "title": "Django 6.0 新特性介绍",
            "slug": "django-6-new-features",
            "summary": "本文详细介绍 Django 6.0 带来的新特性和改进，包括异步视图、性能优化、数据库改进等。",
            "content": "Django 6.0 新特性介绍...",
            "author": users[0],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 1256,
            "like_count": 42,
            "comment_count": 18,
        },
        {
            "title": "Vue 3 组合式 API 完全指南",
            "slug": "vue3-composition-api-guide",
            "summary": "深入学习 Vue 3 的组合式 API，包括 ref、reactive、computed、watch 等核心概念的使用方法。",
            "content": "Vue 3 组合式 API 完全指南...",
            "author": users[1],
            "category": categories[0],
            "status": "published",
            "post_type": "article",
            "view_count": 2341,
            "like_count": 89,
            "comment_count": 32,
        },
        {
            "title": "使用 Docker 部署 Django 应用",
            "slug": "docker-deploy-django",
            "summary": "从零开始学习如何使用 Docker 和 Docker Compose 部署 Django 应用程序。",
            "content": "使用 Docker 部署 Django 应用...",
            "author": users[2],
            "category": categories[2],
            "status": "published",
            "post_type": "note",
            "view_count": 876,
            "like_count": 28,
            "comment_count": 12,
        },
        {
            "title": "我的 2024 年度总结",
            "slug": "my-2024-summary",
            "summary": "回顾过去一年的成长、收获和感悟，展望新的一年。",
            "content": "我的 2024 年度总结...",
            "author": users[0],
            "category": categories[1],
            "status": "published",
            "post_type": "note",
            "view_count": 456,
            "like_count": 15,
            "comment_count": 8,
        },
        {
            "title": "美食探店｜这家店超好吃！",
            "slug": "food-review-awesome-restaurant",
            "summary": "今天发现了一家超级好吃的店！位置、推荐菜品、人均消费都给大家整理好了～",
            "content": "美食探店｜这家店超好吃！...",
            "author": users[3],
            "category": categories[4],
            "status": "published",
            "post_type": "note",
            "view_count": 3241,
            "like_count": 156,
            "comment_count": 48,
        },
        {
            "title": "旅行｜杭州周末游攻略",
            "slug": "travel-hangzhou-weekend-guide",
            "summary": "终于来到了心心念念的杭州！行程安排、拍照打卡点、实用Tips都整理好了～",
            "content": "旅行｜杭州周末游攻略...",
            "author": users[4],
            "category": categories[5],
            "status": "published",
            "post_type": "note",
            "view_count": 4521,
            "like_count": 234,
            "comment_count": 67,
        },
        {
            "title": "学习笔记｜React Hooks 知识点整理",
            "slug": "study-notes-react-hooks",
            "summary": "今天学习了 React Hooks，整理一下重要的知识点～",
            "content": "学习笔记｜React Hooks 知识点整理...",
            "author": users[1],
            "category": categories[2],
            "status": "published",
            "post_type": "note",
            "view_count": 1876,
            "like_count": 98,
            "comment_count": 31,
        },
        {
            "title": "视频教程｜React 入门实战",
            "slug": "video-tutorial-react-basics",
            "summary": "通过视频学习 React 入门知识，包含实战项目演示！",
            "content": "视频教程｜React 入门实战...",
            "author": users[1],
            "category": categories[2],
            "status": "published",
            "post_type": "video",
            "view_count": 3421,
            "like_count": 189,
            "comment_count": 56,
        },
    ]

    posts = []
    for i, post_data in enumerate(posts_data):
        post_data["published_at"] = datetime.now() - timedelta(days=i * 2)
        post, created = Post.objects.get_or_create(slug=post_data["slug"], defaults=post_data)
        if created:
            post_tags = tags[: (i + 2) % len(tags) + 1]
            post.tags.add(*post_tags)
            print(f"✓ 创建文章: {post.title}")
        else:
            print(f"→ 文章已存在: {post.title}")
        posts.append(post)
    return posts


def create_follows(users):
    print("\n--- 创建关注关系 ---")
    follow_count = 0
    for i, follower in enumerate(users):
        for j, following in enumerate(users):
            if i != j and (i + j) % 2 == 0:
                follow, created = Follow.objects.get_or_create(follower=follower, following=following)
                if created:
                    follow_count += 1
    print(f"✓ 创建 {follow_count} 个关注关系")


def create_notifications(users, posts):
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


def create_comments(users, posts):
    print("\n--- 创建评论 ---")
    comment_count = 0
    comment_contents = [
        "写得太好了！学到了很多～",
        "感谢分享，正好需要这个！",
        "收藏了，以后慢慢看",
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
    print(f"✓ 共创建 {comment_count} 条评论")


def create_interactions(users, posts):
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


if __name__ == "__main__":
    create_test_data()
