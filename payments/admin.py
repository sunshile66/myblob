from django.contrib import admin
from .models import Order, Payment, Refund, Wallet, WalletTransaction


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ['order_no', 'user', 'plan', 'amount', 'status', 'payment_method', 'paid_time', 'created_at']
    list_filter = ['status', 'payment_method', 'created_at']
    search_fields = ['order_no', 'user__username', 'user__email']
    readonly_fields = ['order_no', 'created_at', 'updated_at']
    raw_id_fields = ['user', 'plan']


@admin.register(Payment)
class PaymentAdmin(admin.ModelAdmin):
    list_display = ['payment_no', 'order', 'amount', 'channel', 'status', 'paid_time', 'created_at']
    list_filter = ['channel', 'status', 'created_at']
    search_fields = ['payment_no', 'third_party_no', 'order__order_no']
    readonly_fields = ['payment_no', 'created_at', 'updated_at']
    raw_id_fields = ['order']


@admin.register(Refund)
class RefundAdmin(admin.ModelAdmin):
    list_display = ['refund_no', 'order', 'amount', 'status', 'refunded_time', 'created_at']
    list_filter = ['status', 'created_at']
    search_fields = ['refund_no', 'order__order_no']
    readonly_fields = ['refund_no', 'created_at', 'updated_at']
    raw_id_fields = ['order', 'payment']


@admin.register(Wallet)
class WalletAdmin(admin.ModelAdmin):
    list_display = ['user', 'balance', 'frozen_balance', 'total_income', 'total_expense', 'created_at']
    search_fields = ['user__username', 'user__email']
    raw_id_fields = ['user']
    readonly_fields = ['created_at', 'updated_at']


@admin.register(WalletTransaction)
class WalletTransactionAdmin(admin.ModelAdmin):
    list_display = ['transaction_no', 'wallet', 'transaction_type', 'amount', 'created_at']
    list_filter = ['transaction_type', 'created_at']
    search_fields = ['transaction_no', 'wallet__user__username']
    readonly_fields = ['transaction_no', 'created_at']
    raw_id_fields = ['wallet', 'related_order']
