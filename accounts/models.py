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


class Follow(models.Model):
    follower = models.ForeignKey(User, on_delete=models.CASCADE, related_name="following")
    following = models.ForeignKey(User, on_delete=models.CASCADE, related_name="followers")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "accounts_follow"
        verbose_name = "关注"
        verbose_name_plural = "关注"
        constraints = [
            models.UniqueConstraint(fields=["follower", "following"], name="uniq_follower_following")
        ]
        indexes = [
            models.Index(fields=["follower"]),
            models.Index(fields=["following"]),
        ]

    def __str__(self):
        return f"{self.follower.username} 关注了 {self.following.username}"
