from django.db import models
from django.contrib.auth import get_user_model
from django.utils import timezone

User = get_user_model()


class SecurityConfig(models.Model):
    """安全配置"""
    
    # 验证码配置
    verification_code_enabled = models.BooleanField(default=True, verbose_name='启用验证码')
    verification_code_type = models.CharField(max_length=20, default='email', verbose_name='验证码类型', choices=[
        ('email', '邮箱验证码'),
        ('sms', '短信验证码'),
        ('auto', '自动选择（邮箱优先）'),
    ], help_text='默认使用邮箱验证码')
    verification_code_method = models.CharField(max_length=20, default='code', verbose_name='验证方式', choices=[
        ('code', '数字验证码'),
        ('captcha', '图形验证码'),
        ('slider', '滑块验证码'),
        ('none', '无感验证'),
        ('auto', '自动选择'),
    ], help_text='注册时使用的验证方式')
    verification_code_length = models.IntegerField(default=6, verbose_name='验证码长度')
    verification_code_expire_seconds = models.IntegerField(default=300, verbose_name='验证码过期时间(秒)')
    verification_code_only_register = models.BooleanField(default=True, verbose_name='仅注册需要验证码', help_text='勾选后只有注册需要验证码，登录不需要')
    
    # 反爬虫配置
    anti_spider_enabled = models.BooleanField(default=True, verbose_name='启用反爬虫')
    request_limit_per_minute = models.IntegerField(default=60, verbose_name='每分钟请求限制')
    request_limit_per_hour = models.IntegerField(default=1000, verbose_name='每小时请求限制')
    
    # 单点登录配置
    sso_enabled = models.BooleanField(default=True, verbose_name='启用单点登录')
    max_sessions = models.IntegerField(default=1, verbose_name='最大同时在线数')
    
    # 流量监控配置
    traffic_monitor_enabled = models.BooleanField(default=True, verbose_name='启用流量监控')
    auto_block_enabled = models.BooleanField(default=True, verbose_name='自动封禁')
    block_duration_seconds = models.IntegerField(default=3600, verbose_name='封禁时长(秒)')
    
    class Meta:
        verbose_name = '安全配置'
        verbose_name_plural = '安全配置'
    
    def __str__(self):
        return '安全配置'
    
    @classmethod
    def get_config(cls):
        """获取安全配置"""
        config, created = cls.objects.get_or_create(id=1)
        return config


class IPBlock(models.Model):
    """IP封禁记录"""
    
    ip_address = models.GenericIPAddressField(verbose_name='IP地址')
    reason = models.TextField(verbose_name='封禁原因')
    blocked_at = models.DateTimeField(auto_now_add=True, verbose_name='封禁时间')
    blocked_until = models.DateTimeField(verbose_name='解封时间')
    blocked_by = models.ForeignKey(
        User, 
        null=True, 
        blank=True, 
        on_delete=models.SET_NULL, 
        verbose_name='封禁人'
    )
    is_active = models.BooleanField(default=True, verbose_name='是否有效')
    
    class Meta:
        verbose_name = 'IP封禁'
        verbose_name_plural = 'IP封禁'
        indexes = [
            models.Index(fields=['ip_address', 'is_active']),
        ]
    
    def __str__(self):
        return f'{self.ip_address} - {self.reason[:20]}'
    
    def is_expired(self):
        """是否过期"""
        return timezone.now() > self.blocked_until


class RequestLog(models.Model):
    """请求日志"""
    
    ip_address = models.GenericIPAddressField(verbose_name='IP地址')
    user_agent = models.TextField(blank=True, null=True, verbose_name='User Agent')
    path = models.CharField(max_length=500, verbose_name='请求路径')
    method = models.CharField(max_length=10, verbose_name='请求方法')
    status_code = models.IntegerField(verbose_name='状态码')
    response_time_ms = models.IntegerField(verbose_name='响应时间(毫秒)')
    user = models.ForeignKey(
        User, 
        null=True, 
        blank=True, 
        on_delete=models.SET_NULL, 
        verbose_name='用户'
    )
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    is_spider = models.BooleanField(default=False, verbose_name='是否爬虫')
    
    class Meta:
        verbose_name = '请求日志'
        verbose_name_plural = '请求日志'
        indexes = [
            models.Index(fields=['ip_address', 'created_at']),
            models.Index(fields=['created_at']),
        ]
    
    def __str__(self):
        return f'{self.ip_address} {self.method} {self.path}'


