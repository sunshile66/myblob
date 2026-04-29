from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import User, UserProfile


@admin.register(User)
class CustomUserAdmin(UserAdmin):
    list_display = ("username", "email", "nickname", "is_staff", "is_email_verified", "date_joined")
    list_filter = ("is_staff", "is_superuser", "is_email_verified", "date_joined")
    search_fields = ("username", "email", "nickname")
    list_per_page = 20
    list_select_related = True
    
    fieldsets = UserAdmin.fieldsets + (
        ("额外信息", {"fields": ("nickname", "avatar", "bio", "is_email_verified")}),
        ("社交信息", {"fields": ("website", "github", "weibo"), "classes": ("collapse",)}),
    )


@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ("user", "company", "profession", "location")
    search_fields = ("user__username", "user__email", "company", "location")
    list_select_related = ("user",)
    list_per_page = 20
    raw_id_fields = ("user",)
