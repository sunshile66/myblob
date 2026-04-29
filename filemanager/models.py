from django.db import models
from django.conf import settings
import os
from uuid import uuid4


def upload_to(instance, filename):
    ext = filename.split('.')[-1]
    new_filename = f"{uuid4().hex}.{ext}"
    return f"files/{instance.user.id}/{new_filename}"


class File(models.Model):
    FILE_TYPES = [
        ('image', '图片'),
        ('document', '文档'),
        ('video', '视频'),
        ('audio', '音频'),
        ('archive', '压缩包'),
        ('other', '其他'),
    ]

    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='files')
    file = models.FileField(upload_to=upload_to, max_length=500)
    filename = models.CharField(max_length=255)
    file_type = models.CharField(max_length=20, choices=FILE_TYPES, default='other')
    file_size = models.BigIntegerField()
    mime_type = models.CharField(max_length=100, blank=True)
    folder = models.ForeignKey('Folder', on_delete=models.CASCADE, related_name='files', null=True, blank=True)
    is_public = models.BooleanField(default=False)
    upload_time = models.DateTimeField(auto_now_add=True)
    update_time = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['-upload_time']
        verbose_name = '文件'
        verbose_name_plural = '文件'

    def __str__(self):
        return self.filename

    def get_file_extension(self):
        return os.path.splitext(self.filename)[1].lower()

    def get_human_readable_size(self):
        for unit in ['B', 'KB', 'MB', 'GB', 'TB']:
            if self.file_size < 1024.0:
                return f"{self.file_size:.2f} {unit}"
            self.file_size /= 1024.0
        return f"{self.file_size:.2f} PB"


class Folder(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='folders')
    name = models.CharField(max_length=255)
    parent = models.ForeignKey('self', on_delete=models.CASCADE, related_name='children', null=True, blank=True)
    created_time = models.DateTimeField(auto_now_add=True)
    update_time = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['name']
        verbose_name = '文件夹'
        verbose_name_plural = '文件夹'
        unique_together = ['user', 'name', 'parent']

    def __str__(self):
        return self.name

    def get_path(self):
        path = [self.name]
        current = self.parent
        while current:
            path.insert(0, current.name)
            current = current.parent
        return '/'.join(path)


class FileShare(models.Model):
    file = models.ForeignKey(File, on_delete=models.CASCADE, related_name='shares')
    share_code = models.CharField(max_length=100, unique=True)
    share_password = models.CharField(max_length=100, blank=True)
    expire_time = models.DateTimeField(null=True, blank=True)
    download_count = models.IntegerField(default=0)
    max_downloads = models.IntegerField(null=True, blank=True)
    created_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    created_time = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-created_time']
        verbose_name = '文件分享'
        verbose_name_plural = '文件分享'

    def __str__(self):
        return f"{self.file.filename} - {self.share_code}"
