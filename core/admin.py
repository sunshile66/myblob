from django.contrib import admin
from .models import Announcement, Ad


@admin.register(Announcement)
class AnnouncementAdmin(admin.ModelAdmin):
    list_display = ['title', 'is_active', 'is_pinned', 'sort', 'publish_time', 'created_at']
    list_filter = ['is_active', 'is_pinned', 'publish_time']
    search_fields = ['title', 'content']
    list_editable = ['is_active', 'is_pinned', 'sort']
    date_hierarchy = 'publish_time'


@admin.register(Ad)
class AdAdmin(admin.ModelAdmin):
    list_display = ['title', 'position', 'is_active', 'sort', 'click_count', 'start_time', 'end_time', 'created_at']
    list_filter = ['position', 'is_active', 'start_time', 'end_time']
    search_fields = ['title', 'link']
    list_editable = ['is_active', 'sort']
    date_hierarchy = 'created_at'
    readonly_fields = ['click_count']
