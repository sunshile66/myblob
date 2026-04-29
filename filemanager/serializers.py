from rest_framework import serializers
from .models import File, Folder, FileShare


class FolderSerializer(serializers.ModelSerializer):
    class Meta:
        model = Folder
        fields = ['id', 'name', 'parent', 'created_time', 'update_time']
        read_only_fields = ['id', 'created_time', 'update_time']


class FileSerializer(serializers.ModelSerializer):
    folder_name = serializers.CharField(source='folder.name', read_only=True)
    human_readable_size = serializers.SerializerMethodField()

    class Meta:
        model = File
        fields = ['id', 'filename', 'file_type', 'file_size', 'human_readable_size', 
                  'mime_type', 'folder', 'folder_name', 'is_public', 
                  'upload_time', 'update_time', 'file']
        read_only_fields = ['id', 'upload_time', 'update_time']

    def get_human_readable_size(self, obj):
        size = obj.file_size
        for unit in ['B', 'KB', 'MB', 'GB', 'TB']:
            if size < 1024.0:
                return f"{size:.2f} {unit}"
            size /= 1024.0
        return f"{size:.2f} PB"


class FileShareSerializer(serializers.ModelSerializer):
    filename = serializers.CharField(source='file.filename', read_only=True)
    file_id = serializers.IntegerField(source='file.id', read_only=True)

    class Meta:
        model = FileShare
        fields = ['id', 'file', 'file_id', 'filename', 'share_code', 'expire_time', 
                  'download_count', 'max_downloads', 'created_time']
        read_only_fields = ['id', 'share_code', 'download_count', 'created_time']
