from django.contrib import admin
from .models import SocialAccount, SocialLoginLog, OAuthApp


@admin.register(SocialAccount)
class SocialAccountAdmin(admin.ModelAdmin):
    list_display = ['user', 'provider', 'nickname', 'created_at']
    list_filter = ['provider', 'created_at']
    search_fields = ['user__username', 'user__email', 'openid', 'nickname']
    raw_id_fields = ['user']
    readonly_fields = ['created_at', 'updated_at']


@admin.register(SocialLoginLog)
class SocialLoginLogAdmin(admin.ModelAdmin):
    list_display = ['provider', 'openid', 'user', 'status', 'ip_address', 'created_at']
    list_filter = ['provider', 'status', 'created_at']
    search_fields = ['openid', 'user__username', 'ip_address']
    raw_id_fields = ['user']
    readonly_fields = ['created_at']


@admin.register(OAuthApp)
class OAuthAppAdmin(admin.ModelAdmin):
    list_display = ['provider', 'app_id', 'is_active', 'created_at']
    list_filter = ['provider', 'is_active', 'created_at']
    search_fields = ['provider', 'app_id']
    readonly_fields = ['created_at', 'updated_at']
