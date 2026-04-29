from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
from blog.models import Category, Tag, Post
from comments.models import Emoji, Sticker
from social.models import OAuthApp
from core.models import Announcement, SiteConfig
import random
import string


class Command(BaseCommand):
    help = "填充测试数据"
    
    def handle(self, *args, **options):
        self.stdout.write("开始填充测试数据...")
        
        User = get_user_model()
        
        # 创建测试用户
        if not User.objects.filter(username="testuser").exists():
            user = User.objects.create_user(
                username="testuser",
                email="testuser@example.com",
                password="test123456",
                nickname="测试用户"
            )
            self.stdout.write(self.style.SUCCESS(f"创建测试用户: {user.username}"))
        
        admin_user = User.objects.filter(is_superuser=True).first()
        if not admin_user:
            admin_user = User.objects.create_superuser(
                username="admin",
                email="admin@example.com",
                password="admin123456"
            )
            self.stdout.write(self.style.SUCCESS(f"创建管理员: {admin_user.username}"))
        
        # 创建分类
        categories_data = [
            ("技术", "tech"),
            ("生活", "life"),
            ("学习", "study"),
            ("娱乐", "entertainment"),
        ]
        for name, slug in categories_data:
            cat, created = Category.objects.get_or_create(
                slug=slug,
                defaults={"name": name, "sort": categories_data.index((name, slug))}
            )
            if created:
                self.stdout.write(self.style.SUCCESS(f"创建分类: {name}"))
        
        # 创建标签
        tags_data = [
            ("Python", "python"),
            ("Django", "django"),
            ("JavaScript", "javascript"),
            ("Vue", "vue"),
            ("生活", "life"),
            ("学习", "study"),
        ]
        for name, slug in tags_data:
            tag, created = Tag.objects.get_or_create(
                slug=slug,
                defaults={"name": name}
            )
            if created:
                self.stdout.write(self.style.SUCCESS(f"创建标签: {name}"))
        
        # 创建测试文章
        categories = list(Category.objects.all())
        tags = list(Tag.objects.all())
        users = list(User.objects.filter(is_superuser=False))
        
        if categories and users:
            for i in range(1, 6):
                slug = f"test-post-{i}"
                if not Post.objects.filter(slug=slug).exists():
                    post = Post.objects.create(
                        title=f"测试文章 {i}",
                        slug=slug,
                        summary=f"这是测试文章 {i} 的摘要",
                        content=f"# 测试文章 {i}\n\n这是测试文章 {i} 的内容。\n\n## 章节 1\n\n这是章节 1 的内容。\n\n## 章节 2\n\n这是章节 2 的内容。",
                        author=random.choice(users),
                        category=random.choice(categories),
                        status="published",
                        post_type="article",
                        view_count=random.randint(10, 1000),
                        like_count=random.randint(0, 100),
                        comment_count=random.randint(0, 50),
                    )
                    post.tags.add(*random.sample(tags, min(3, len(tags))))
                    self.stdout.write(self.style.SUCCESS(f"创建文章: {post.title}"))
        
        # 创建表情
        emojis_data = [
            ("😀", "笑脸", "emotions"),
            ("😂", "大笑", "emotions"),
            ("😍", "喜欢", "emotions"),
            ("👍", "赞", "actions"),
            ("❤️", "爱心", "emotions"),
            ("🎉", "庆祝", "celebration"),
            ("🤔", "思考", "emotions"),
            ("😢", "难过", "emotions"),
            ("😡", "愤怒", "emotions"),
            ("😮", "惊讶", "emotions"),
        ]
        for code, name, category in emojis_data:
            emoji, created = Emoji.objects.get_or_create(
                code=code,
                defaults={"name": name, "category": category, "sort_order": emojis_data.index((code, name, category))}
            )
            if created:
                self.stdout.write(self.style.SUCCESS(f"创建表情: {name}"))
        
        # 创建OAuth应用配置
        oauth_providers = [
            ("wechat", "微信登录"),
            ("qq", "QQ登录"),
            ("weibo", "微博登录"),
            ("github", "GitHub登录"),
        ]
        for provider, name in oauth_providers:
            app, created = OAuthApp.objects.get_or_create(
                provider=provider,
                defaults={
                    "name": name,
                    "app_id": f"test-{provider}-id",
                    "app_secret": f"test-{provider}-secret",
                    "redirect_uri": "http://localhost:3004/auth/callback",
                    "scope": "user_info",
                    "is_active": True,
                }
            )
            if created:
                self.stdout.write(self.style.SUCCESS(f"创建OAuth应用: {name}"))
        
        # 创建公告
        if not Announcement.objects.exists():
            Announcement.objects.create(
                title="欢迎访问MyBlob",
                content="这是一个测试公告，欢迎访问我们的网站！",
                is_active=True,
            )
            self.stdout.write(self.style.SUCCESS("创建公告"))
        
        # 创建网站配置
        site_configs = {
            "site_name": "MyBlob",
            "site_description": "一个有趣的博客平台",
            "site_keywords": "博客,分享,学习",
        }
        for key, value in site_configs.items():
            config, created = SiteConfig.objects.get_or_create(
                key=key,
                defaults={"value": value}
            )
            if created:
                self.stdout.write(self.style.SUCCESS(f"创建配置: {key}"))
        
        self.stdout.write(self.style.SUCCESS("测试数据填充完成！"))
