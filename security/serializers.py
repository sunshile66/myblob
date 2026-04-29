from rest_framework import serializers
from .models import SecurityConfig, IPBlock, RequestLog, UserSession


class SecurityConfigSerializer(serializers.ModelSerializer):
    """安全配置序列化器"""
    
    class Meta:
        model = SecurityConfig
        fields = '__all__'
        read_only_fields = ['id']


class IPBlockSerializer(serializers.ModelSerializer):
    """IP封禁序列化器"""
    blocked_by_username = serializers.CharField(source='blocked_by.username', read_only=True)
    is_expired = serializers.BooleanField(read_only=True)
    
    class Meta:
        model = IPBlock
        fields = '__all__'
        read_only_fields = ['id', 'blocked_at', 'blocked_by']


class RequestLogSerializer(serializers.ModelSerializer):
    """请求日志序列化器"""
    username = serializers.CharField(source='user.username', read_only=True)
    
    class Meta:
        model = RequestLog
        fields = '__all__'
        read_only_fields = ['id', 'created_at']


class UserSessionSerializer(serializers.ModelSerializer):
    """用户会话序列化器"""
    username = serializers.CharField(source='user.username', read_only=True)
    
    class Meta:
        model = UserSession
        fields = '__all__'
        read_only_fields = ['id', 'created_at', 'last_activity']
