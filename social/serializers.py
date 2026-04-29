from rest_framework import serializers
from .models import SocialAccount, SocialLoginLog, OAuthApp


class OAuthAppSerializer(serializers.ModelSerializer):
    provider_display = serializers.CharField(source='get_provider_display', read_only=True)

    class Meta:
        model = OAuthApp
        fields = [
            'id', 'provider', 'provider_display', 'app_id', 'redirect_uri',
            'scope', 'is_active', 'created_at', 'updated_at'
        ]
        read_only_fields = ['app_secret']


class SocialAccountSerializer(serializers.ModelSerializer):
    provider_display = serializers.CharField(source='get_provider_display', read_only=True)

    class Meta:
        model = SocialAccount
        fields = [
            'id', 'user', 'provider', 'provider_display', 'openid',
            'unionid', 'nickname', 'avatar', 'created_at', 'updated_at'
        ]
        read_only_fields = ['user', 'openid', 'unionid', 'access_token', 'refresh_token', 'expires_at']


class SocialLoginLogSerializer(serializers.ModelSerializer):
    provider_display = serializers.CharField(source='get_provider_display', read_only=True)
    status_display = serializers.CharField(source='get_status_display', read_only=True)

    class Meta:
        model = SocialLoginLog
        fields = [
            'id', 'provider', 'provider_display', 'openid', 'user',
            'status', 'status_display', 'ip_address', 'user_agent',
            'error_message', 'created_at'
        ]
        read_only_fields = ['created_at']
