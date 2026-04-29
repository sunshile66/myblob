from rest_framework import serializers
from .models import APIService, APIEndpoint, APIKey, APICallLog


class APIEndpointSerializer(serializers.ModelSerializer):
    class Meta:
        model = APIEndpoint
        fields = ['id', 'path', 'method', 'summary', 'description', 
                  'request_example', 'response_example', 'is_active', 'created_time']
        read_only_fields = ['id', 'created_time']


class APIServiceSerializer(serializers.ModelSerializer):
    endpoints = APIEndpointSerializer(many=True, read_only=True)
    created_by_username = serializers.CharField(source='created_by.username', read_only=True)

    class Meta:
        model = APIService
        fields = ['id', 'name', 'slug', 'description', 'category', 'status', 
                  'base_url', 'documentation_url', 'version', 'price_per_call', 
                  'free_calls_per_day', 'is_public', 'is_paid', 'endpoints',
                  'created_by', 'created_by_username', 'created_time', 'updated_time']
        read_only_fields = ['id', 'created_time', 'updated_time']


class APIKeySerializer(serializers.ModelSerializer):
    class Meta:
        model = APIKey
        fields = ['id', 'name', 'key', 'is_active', 'rate_limit_per_minute', 
                  'rate_limit_per_day', 'created_time', 'expires_time']
        read_only_fields = ['id', 'key', 'created_time']


class APICallLogSerializer(serializers.ModelSerializer):
    service_name = serializers.CharField(source='service.name', read_only=True)
    api_key_name = serializers.CharField(source='api_key.name', read_only=True)

    class Meta:
        model = APICallLog
        fields = ['id', 'service', 'service_name', 'endpoint', 'api_key', 'api_key_name',
                  'request_method', 'request_url', 'response_status', 'execution_time', 'call_time']
        read_only_fields = ['id', 'call_time']
