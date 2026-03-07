1）账户层：认证表和资料表分离

保留自定义用户表，但尽量轻量化。

推荐：

# accounts/models.py
from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    email = models.EmailField("邮箱", unique=True)
    nickname = models.CharField("昵称", max_length=30, blank=True)
    avatar = models.ImageField("头像", upload_to="avatars/%Y/%m/", blank=True, null=True)
    is_email_verified = models.BooleanField("邮箱已验证", default=False)

    class Meta:
        db_table = "accounts_user"
        indexes = [
            models.Index(fields=["username"]),
            models.Index(fields=["email"]),
        ]

    def __str__(self):
        return self.nickname or self.username

扩展资料单独放：

class UserProfile(models.Model):
    user = models.OneToOneField("accounts.User", on_delete=models.CASCADE, related_name="profile")
    bio = models.TextField("个人简介", blank=True)
    website = models.URLField("个人网站", blank=True)
    company = models.CharField("公司", max_length=100, blank=True)
    profession = models.CharField("职业", max_length=100, blank=True)
    location = models.CharField("所在地", max_length=100, blank=True)
    phone = models.CharField("手机号", max_length=20, blank=True)
    wechat = models.CharField("微信", max_length=50, blank=True)
    qq = models.CharField("QQ", max_length=20, blank=True)
    weibo = models.CharField("微博", max_length=100, blank=True)

    class Meta:
        db_table = "accounts_profile"

这样做的好处是：

登录认证字段更干净；

后续要接短信、邮箱验证、OAuth 更方便；

用户资料表可以慢慢扩展，不污染 auth 主表。

2）博客核心表：分类、标签、文章、文章版本

博客系统最核心的其实不是“留言板”，而是文章系统。

推荐最少包含这几个表：

分类表 Category
class Category(models.Model):
    name = models.CharField("分类名", max_length=50, unique=True)
    slug = models.SlugField("路由标识", max_length=60, unique=True)
    description = models.CharField("描述", max_length=255, blank=True)
    sort = models.PositiveIntegerField("排序", default=0)
    is_active = models.BooleanField("启用", default=True)

    class Meta:
        db_table = "blog_category"
        ordering = ["sort", "id"]
标签表 Tag
class Tag(models.Model):
    name = models.CharField("标签名", max_length=30, unique=True)
    slug = models.SlugField("路由标识", max_length=40, unique=True)

    class Meta:
        db_table = "blog_tag"
文章表 Post
class Post(models.Model):
    class Status(models.TextChoices):
        DRAFT = "draft", "草稿"
        REVIEW = "review", "待审核"
        PUBLISHED = "published", "已发布"
        HIDDEN = "hidden", "隐藏"

    author = models.ForeignKey("accounts.User", on_delete=models.PROTECT, related_name="posts")
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True, related_name="posts")
    title = models.CharField("标题", max_length=200)
    slug = models.SlugField("slug", max_length=220, unique=True)
    summary = models.CharField("摘要", max_length=300, blank=True)
    content = models.TextField("正文")
    cover = models.ImageField("封面", upload_to="posts/covers/%Y/%m/", blank=True, null=True)
    status = models.CharField("状态", max_length=20, choices=Status.choices, default=Status.DRAFT)

    is_original = models.BooleanField("原创", default=True)
    allow_comment = models.BooleanField("允许评论", default=True)
    is_top = models.BooleanField("置顶", default=False)

    published_at = models.DateTimeField("发布时间", null=True, blank=True)
    created_at = models.DateTimeField("创建时间", auto_now_add=True)
    updated_at = models.DateTimeField("更新时间", auto_now=True)

    view_count = models.PositiveIntegerField("浏览量", default=0)
    comment_count = models.PositiveIntegerField("评论数", default=0)
    like_count = models.PositiveIntegerField("点赞数", default=0)

    tags = models.ManyToManyField(Tag, blank=True, related_name="posts")

    class Meta:
        db_table = "blog_post"
        ordering = ["-is_top", "-published_at", "-id"]
        indexes = [
            models.Index(fields=["status", "-published_at"]),
            models.Index(fields=["author", "status"]),
            models.Index(fields=["category", "status"]),
            models.Index(fields=["slug"]),
            models.Index(fields=["created_at"]),
        ]

