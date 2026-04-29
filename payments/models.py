from django.db import models
from django.utils import timezone
from django.conf import settings
from core.models import BaseModel
from membership.models import MembershipPlan


class Order(BaseModel):
    """订单"""
    ORDER_STATUS_CHOICES = [
        ('pending', '待支付'),
        ('paid', '已支付'),
        ('cancelled', '已取消'),
        ('refunded', '已退款'),
        ('expired', '已过期'),
    ]

    PAYMENT_METHOD_CHOICES = [
        ('alipay', '支付宝'),
        ('wechat', '微信支付'),
        ('balance', '余额支付'),
    ]

    order_no = models.CharField(max_length=32, unique=True, verbose_name='订单号')
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='orders',
        verbose_name='用户'
    )
    plan = models.ForeignKey(
        MembershipPlan,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='orders',
        verbose_name='会员套餐'
    )
    amount = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='订单金额')
    status = models.CharField(max_length=20, choices=ORDER_STATUS_CHOICES, default='pending', verbose_name='订单状态')
    payment_method = models.CharField(max_length=20, choices=PAYMENT_METHOD_CHOICES, blank=True, verbose_name='支付方式')
    paid_time = models.DateTimeField(verbose_name='支付时间', null=True, blank=True)
    expired_time = models.DateTimeField(verbose_name='过期时间', null=True, blank=True)
    remark = models.TextField(verbose_name='备注', blank=True)

    class Meta:
        verbose_name = '订单'
        verbose_name_plural = '订单'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.order_no} - {self.user.username}'

    def is_paid(self):
        """是否已支付"""
        return self.status == 'paid'

    def is_expired(self):
        """是否已过期"""
        if self.status != 'pending':
            return False
        if not self.expired_time:
            return False
        return timezone.now() > self.expired_time


class Payment(BaseModel):
    """支付记录"""
    PAYMENT_STATUS_CHOICES = [
        ('pending', '待支付'),
        ('success', '支付成功'),
        ('failed', '支付失败'),
        ('refunded', '已退款'),
    ]

    PAYMENT_CHANNEL_CHOICES = [
        ('alipay', '支付宝'),
        ('wechat', '微信支付'),
    ]

    order = models.ForeignKey(
        Order,
        on_delete=models.CASCADE,
        related_name='payments',
        verbose_name='订单'
    )
    payment_no = models.CharField(max_length=64, unique=True, verbose_name='支付流水号')
    third_party_no = models.CharField(max_length=128, verbose_name='第三方支付号', blank=True)
    amount = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='支付金额')
    channel = models.CharField(max_length=20, choices=PAYMENT_CHANNEL_CHOICES, verbose_name='支付渠道')
    status = models.CharField(max_length=20, choices=PAYMENT_STATUS_CHOICES, default='pending', verbose_name='支付状态')
    paid_time = models.DateTimeField(verbose_name='支付时间', null=True, blank=True)
    raw_data = models.JSONField(verbose_name='原始数据', default=dict, blank=True)

    class Meta:
        verbose_name = '支付记录'
        verbose_name_plural = '支付记录'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.payment_no} - {self.order.order_no}'


class Refund(BaseModel):
    """退款记录"""
    REFUND_STATUS_CHOICES = [
        ('pending', '待处理'),
        ('processing', '处理中'),
        ('success', '退款成功'),
        ('failed', '退款失败'),
    ]

    order = models.ForeignKey(
        Order,
        on_delete=models.CASCADE,
        related_name='refunds',
        verbose_name='订单'
    )
    payment = models.ForeignKey(
        Payment,
        on_delete=models.CASCADE,
        related_name='refunds',
        verbose_name='支付记录',
        null=True,
        blank=True
    )
    refund_no = models.CharField(max_length=64, unique=True, verbose_name='退款单号')
    amount = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='退款金额')
    reason = models.TextField(verbose_name='退款原因')
    status = models.CharField(max_length=20, choices=REFUND_STATUS_CHOICES, default='pending', verbose_name='退款状态')
    refunded_time = models.DateTimeField(verbose_name='退款时间', null=True, blank=True)
    remark = models.TextField(verbose_name='备注', blank=True)

    class Meta:
        verbose_name = '退款记录'
        verbose_name_plural = '退款记录'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.refund_no} - {self.order.order_no}'


class Wallet(BaseModel):
    """用户钱包"""
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='wallet',
        verbose_name='用户'
    )
    balance = models.DecimalField(max_digits=10, decimal_places=2, default=0, verbose_name='余额')
    frozen_balance = models.DecimalField(max_digits=10, decimal_places=2, default=0, verbose_name='冻结余额')
    total_income = models.DecimalField(max_digits=10, decimal_places=2, default=0, verbose_name='累计收入')
    total_expense = models.DecimalField(max_digits=10, decimal_places=2, default=0, verbose_name='累计支出')

    class Meta:
        verbose_name = '用户钱包'
        verbose_name_plural = '用户钱包'

    def __str__(self):
        return f'{self.user.username} - ¥{self.balance}'

    def available_balance(self):
        """可用余额"""
        return self.balance - self.frozen_balance


class WalletTransaction(BaseModel):
    """钱包交易记录"""
    TRANSACTION_TYPE_CHOICES = [
        ('recharge', '充值'),
        ('consume', '消费'),
        ('refund', '退款'),
        ('income', '收入'),
    ]

    wallet = models.ForeignKey(
        Wallet,
        on_delete=models.CASCADE,
        related_name='transactions',
        verbose_name='钱包'
    )
    transaction_no = models.CharField(max_length=32, unique=True, verbose_name='交易号')
    transaction_type = models.CharField(max_length=20, choices=TRANSACTION_TYPE_CHOICES, verbose_name='交易类型')
    amount = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='金额')
    balance_before = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='变动前余额')
    balance_after = models.DecimalField(max_digits=10, decimal_places=2, verbose_name='变动后余额')
    description = models.CharField(max_length=200, verbose_name='描述', blank=True)
    related_order = models.ForeignKey(
        Order,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='wallet_transactions',
        verbose_name='关联订单'
    )

    class Meta:
        verbose_name = '钱包交易记录'
        verbose_name_plural = '钱包交易记录'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.transaction_no} - {self.transaction_type}'
