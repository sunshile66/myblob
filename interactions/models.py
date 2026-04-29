from django.db import models


class PostLike(models.Model):
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE)
    post = models.ForeignKey("blog.Post", on_delete=models.CASCADE, related_name="likes")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_post_like"
        verbose_name = "文章点赞"
        verbose_name_plural = "文章点赞"
        constraints = [
            models.UniqueConstraint(fields=["user", "post"], name="uniq_user_post_like")
        ]

    def __str__(self):
        return f"{self.user.username} 赞了 {self.post.title}"


class Favorite(models.Model):
    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE)
    post = models.ForeignKey("blog.Post", on_delete=models.CASCADE, related_name="favorites")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "blog_favorite"
        verbose_name = "收藏"
        verbose_name_plural = "收藏"
        constraints = [
            models.UniqueConstraint(fields=["user", "post"], name="uniq_user_post_favorite")
        ]

    def __str__(self):
        return f"{self.user.username} 收藏了 {self.post.title}"


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
        verbose_name = "留言板"
        verbose_name_plural = "留言板"
        indexes = [
            models.Index(fields=["created_at"]),
        ]

    def __str__(self):
        return f"{self.nickname} - {self.content[:30]}"


class Notification(models.Model):
    class NotificationType(models.TextChoices):
        COMMENT = "comment", "评论"
        LIKE = "like", "点赞"
        SYSTEM = "system", "系统通知"

    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE, related_name="notifications")
    type = models.CharField(max_length=20, choices=NotificationType.choices, default=NotificationType.SYSTEM)
    content = models.TextField()
    is_read = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "notification"
        ordering = ["-created_at"]
        verbose_name = "通知"
        verbose_name_plural = "通知"
        indexes = [
            models.Index(fields=["user", "is_read", "-created_at"]),
        ]

    def __str__(self):
        return f"{self.user.username} - {self.get_type_display()} - {self.content[:30]}"