这里几个关键点：

author 用 PROTECT，防止删用户把文章也误删；

slug 单独唯一，便于做 SEO 路由；

status + published_at 建组合索引，列表页和归档页会快很多；

comment_count / like_count / view_count 做冗余计数字段，别每次页面都 annotate(Count(...))；

草稿、待审核、已发布要分清。

Django 6.0 依然推荐把索引加在 Meta.indexes 中，把约束加在 Meta.constraints 中。

文章历史表 PostRevision

如果你后面要做“重新编辑 / 回滚版本”，建议加这个：

class PostRevision(models.Model):
    post = models.ForeignKey(Post, on_delete=models.CASCADE, related_name="revisions")
    editor = models.ForeignKey("accounts.User", on_delete=models.SET_NULL, null=True)
    title = models.CharField(max_length=200)
    summary = models.CharField(max_length=300, blank=True)
    content = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_post_revision"
        ordering = ["-id"]
3）互动层：评论、点赞、收藏、留言板
评论表 Comment

评论建议做成树形，但不要无限深度。

class Comment(models.Model):
    post = models.ForeignKey(Post, on_delete=models.CASCADE, related_name="comments")
    user = models.ForeignKey("accounts.User", on_delete=models.SET_NULL, null=True, blank=True)
    parent = models.ForeignKey("self", on_delete=models.CASCADE, null=True, blank=True, related_name="children")
    reply_to = models.ForeignKey("self", on_delete=models.SET_NULL, null=True, blank=True, related_name="+")
    nickname = models.CharField(max_length=50, blank=True)
    email = models.EmailField(blank=True)
    content = models.TextField()
    is_approved = models.BooleanField(default=True)
    is_deleted = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_comment"
        indexes = [
            models.Index(fields=["post", "is_approved", "created_at"]),
            models.Index(fields=["parent", "created_at"]),
        ]
点赞表 PostLike
class PostLike(models.Model):
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE)
    post = models.ForeignKey(Post, on_delete=models.CASCADE, related_name="likes")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_post_like"
        constraints = [
            models.UniqueConstraint(fields=["user", "post"], name="uniq_user_post_like")
        ]
收藏表 Favorite
class Favorite(models.Model):
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE)
    post = models.ForeignKey(Post, on_delete=models.CASCADE, related_name="favorites")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_favorite"
        constraints = [
            models.UniqueConstraint(fields=["user", "post"], name="uniq_user_post_favorite")
        ]
留言板 Board

你的 Board 可以保留，但建议改成：

class BoardMessage(models.Model):
    user = models.ForeignKey("accounts.User", on_delete=models.SET_NULL, null=True, blank=True)
    nickname = models.CharField(max_length=50)
    email = models.EmailField(max_length=254, blank=True)
    content = models.TextField()
    is_public = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "board_message"
        ordering = ["-id"]
        indexes = [
            models.Index(fields=["created_at"]),
        ]
4）媒体资源层：别只用 AlbumInfo

你当前的 AlbumInfo 更像一个简化版图片墙。博客系统建议抽象成统一资源表：

