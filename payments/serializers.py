from rest_framework import serializers
from .models import Order, Payment, Refund, Wallet, WalletTransaction


class OrderSerializer(serializers.ModelSerializer):
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    payment_method_display = serializers.CharField(source='get_payment_method_display', read_only=True)

    class Meta:
        model = Order
        fields = [
            'id', 'order_no', 'user', 'plan', 'amount', 'status',
            'status_display', 'payment_method', 'payment_method_display',
            'paid_time', 'expired_time', 'remark', 'created_at', 'updated_at'
        ]
        read_only_fields = ['user', 'order_no', 'paid_time', 'created_at', 'updated_at']


class PaymentSerializer(serializers.ModelSerializer):
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    channel_display = serializers.CharField(source='get_channel_display', read_only=True)

    class Meta:
        model = Payment
        fields = [
            'id', 'order', 'payment_no', 'third_party_no', 'amount',
            'channel', 'channel_display', 'status', 'status_display',
            'paid_time', 'created_at', 'updated_at'
        ]
        read_only_fields = ['payment_no', 'paid_time', 'created_at', 'updated_at']


class RefundSerializer(serializers.ModelSerializer):
    status_display = serializers.CharField(source='get_status_display', read_only=True)

    class Meta:
        model = Refund
        fields = [
            'id', 'order', 'payment', 'refund_no', 'amount', 'reason',
            'status', 'status_display', 'refunded_time', 'remark',
            'created_at', 'updated_at'
        ]
        read_only_fields = ['refund_no', 'refunded_time', 'created_at', 'updated_at']


class WalletSerializer(serializers.ModelSerializer):
    available_balance = serializers.DecimalField(
        max_digits=10, decimal_places=2, read_only=True
    )

    class Meta:
        model = Wallet
        fields = [
            'id', 'user', 'balance', 'frozen_balance', 'available_balance',
            'total_income', 'total_expense', 'created_at', 'updated_at'
        ]
        read_only_fields = ['user', 'created_at', 'updated_at']


class WalletTransactionSerializer(serializers.ModelSerializer):
    transaction_type_display = serializers.CharField(
        source='get_transaction_type_display', read_only=True
    )

    class Meta:
        model = WalletTransaction
        fields = [
            'id', 'wallet', 'transaction_no', 'transaction_type',
            'transaction_type_display', 'amount', 'balance_before',
            'balance_after', 'description', 'related_order', 'created_at'
        ]
        read_only_fields = ['transaction_no', 'created_at']
