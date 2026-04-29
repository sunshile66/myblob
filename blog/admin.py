from django.contrib import admin
from .models import Category, Tag, Post, PostRevision


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ("name", "slug", "sort", "is_active")
    list_filter = ("is_active",)
    search_fields = ("name", "slug")
    prepopulated_fields = {"slug": ("name",)}
    list_per_page = 20


@admin.register(Tag)
class TagAdmin(admin.ModelAdmin):
    list_display = ("name", "slug")
    search_fields = ("name", "slug")
    prepopulated_fields = {"slug": ("name",)}
    list_per_page = 50


@admin.register(Post)
class PostAdmin(admin.ModelAdmin):
    list_display = ("title", "author", "category", "status", "is_top", "published_at", "view_count")
    list_filter = ("status", "is_top", "is_original", "category", "created_at")
    search_fields = ("title", "slug", "content", "author__username")
    prepopulated_fields = {"slug": ("title",)}
    filter_horizontal = ("tags",)
    raw_id_fields = ("author",)
    date_hierarchy = "published_at"
    list_select_related = ("author", "category")
    list_per_page = 20
    
    fieldsets = (
        ("基本信息", {"fields": ("title", "slug", "author", "category", "tags")}),
        ("内容", {"fields": ("content", "summary", "cover_image")}),
        ("设置", {"fields": ("status", "is_top", "is_original", "post_type"), "classes": ("collapse",)}),
        ("发布时间", {"fields": ("published_at",), "classes": ("collapse",)}),
        ("SEO", {"fields": ("meta_title", "meta_description"), "classes": ("collapse",)}),
    )


@admin.register(PostRevision)
class PostRevisionAdmin(admin.ModelAdmin):
    list_display = ("post", "editor", "created_at")
    list_filter = ("created_at",)
    search_fields = ("post__title", "editor__username")
    raw_id_fields = ("post", "editor")
    list_select_related = ("post", "editor")
    list_per_page = 20
