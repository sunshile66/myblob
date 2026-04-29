from django.db import models
from django.utils import timezone


class BaseModel(models.Model):
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')

    class Meta:
        abstract = True


class Announcement(BaseModel):
    """网站公告"""
    ANNOUNCEMENT_TYPE_CHOICES = [
        ('bar', '公告栏'),
        ('modal', '弹窗'),
    ]
    
    title = models.CharField(max_length=200, verbose_name='标题')
    content = models.TextField(verbose_name='内容')
    announcement_type = models.CharField(max_length=20, choices=ANNOUNCEMENT_TYPE_CHOICES, default='bar', verbose_name='公告类型')
    is_active = models.BooleanField(default=True, verbose_name='是否激活')
    is_pinned = models.BooleanField(default=False, verbose_name='是否置顶')
    sort = models.IntegerField(default=0, verbose_name='排序')
    publish_time = models.DateTimeField(default=timezone.now, verbose_name='发布时间')
    
    # 弹窗相关配置
    show_delay = models.IntegerField(default=5, verbose_name='延迟关闭时间(秒)', help_text='弹窗显示后多少秒可以关闭，0表示立即可以关闭')
    auto_close = models.BooleanField(default=False, verbose_name='是否自动关闭')
    auto_close_time = models.IntegerField(default=30, verbose_name='自动关闭时间(秒)', help_text='多少秒后自动关闭')

    class Meta:
        verbose_name = '公告'
        verbose_name_plural = '公告'
        ordering = ['-is_pinned', 'sort', '-publish_time']

    def __str__(self):
        return self.title


class SiteConfig(BaseModel):
    """网站配置"""
    key = models.CharField(max_length=100, unique=True, verbose_name='配置键')
    value = models.TextField(verbose_name='配置值')
    description = models.CharField(max_length=200, blank=True, verbose_name='描述')

    class Meta:
        verbose_name = '网站配置'
        verbose_name_plural = '网站配置'

    def __str__(self):
        return self.key


class Ad(BaseModel):
    """广告"""
    AD_POSITION_CHOICES = [
        ('header', '顶部'),
        ('sidebar', '侧边栏'),
        ('footer', '底部'),
        ('article_top', '文章顶部'),
        ('article_bottom', '文章底部'),
    ]

    title = models.CharField(max_length=200, verbose_name='标题')
    image = models.ImageField(upload_to='ads/', verbose_name='广告图片', blank=True, null=True)
    link = models.URLField(verbose_name='链接地址', blank=True, null=True)
    position = models.CharField(max_length=20, choices=AD_POSITION_CHOICES, default='sidebar', verbose_name='广告位置')
    is_active = models.BooleanField(default=True, verbose_name='是否激活')
    sort = models.IntegerField(default=0, verbose_name='排序')
    start_time = models.DateTimeField(verbose_name='开始时间', blank=True, null=True)
    end_time = models.DateTimeField(verbose_name='结束时间', blank=True, null=True)
    click_count = models.IntegerField(default=0, verbose_name='点击次数')

    class Meta:
        verbose_name = '广告'
        verbose_name_plural = '广告'
        ordering = ['position', 'sort', '-created_at']

    def __str__(self):
        return self.title

    def is_available(self):
        """检查广告是否在有效期内"""
        if not self.is_active:
            return False
        now = timezone.now()
        if self.start_time and now < self.start_time:
            return False
        if self.end_time and now > self.end_time:
            return False
        return True
