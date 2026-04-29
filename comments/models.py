from django.db import models


class Comment(models.Model):
    post = models.ForeignKey("blog.Post", on_delete=models.CASCADE, related_name="comments")
    user = models.ForeignKey("accounts.User", on_delete=models.SET_NULL, null=True, blank=True)
    parent = models.ForeignKey("self", on_delete=models.CASCADE, null=True, blank=True, related_name="children")
    reply_to = models.ForeignKey("self", on_delete=models.SET_NULL, null=True, blank=True, related_name="+")
    nickname = models.CharField(max_length=50, blank=True)
    email = models.EmailField(blank=True)
    content = models.TextField()
    like_count = models.IntegerField(default=0, verbose_name="点赞数")
    is_approved = models.BooleanField(default=True)
    is_deleted = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_comment"
        ordering = ["-created_at"]
        verbose_name = "评论"
        verbose_name_plural = "评论"
        indexes = [
            models.Index(fields=["post", "is_approved", "created_at"]),
            models.Index(fields=["parent", "created_at"]),
        ]

    def __str__(self):
        return f"{self.nickname or self.user.username if self.user else '匿名'} - {self.content[:30]}"


class CommentLike(models.Model):
    """评论点赞"""
    comment = models.ForeignKey(Comment, on_delete=models.CASCADE, related_name="likes")
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE, related_name="comment_likes")
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = "comment_like"
        unique_together = ("comment", "user")
        verbose_name = "评论点赞"
        verbose_name_plural = "评论点赞"
    
    def __str__(self):
        return f"{self.user.username} 赞了 {self.comment.id}"


class Emoji(models.Model):
    """表情包"""
    name = models.CharField(max_length=50, verbose_name="表情名称")
    code = models.CharField(max_length=20, unique=True, verbose_name="表情代码")
    image_url = models.URLField(blank=True, verbose_name="表情图片")
    category = models.CharField(max_length=50, blank=True, verbose_name="分类")
    sort_order = models.IntegerField(default=0, verbose_name="排序")
    is_active = models.BooleanField(default=True, verbose_name="是否启用")
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = "emoji"
        ordering = ["sort_order", "created_at"]
        verbose_name = "表情"
        verbose_name_plural = "表情"
    
    def __str__(self):
        return f"{self.code} - {self.name}"


class CommentReaction(models.Model):
    """评论表情反应"""
    REACTION_EMOJIS = [
        ("👍", "赞"),
        ("❤️", "喜欢"),
        ("😂", "大笑"),
        ("😮", "惊讶"),
        ("😢", "难过"),
        ("😡", "愤怒"),
    ]
    
    comment = models.ForeignKey(Comment, on_delete=models.CASCADE, related_name="reactions")
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE, related_name="comment_reactions")
    emoji = models.CharField(max_length=10, choices=REACTION_EMOJIS, verbose_name="表情")
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = "comment_reaction"
        unique_together = ("comment", "user")
        verbose_name = "评论表情反应"
        verbose_name_plural = "评论表情反应"
    
    def __str__(self):
        return f"{self.user.username} {self.emoji} 评论 {self.comment.id}"


class Sticker(models.Model):
    """贴图/GIF动图"""
    name = models.CharField(max_length=100, verbose_name="贴图名称")
    image_url = models.URLField(verbose_name="贴图地址")
    thumbnail_url = models.URLField(blank=True, verbose_name="缩略图地址")
    category = models.CharField(max_length=50, blank=True, verbose_name="分类")
    is_gif = models.BooleanField(default=False, verbose_name="是否为GIF")
    sort_order = models.IntegerField(default=0, verbose_name="排序")
    is_active = models.BooleanField(default=True, verbose_name="是否启用")
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = "sticker"
        ordering = ["sort_order", "created_at"]
        verbose_name = "贴图"
        verbose_name_plural = "贴图"
    
    def __str__(self):
        return self.name

