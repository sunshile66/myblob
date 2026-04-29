from django.db import models
from django.utils import timezone


class Category(models.Model):
    name = models.CharField("分类名", max_length=50, unique=True)
    slug = models.SlugField("路由标识", max_length=60, unique=True)
    description = models.CharField("描述", max_length=255, blank=True)
    sort = models.PositiveIntegerField("排序", default=0)
    is_active = models.BooleanField("启用", default=True)

    class Meta:
        db_table = "blog_category"
        ordering = ["sort", "id"]
        verbose_name = "分类"
        verbose_name_plural = "分类"

    def __str__(self):
        return self.name


class Tag(models.Model):
    name = models.CharField("标签名", max_length=30, unique=True)
    slug = models.SlugField("路由标识", max_length=40, unique=True)

    class Meta:
        db_table = "blog_tag"
        verbose_name = "标签"
        verbose_name_plural = "标签"

    def __str__(self):
        return self.name


class Post(models.Model):
    class Status(models.TextChoices):
        DRAFT = "draft", "草稿"
        REVIEW = "review", "待审核"
        PUBLISHED = "published", "已发布"
        HIDDEN = "hidden", "隐藏"

    class PostType(models.TextChoices):
        ARTICLE = "article", "文章"
        NOTE = "note", "笔记"
        VIDEO = "video", "视频"

    author = models.ForeignKey("accounts.User", on_delete=models.PROTECT, related_name="posts")
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True, related_name="posts")
    title = models.CharField("标题", max_length=200)
    slug = models.SlugField("slug", max_length=220, unique=True)
    summary = models.CharField("摘要", max_length=300, blank=True)
    content = models.TextField("正文")
    cover = models.ImageField("封面", upload_to="posts/covers/%Y/%m/", blank=True, null=True)
    video = models.ForeignKey("mediaapp.MediaAsset", on_delete=models.SET_NULL, null=True, blank=True, related_name="video_posts")
    post_type = models.CharField("类型", max_length=20, choices=PostType.choices, default=PostType.NOTE)
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
        verbose_name = "文章"
        verbose_name_plural = "文章"
        indexes = [
            models.Index(fields=["status", "-published_at"]),
            models.Index(fields=["author", "status"]),
            models.Index(fields=["category", "status"]),
            models.Index(fields=["slug"]),
            models.Index(fields=["created_at"]),
        ]

    def __str__(self):
        return self.title

    def save(self, *args, **kwargs):
        if self.status == self.Status.PUBLISHED and not self.published_at:
            self.published_at = timezone.now()
        super().save(*args, **kwargs)


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
        verbose_name = "文章版本"
        verbose_name_plural = "文章版本"

    def __str__(self):
        return f"{self.post.title} - {self.created_at}"
