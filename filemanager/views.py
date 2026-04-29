from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from django.http import FileResponse
from django.utils import timezone
from uuid import uuid4
from .models import File, Folder, FileShare
from .serializers import FileSerializer, FolderSerializer, FileShareSerializer


class FolderViewSet(viewsets.ModelViewSet):
    serializer_class = FolderSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Folder.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class FileViewSet(viewsets.ModelViewSet):
    serializer_class = FileSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return File.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        file_obj = self.request.FILES.get('file')
        if file_obj:
            filename = file_obj.name
            file_size = file_obj.size
            mime_type = file_obj.content_type
            
            ext = filename.split('.')[-1].lower()
            if ext in ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg']:
                file_type = 'image'
            elif ext in ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md']:
                file_type = 'document'
            elif ext in ['mp4', 'avi', 'mov', 'wmv', 'mkv']:
                file_type = 'video'
            elif ext in ['mp3', 'wav', 'flac', 'aac', 'ogg']:
                file_type = 'audio'
            elif ext in ['zip', 'rar', '7z', 'tar', 'gz']:
                file_type = 'archive'
            else:
                file_type = 'other'
            
            serializer.save(
                user=self.request.user,
                filename=filename,
                file_size=file_size,
                mime_type=mime_type,
                file_type=file_type
            )

    @action(detail=True, methods=['get'])
    def download(self, request, pk=None):
        file_obj = self.get_object()
        response = FileResponse(file_obj.file.open(), as_attachment=True, filename=file_obj.filename)
        return response


class FileShareViewSet(viewsets.ModelViewSet):
    serializer_class = FileShareSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return FileShare.objects.filter(created_by=self.request.user)

    def perform_create(self, serializer):
        share_code = uuid4().hex[:12]
        serializer.save(created_by=self.request.user, share_code=share_code)

    @action(detail=False, methods=['get'], url_path='access/(?P<share_code>[^/.]+)')
    def access_share(self, request, share_code=None):
        share = get_object_or_404(FileShare, share_code=share_code)
        
        if share.expire_time and share.expire_time < timezone.now():
            return Response({'detail': '分享已过期'}, status=status.HTTP_400_BAD_REQUEST)
        
        if share.max_downloads and share.download_count >= share.max_downloads:
            return Response({'detail': '下载次数已达上限'}, status=status.HTTP_400_BAD_REQUEST)
        
        share.download_count += 1
        share.save()
        
        file_obj = share.file
        response = FileResponse(file_obj.file.open(), as_attachment=True, filename=file_obj.filename)
        return response
