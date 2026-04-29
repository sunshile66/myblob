from django.db import models


class MediaAsset(models.Model):
    class MediaType(models.TextChoices):
        IMAGE = "image", "图片"
        FILE = "file", "文件"
        VIDEO = "video", "视频"

    user = models.ForeignKey("accounts.User", on_delete=models.CASCADE, related_name="assets")
    file = models.FileField(upload_to="uploads/%Y/%m/")
    media_type = models.CharField(max_length=20, choices=MediaType.choices)
    title = models.CharField(max_length=100, blank=True)
    alt_text = models.CharField(max_length=150, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "media_asset"
        ordering = ["-created_at"]
        verbose_name = "媒体资源"
        verbose_name_plural = "媒体资源"
        indexes = [
            models.Index(fields=["user", "media_type"]),
            models.Index(fields=["created_at"]),
        ]

    def __str__(self):
        return self.title or self.file.name
