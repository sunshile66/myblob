from rest_framework import serializers
from .models import Announcement, Ad, SiteConfig


class AnnouncementSerializer(serializers.ModelSerializer):
    class Meta:
        model = Announcement
        fields = [
            'id', 'title', 'content', 'announcement_type',
            'is_active', 'is_pinned', 'sort', 'publish_time',
            'show_delay', 'auto_close', 'auto_close_time',
            'created_at', 'updated_at'
        ]
        read_only_fields = ['created_at', 'updated_at']


class AdSerializer(serializers.ModelSerializer):
    class Meta:
        model = Ad
        fields = ['id', 'title', 'image', 'link', 'position', 'is_active', 'sort', 'start_time', 'end_time', 'click_count', 'created_at', 'updated_at']
        read_only_fields = ['click_count', 'created_at', 'updated_at']


class SiteConfigSerializer(serializers.ModelSerializer):
    class Meta:
        model = SiteConfig
        fields = ['id', 'key', 'value', 'description', 'created_at', 'updated_at']
        read_only_fields = ['created_at', 'updated_at']