class MediaAsset(models.Model):
    class MediaType(models.TextChoices):
        IMAGE = "image", "图片"
        FILE = "file", "文件"
        VIDEO = "video", "视频"

    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE, related_name="assets")
    file = models.FileField(upload_to="uploads/%Y/%m/")
    media_type = models.CharField(max_length=20, choices=MediaType.choices)
    title = models.CharField(max_length=100, blank=True)
    alt_text = models.CharField(max_length=150, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "media_asset"
        indexes = [
            models.Index(fields=["user", "media_type"]),
            models.Index(fields=["created_at"]),
        ]

这比单独一个 AlbumInfo 更适合接富文本编辑器、Markdown 图片管理、文章封面库。你当前 AlbumInfo 只保存了 title/introduce/photo，扩展性比较弱。

models模型

settings.py 应该怎么优化

你现在的 settings 主要问题是“开发默认值太多”。

settings

 

settings背景设定

推荐拆分

建议改成：

config/
    settings/
        __init__.py
        base.py
        dev.py
        prod.py
base.py 核心建议
from pathlib import Path
import os

BASE_DIR = Path(__file__).resolve().parent.parent.parent

SECRET_KEY = os.getenv("DJANGO_SECRET_KEY", "dev-only-key")
DEBUG = os.getenv("DJANGO_DEBUG", "false").lower() == "true"
ALLOWED_HOSTS = os.getenv("DJANGO_ALLOWED_HOSTS", "127.0.0.1,localhost").split(",")

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "django.contrib.humanize",

    "accounts",
    "blog",
    "comment",
    "mediaapp",
]
数据库建议

你现在已经在用 PostgreSQL，这对博客系统是好事。

settings


建议至少改成环境变量读取：

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": os.getenv("POSTGRES_DB", "blog"),
        "USER": os.getenv("POSTGRES_USER", "postgres"),
        "PASSWORD": os.getenv("POSTGRES_PASSWORD", ""),
        "HOST": os.getenv("POSTGRES_HOST", "127.0.0.1"),
        "PORT": os.getenv("POSTGRES_PORT", "5432"),
        "CONN_MAX_AGE": 60,
        "OPTIONS": {
            # "sslmode": "require",
        },
    }
}
国际化建议

你现在是 LANGUAGE_CODE='en-us'、TIME_ZONE='UTC'。中文博客一般应改为：

LANGUAGE_CODE = "zh-hans"
TIME_ZONE = "Asia/Shanghai"
USE_I18N = True
USE_TZ = True

你当前配置如下。

settings背景设定

静态与媒体

你现在的 STATIC_URL / STATICFILES_DIRS / STATIC_ROOT / MEDIA_ROOT 基本方向对，但建议补尾斜杠并加模板上下文支持。

settings背景设定

STATIC_URL = "/static/"
STATIC_ROOT = BASE_DIR / "staticfiles"
STATICFILES_DIRS = [BASE_DIR / "static"]

MEDIA_URL = "/media/"
MEDIA_ROOT = BASE_DIR / "media"
安全设置

Django 6.0 的安全建议里，SecurityMiddleware、HTTPS 跳转、Cookie 安全标志这些都值得开，尤其生产环境。

生产环境建议：

SECURE_BROWSER_XSS_FILTER = True
SECURE_CONTENT_TYPE_NOSNIFF = True
X_FRAME_OPTIONS = "DENY"

SESSION_COOKIE_HTTPONLY = True
CSRF_COOKIE_HTTPONLY = True
SESSION_COOKIE_SECURE = True
CSRF_COOKIE_SECURE = True

SECURE_SSL_REDIRECT = True
SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
结构设计建议

我建议你别再用现在这种“article / album / account / interflow”比较散的方式继续堆功能了，直接整理成下面这样：

project/
├─ config/
│  ├─ settings/
│  │  ├─ base.py
│  │  ├─ dev.py
│  │  └─ prod.py
│  ├─ urls.py
│  └─ wsgi.py / asgi.py
├─ apps/
│  ├─ accounts/
│  ├─ blog/
│  ├─ comments/
│  ├─ interactions/
│  ├─ mediaapp/
│  └─ common/
├─ templates/
├─ static/
├─ media/
└─ manage.py

这样好处是：

accounts 只管登录注册和资料；

blog 只管文章、分类、标签；

comments 只管评论；

interactions 只管点赞收藏浏览记录；

mediaapp 只管上传文件；

common 放基类、工具类、中间件。