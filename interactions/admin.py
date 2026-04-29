from django.contrib import admin
from .models import PostLike, Favorite, BoardMessage, Notification


@admin.register(PostLike)
class PostLikeAdmin(admin.ModelAdmin):
    list_display = ("user", "post", "created_at")
    list_filter = ("created_at",)
    search_fields = ("user__username", "post__title")
    raw_id_fields = ("user", "post")
    date_hierarchy = "created_at"


@admin.register(Favorite)
class FavoriteAdmin(admin.ModelAdmin):
    list_display = ("user", "post", "created_at")
    list_filter = ("created_at",)
    search_fields = ("user__username", "post__title")
    raw_id_fields = ("user", "post")
    date_hierarchy = "created_at"


@admin.register(BoardMessage)
class BoardMessageAdmin(admin.ModelAdmin):
    list_display = ("nickname", "email", "user", "is_public", "created_at")
    list_filter = ("is_public", "created_at")
    search_fields = ("nickname", "email", "content", "user__username")
    raw_id_fields = ("user",)
    date_hierarchy = "created_at"


@admin.register(Notification)
class NotificationAdmin(admin.ModelAdmin):
    list_display = ("user", "type", "is_read", "created_at")
    list_filter = ("type", "is_read", "created_at")
    search_fields = ("user__username", "content")
    raw_id_fields = ("user",)
    date_hierarchy = "created_at"
    actions = ["mark_as_read", "mark_as_unread"]

    def mark_as_read(self, request, queryset):
        queryset.update(is_read=True)

    mark_as_read.short_description = "标记为已读"

    def mark_as_unread(self, request, queryset):
        queryset.update(is_read=False)

    mark_as_unread.short_description = "标记为未读"

