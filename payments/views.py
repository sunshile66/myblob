from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.utils import timezone
from django.db import transaction
from django.utils.crypto import get_random_string
from .models import Order, Payment, Wallet, WalletTransaction
from .serializers import (
    OrderSerializer, PaymentSerializer, WalletSerializer, WalletTransactionSerializer
)
from membership.models import MembershipPlan, UserMembership


def generate_order_no():
    """生成订单号"""
    timestamp = timezone.now().strftime('%Y%m%d%H%M%S')
    random_str = get_random_string(6, allowed_chars='0123456789')
    return f'ORD{timestamp}{random_str}'


def generate_payment_no():
    """生成支付流水号"""
    timestamp = timezone.now().strftime('%Y%m%d%H%M%S')
    random_str = get_random_string(8, allowed_chars='0123456789')
    return f'PAY{timestamp}{random_str}'


def generate_transaction_no():
    """生成交易号"""
    timestamp = timezone.now().strftime('%Y%m%d%H%M%S')
    random_str = get_random_string(6, allowed_chars='0123456789')
    return f'TXN{timestamp}{random_str}'


class OrderViewSet(viewsets.ModelViewSet):
    """订单视图"""
    serializer_class = OrderSerializer
    permission_classes = [permissions.IsAuthenticated]
    http_method_names = ['get', 'post', 'delete']

    def get_queryset(self):
        return Order.objects.filter(user=self.request.user).order_by('-created_at')

    def create(self, request, *args, **kwargs):
        """创建订单"""
        plan_id = request.data.get('plan_id')
        if not plan_id:
            return Response(
                {'error': '请选择会员套餐'},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            plan = MembershipPlan.objects.get(id=plan_id, is_active=True)
        except MembershipPlan.DoesNotExist:
            return Response(
                {'error': '会员套餐不存在或已下架'},
                status=status.HTTP_400_BAD_REQUEST
            )

        with transaction.atomic():
            order = Order.objects.create(
                order_no=generate_order_no(),
                user=request.user,
                plan=plan,
                amount=plan.price,
                status='pending',
                expired_time=timezone.now() + timezone.timedelta(hours=24)
            )

        serializer = self.get_serializer(order)
        return Response(serializer.data, status=status.HTTP_201_CREATED)

    @action(detail=True, methods=['post'])
    def cancel(self, request, pk=None):
        """取消订单"""
        order = self.get_object()
        
        if order.status != 'pending':
            return Response(
                {'error': '只有待支付订单才能取消'},
                status=status.HTTP_400_BAD_REQUEST
            )

        order.status = 'cancelled'
        order.save()
        
        serializer = self.get_serializer(order)
        return Response(serializer.data)

    @action(detail=True, methods=['post'])
    def pay(self, request, pk=None):
        """支付订单"""
        order = self.get_object()
        payment_method = request.data.get('payment_method', 'alipay')

        if order.status != 'pending':
            return Response(
                {'error': '订单状态异常，无法支付'},
                status=status.HTTP_400_BAD_REQUEST
            )

        if order.is_expired():
            order.status = 'expired'
            order.save()
            return Response(
                {'error': '订单已过期，请重新下单'},
                status=status.HTTP_400_BAD_REQUEST
            )

        with transaction.atomic():
            payment = Payment.objects.create(
                order=order,
                payment_no=generate_payment_no(),
                amount=order.amount,
                channel=payment_method,
                status='success',
                paid_time=timezone.now()
            )

            order.status = 'paid'
            order.payment_method = payment_method
            order.paid_time = timezone.now()
            order.save()

            self._activate_membership(order)

        serializer = self.get_serializer(order)
        return Response(serializer.data)

    def _activate_membership(self, order):
        """激活会员"""
        user_membership, created = UserMembership.objects.get_or_create(user=order.user)
        
        if order.plan:
            user_membership.plan = order.plan
            
            if order.plan.duration_days == 0:
                user_membership.is_lifetime = True
                user_membership.start_time = timezone.now()
                user_membership.end_time = None
            else:
                user_membership.is_lifetime = False
                if user_membership.end_time and user_membership.end_time > timezone.now():
                    user_membership.end_time = user_membership.end_time + timezone.timedelta(days=order.plan.duration_days)
                else:
                    user_membership.start_time = timezone.now()
                    user_membership.end_time = timezone.now() + timezone.timedelta(days=order.plan.duration_days)
            
            user_membership.is_active = True
            user_membership.save()


class WalletViewSet(viewsets.ReadOnlyModelViewSet):
    """钱包视图"""
    serializer_class = WalletSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Wallet.objects.filter(user=self.request.user)

    @action(detail=False, methods=['get'])
    def my_wallet(self, request):
        """获取当前用户的钱包"""
        wallet, created = Wallet.objects.get_or_create(user=request.user)
        serializer = self.get_serializer(wallet)
        return Response(serializer.data)


class WalletTransactionViewSet(viewsets.ReadOnlyModelViewSet):
    """钱包交易记录视图"""
    serializer_class = WalletTransactionSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return WalletTransaction.objects.filter(
            wallet__user=self.request.user
        ).order_by('-created_at')

