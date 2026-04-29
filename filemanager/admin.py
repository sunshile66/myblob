from django.contrib import admin
from .models import File, Folder, FileShare


@admin.register(File)
class FileAdmin(admin.ModelAdmin):
    list_display = ('filename', 'user', 'file_type', 'file_size', 'folder', 'is_public', 'upload_time')
    list_filter = ('file_type', 'is_public', 'upload_time')
    search_fields = ('filename', 'user__username')
    list_select_related = ('user', 'folder')
    readonly_fields = ('upload_time', 'update_time')


@admin.register(Folder)
class FolderAdmin(admin.ModelAdmin):
    list_display = ('name', 'user', 'parent', 'created_time')
    list_filter = ('created_time',)
    search_fields = ('name', 'user__username')
    list_select_related = ('user', 'parent')
    readonly_fields = ('created_time', 'update_time')


@admin.register(FileShare)
class FileShareAdmin(admin.ModelAdmin):
    list_display = ('file', 'share_code', 'created_by', 'download_count', 'expire_time', 'created_time')
    list_filter = ('created_time', 'expire_time')
    search_fields = ('share_code', 'file__filename', 'created_by__username')
    list_select_related = ('file', 'created_by')
    readonly_fields = ('created_time',)
