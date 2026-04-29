from django.contrib import admin
from .models import MediaAsset


@admin.register(MediaAsset)
class MediaAssetAdmin(admin.ModelAdmin):
    list_display = ("title", "user", "media_type", "created_at")
    list_filter = ("media_type", "created_at")
    search_fields = ("title", "alt_text", "user__username")
    raw_id_fields = ("user",)
    date_hierarchy = "created_at"
