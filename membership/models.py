from django.db import models
from django.utils import timezone
from django.conf import settings
from core.models import BaseModel


class MembershipPlan(BaseModel):
    """会员套餐"""
    name = models.CharField(max_length=100, verbose_name='套餐名称')
    description = models.TextField(verbose_name='套餐描述', blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='价格')
    duration_days = models.IntegerField(verbose_name='有效期(天)', help_text='0表示永久')
    features = models.JSONField(verbose_name='权益列表', default=list, blank=True)
    is_active = models.BooleanField(default=True, verbose_name='是否启用')
    sort = models.IntegerField(default=0, verbose_name='排序')
    is_popular = models.BooleanField(default=False, verbose_name='是否推荐')
    badge_text = models.CharField(max_length=50, verbose_name='角标文字', blank=True)

    class Meta:
        verbose_name = '会员套餐'
        verbose_name_plural = '会员套餐'
        ordering = ['sort', '-created_at']

    def __str__(self):
        return f'{self.name} - ¥{self.price}'


class UserMembership(BaseModel):
    """用户会员"""
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='membership',
        verbose_name='用户'
    )
    plan = models.ForeignKey(
        MembershipPlan,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='user_memberships',
        verbose_name='会员套餐'
    )
    start_time = models.DateTimeField(verbose_name='开始时间', null=True, blank=True)
    end_time = models.DateTimeField(verbose_name='结束时间', null=True, blank=True)
    is_active = models.BooleanField(default=False, verbose_name='是否有效')
    is_lifetime = models.BooleanField(default=False, verbose_name='是否永久会员')

    class Meta:
        verbose_name = '用户会员'
        verbose_name_plural = '用户会员'

    def __str__(self):
        return f'{self.user.username} - {self.plan.name if self.plan else "无会员"}'

    def is_valid(self):
        """检查会员是否有效"""
        if not self.is_active:
            return False
        if self.is_lifetime:
            return True
        if not self.end_time:
            return False
        return timezone.now() < self.end_time


class MembershipBenefit(BaseModel):
    """会员权益"""
    BENEFIT_TYPE_CHOICES = [
        ('feature', '功能权限'),
        ('quota', '数量配额'),
        ('discount', '折扣优惠'),
        ('other', '其他'),
    ]

    name = models.CharField(max_length=100, verbose_name='权益名称')
    code = models.CharField(max_length=50, unique=True, verbose_name='权益代码')
    benefit_type = models.CharField(max_length=20, choices=BENEFIT_TYPE_CHOICES, default='feature', verbose_name='权益类型')
    description = models.TextField(verbose_name='权益描述', blank=True)
    icon = models.CharField(max_length=100, verbose_name='图标', blank=True)
    is_active = models.BooleanField(default=True, verbose_name='是否启用')

    class Meta:
        verbose_name = '会员权益'
        verbose_name_plural = '会员权益'
        ordering = ['-created_at']

    def __str__(self):
        return self.name


class PlanBenefitRelation(BaseModel):
    """套餐-权益关联"""
    plan = models.ForeignKey(
        MembershipPlan,
        on_delete=models.CASCADE,
        related_name='benefit_relations',
        verbose_name='会员套餐'
    )
    benefit = models.ForeignKey(
        MembershipBenefit,
        on_delete=models.CASCADE,
        related_name='plan_relations',
        verbose_name='会员权益'
    )
    value = models.CharField(max_length=200, verbose_name='权益值', blank=True, help_text='如配额数量、折扣比例等')

    class Meta:
        verbose_name = '套餐权益关联'
        verbose_name_plural = '套餐权益关联'
        unique_together = ['plan', 'benefit']

    def __str__(self):
        return f'{self.plan.name} - {self.benefit.name}'
