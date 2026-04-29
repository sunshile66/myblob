from django.db import models
from django.conf import settings
from core.models import BaseModel


class SocialAccount(BaseModel):
    """第三方账号绑定"""
    PROVIDER_CHOICES = [
        ('wechat', '微信'),
        ('qq', 'QQ'),
        ('weibo', '微博'),
        ('github', 'GitHub'),
        ('google', 'Google'),
        ('facebook', 'Facebook'),
        ('twitter', 'Twitter'),
    ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='social_accounts',
        verbose_name='用户'
    )
    provider = models.CharField(max_length=20, choices=PROVIDER_CHOICES, verbose_name='第三方平台')
    openid = models.CharField(max_length=100, verbose_name='第三方唯一标识')
    unionid = models.CharField(max_length=100, verbose_name='联合ID', blank=True)
    nickname = models.CharField(max_length=100, verbose_name='昵称', blank=True)
    avatar = models.URLField(verbose_name='头像', blank=True)
    access_token = models.TextField(verbose_name='访问令牌', blank=True)
    refresh_token = models.TextField(verbose_name='刷新令牌', blank=True)
    expires_at = models.DateTimeField(verbose_name='过期时间', null=True, blank=True)
    extra_data = models.JSONField(verbose_name='额外数据', default=dict, blank=True)

    class Meta:
        verbose_name = '第三方账号'
        verbose_name_plural = '第三方账号'
        unique_together = ['provider', 'openid']
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.user.username} - {self.get_provider_display()}'


class SocialLoginLog(BaseModel):
    """第三方登录日志"""
    STATUS_CHOICES = [
        ('success', '成功'),
        ('failed', '失败'),
    ]

    provider = models.CharField(max_length=20, choices=SocialAccount.PROVIDER_CHOICES, verbose_name='第三方平台')
    openid = models.CharField(max_length=100, verbose_name='第三方唯一标识', blank=True)
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='social_login_logs',
        verbose_name='用户'
    )
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default='success', verbose_name='状态')
    ip_address = models.GenericIPAddressField(verbose_name='IP地址', null=True, blank=True)
    user_agent = models.TextField(verbose_name='User Agent', blank=True)
    error_message = models.TextField(verbose_name='错误信息', blank=True)

    class Meta:
        verbose_name = '第三方登录日志'
        verbose_name_plural = '第三方登录日志'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.get_provider_display()} - {self.get_status_display()}'


class OAuthApp(BaseModel):
    """OAuth应用配置"""
    PROVIDER_CHOICES = [
        ('wechat', '微信'),
        ('qq', 'QQ'),
        ('weibo', '微博'),
        ('github', 'GitHub'),
        ('google', 'Google'),
        ('facebook', 'Facebook'),
        ('twitter', 'Twitter'),
    ]

    provider = models.CharField(max_length=20, choices=PROVIDER_CHOICES, unique=True, verbose_name='第三方平台')
    app_id = models.CharField(max_length=200, verbose_name='应用ID')
    app_secret = models.CharField(max_length=200, verbose_name='应用密钥')
    redirect_uri = models.URLField(verbose_name='回调地址', blank=True)
    scope = models.CharField(max_length=200, verbose_name='权限范围', blank=True)
    is_active = models.BooleanField(default=True, verbose_name='是否启用')
    config = models.JSONField(verbose_name='额外配置', default=dict, blank=True)

    class Meta:
        verbose_name = 'OAuth应用配置'
        verbose_name_plural = 'OAuth应用配置'
        ordering = ['provider']

    def __str__(self):
        return f'{self.get_provider_display()} - {self.app_id}'