class UserSession(models.Model):
    """用户会话"""
    
    user = models.ForeignKey(User, on_delete=models.CASCADE, verbose_name='用户')
    session_key = models.CharField(max_length=40, verbose_name='会话Key')
    ip_address = models.GenericIPAddressField(verbose_name='IP地址')
    user_agent = models.TextField(blank=True, null=True, verbose_name='User Agent')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='登录时间')
    last_activity = models.DateTimeField(auto_now=True, verbose_name='最后活跃时间')
    is_active = models.BooleanField(default=True, verbose_name='是否活跃')
    
    class Meta:
        verbose_name = '用户会话'
        verbose_name_plural = '用户会话'
        indexes = [
            models.Index(fields=['user', 'is_active']),
        ]
    
    def __str__(self):
        return f'{self.user.username} - {self.ip_address}'


class EmailConfig(models.Model):
    """邮箱配置"""
    
    provider = models.CharField(max_length=50, default='qq', verbose_name='邮箱服务商', choices=[
        ('qq', 'QQ邮箱'),
        ('163', '163邮箱'),
        ('gmail', 'Gmail'),
        ('outlook', 'Outlook'),
        ('custom', '自定义'),
    ])
    host = models.CharField(max_length=200, blank=True, verbose_name='SMTP服务器')
    port = models.IntegerField(default=587, verbose_name='端口')
    use_tls = models.BooleanField(default=True, verbose_name='使用TLS')
    use_ssl = models.BooleanField(default=False, verbose_name='使用SSL')
    username = models.EmailField(verbose_name='邮箱账号')
    password = models.CharField(max_length=200, verbose_name='密码/授权码')
    from_email = models.EmailField(verbose_name='发件人邮箱')
    is_active = models.BooleanField(default=True, verbose_name='是否启用')
    
    class Meta:
        verbose_name = '邮箱配置'
        verbose_name_plural = '邮箱配置'
    
    def __str__(self):
        return f'{self.get_provider_display()} - {self.username}'
    
    @classmethod
    def get_config(cls):
        """获取邮箱配置"""
        config = cls.objects.filter(is_active=True).first()
        return config


class SmsConfig(models.Model):
    """短信配置"""
    
    provider = models.CharField(max_length=50, default='aliyun', verbose_name='短信服务商', choices=[
        ('aliyun', '阿里云'),
        ('tencent', '腾讯云'),
        ('custom', '自定义'),
    ])
    access_key_id = models.CharField(max_length=200, verbose_name='AccessKey ID')
    access_key_secret = models.CharField(max_length=200, verbose_name='AccessKey Secret')
    sign_name = models.CharField(max_length=50, default='MyBlob', verbose_name='签名')
    template_code = models.CharField(max_length=50, verbose_name='模板CODE')
    is_active = models.BooleanField(default=True, verbose_name='是否启用')
    
    # 防盗刷配置
    daily_limit = models.IntegerField(default=100, verbose_name='每日发送限制')
    hourly_limit = models.IntegerField(default=20, verbose_name='每小时发送限制')
    minute_limit = models.IntegerField(default=5, verbose_name='每分钟发送限制')
    same_ip_limit = models.IntegerField(default=3, verbose_name='同一IP限制')
    same_receiver_limit = models.IntegerField(default=2, verbose_name='同一接收者限制')
    
    class Meta:
        verbose_name = '短信配置'
        verbose_name_plural = '短信配置'
    
    def __str__(self):
        return f'{self.get_provider_display()} - {self.sign_name}'
    
    @classmethod
    def get_config(cls):
        """获取短信配置"""
        config = cls.objects.filter(is_active=True).first()
        return config


class VerificationLog(models.Model):
    """验证码发送记录"""
    
    TYPE_CHOICES = [
        ('sms', '短信'),
        ('email', '邮箱'),
    ]
    
    STATUS_CHOICES = [
        ('success', '成功'),
        ('failed', '失败'),
        ('pending', '待发送'),
    ]
    
    receiver = models.CharField(max_length=200, verbose_name='接收者')
    code = models.CharField(max_length=20, verbose_name='验证码')
    code_type = models.CharField(max_length=10, choices=TYPE_CHOICES, verbose_name='类型')
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending', verbose_name='状态')
    ip_address = models.GenericIPAddressField(verbose_name='IP地址', null=True, blank=True)
    error_message = models.TextField(blank=True, verbose_name='错误信息')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    sent_at = models.DateTimeField(null=True, blank=True, verbose_name='发送时间')
    
    class Meta:
        verbose_name = '验证码记录'
        verbose_name_plural = '验证码记录'
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['receiver', 'created_at']),
            models.Index(fields=['ip_address', 'created_at']),
            models.Index(fields=['code_type', 'created_at']),
        ]
    
    def __str__(self):
        return f'{self.receiver} - {self.code} - {self.get_status_display()}'
