from django.db import models
from django.conf import settings
from uuid import uuid4


class APIService(models.Model):
    STATUS_CHOICES = [
        ('draft', '草稿'),
        ('published', '已发布'),
        ('deprecated', '已废弃'),
    ]

    CATEGORY_CHOICES = [
        ('ai', '人工智能'),
        ('data', '数据服务'),
        ('tools', '工具服务'),
        ('social', '社交服务'),
        ('other', '其他'),
    ]

    name = models.CharField(max_length=200, verbose_name='服务名称')
    slug = models.SlugField(max_length=200, unique=True, verbose_name='标识')
    description = models.TextField(verbose_name='服务描述')
    category = models.CharField(max_length=50, choices=CATEGORY_CHOICES, default='other', verbose_name='分类')
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='draft', verbose_name='状态')
    
    base_url = models.URLField(verbose_name='基础URL')
    documentation_url = models.URLField(blank=True, verbose_name='文档URL')
    
    version = models.CharField(max_length=50, default='1.0.0', verbose_name='版本号')
    price_per_call = models.DecimalField(max_digits=10, decimal_places=6, default=0.0, verbose_name='每次调用价格')
    free_calls_per_day = models.IntegerField(default=100, verbose_name='每日免费调用次数')
    
    is_public = models.BooleanField(default=True, verbose_name='公开可见')
    is_paid = models.BooleanField(default=False, verbose_name='付费服务')
    
    created_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='api_services')
    created_time = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_time = models.DateTimeField(auto_now=True, verbose_name='更新时间')

    class Meta:
        ordering = ['-created_time']
        verbose_name = 'API服务'
        verbose_name_plural = 'API服务'

    def __str__(self):
        return self.name


class APIEndpoint(models.Model):
    METHOD_CHOICES = [
        ('GET', 'GET'),
        ('POST', 'POST'),
        ('PUT', 'PUT'),
        ('DELETE', 'DELETE'),
        ('PATCH', 'PATCH'),
    ]

    service = models.ForeignKey(APIService, on_delete=models.CASCADE, related_name='endpoints', verbose_name='所属服务')
    path = models.CharField(max_length=500, verbose_name='路径')
    method = models.CharField(max_length=10, choices=METHOD_CHOICES, default='GET', verbose_name='请求方法')
    summary = models.CharField(max_length=500, verbose_name='接口简介')
    description = models.TextField(blank=True, verbose_name='详细描述')
    
    request_example = models.TextField(blank=True, verbose_name='请求示例')
    response_example = models.TextField(blank=True, verbose_name='响应示例')
    
    is_active = models.BooleanField(default=True, verbose_name='启用')
    created_time = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')

    class Meta:
        ordering = ['path']
        verbose_name = 'API端点'
        verbose_name_plural = 'API端点'
        unique_together = ['service', 'path', 'method']

    def __str__(self):
        return f"{self.method} {self.path}"


class APIKey(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='api_keys', verbose_name='用户')
    name = models.CharField(max_length=200, verbose_name='密钥名称')
    key = models.CharField(max_length=100, unique=True, default=uuid4, verbose_name='API密钥')
    
    is_active = models.BooleanField(default=True, verbose_name='启用')
    rate_limit_per_minute = models.IntegerField(default=60, verbose_name='每分钟限制')
    rate_limit_per_day = models.IntegerField(default=1000, verbose_name='每日限制')
    
    created_time = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    expires_time = models.DateTimeField(null=True, blank=True, verbose_name='过期时间')

    class Meta:
        ordering = ['-created_time']
        verbose_name = 'API密钥'
        verbose_name_plural = 'API密钥'

    def __str__(self):
        return f"{self.user.username} - {self.name}"


class APICallLog(models.Model):
    service = models.ForeignKey(APIService, on_delete=models.CASCADE, related_name='call_logs', verbose_name='服务')
    endpoint = models.ForeignKey(APIEndpoint, on_delete=models.SET_NULL, null=True, blank=True, verbose_name='端点')
    api_key = models.ForeignKey(APIKey, on_delete=models.CASCADE, related_name='call_logs', verbose_name='API密钥')
    
    request_method = models.CharField(max_length=10, verbose_name='请求方法')
    request_url = models.URLField(verbose_name='请求URL')
    request_headers = models.JSONField(blank=True, null=True, verbose_name='请求头')
    request_body = models.TextField(blank=True, verbose_name='请求体')
    
    response_status = models.IntegerField(verbose_name='响应状态码')
    response_headers = models.JSONField(blank=True, null=True, verbose_name='响应头')
    response_body = models.TextField(blank=True, verbose_name='响应体')
    
    execution_time = models.FloatField(verbose_name='执行时间(秒)')
    call_time = models.DateTimeField(auto_now_add=True, verbose_name='调用时间')

    class Meta:
        ordering = ['-call_time']
        verbose_name = 'API调用日志'
        verbose_name_plural = 'API调用日志'

    def __str__(self):
        return f"{self.service.name} - {self.call_time}"
