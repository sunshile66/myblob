from django.contrib import admin
from .models import Comment, CommentLike, CommentReaction, Emoji, Sticker


@admin.register(Comment)
class CommentAdmin(admin.ModelAdmin):
    list_display = ["id", "post", "user", "nickname", "content_short", "like_count", "is_approved", "is_deleted", "created_at"]
    list_filter = ["is_approved", "is_deleted", "created_at"]
    search_fields = ["content", "nickname", "user__username"]
    list_per_page = 50
    actions = ["approve_comments", "unapprove_comments", "delete_comments"]
    
    def content_short(self, obj):
        return obj.content[:50] + "..." if len(obj.content) > 50 else obj.content
    content_short.short_description = "内容"
    
    def approve_comments(self, request, queryset):
        queryset.update(is_approved=True)
    approve_comments.short_description = "批准选中的评论"
    
    def unapprove_comments(self, request, queryset):
        queryset.update(is_approved=False)
    unapprove_comments.short_description = "取消批准选中的评论"
    
    def delete_comments(self, request, queryset):
        queryset.update(is_deleted=True)
    delete_comments.short_description = "删除选中的评论"


@admin.register(CommentLike)
class CommentLikeAdmin(admin.ModelAdmin):
    list_display = ["id", "comment", "user", "created_at"]
    list_filter = ["created_at"]
    search_fields = ["user__username", "comment__id"]
    list_per_page = 50


@admin.register(CommentReaction)
class CommentReactionAdmin(admin.ModelAdmin):
    list_display = ["id", "comment", "user", "emoji", "created_at"]
    list_filter = ["emoji", "created_at"]
    search_fields = ["user__username", "comment__id"]
    list_per_page = 50


@admin.register(Emoji)
class EmojiAdmin(admin.ModelAdmin):
    list_display = ["id", "code", "name", "category", "sort_order", "is_active", "created_at"]
    list_filter = ["category", "is_active", "created_at"]
    search_fields = ["name", "code"]
    list_editable = ["sort_order", "is_active"]
    list_per_page = 50


@admin.register(Sticker)
class StickerAdmin(admin.ModelAdmin):
    list_display = ["id", "name", "category", "is_gif", "sort_order", "is_active", "created_at"]
    list_filter = ["category", "is_gif", "is_active", "created_at"]
    search_fields = ["name"]
    list_editable = ["sort_order", "is_active"]
    list_per_page = 50
