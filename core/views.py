from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.response import Response
from django.db import models
from django.utils import timezone
from .models import Announcement, Ad, SiteConfig
from .serializers import AnnouncementSerializer, AdSerializer
from .verification import send_verification_code, verify_verification_code


class AnnouncementViewSet(viewsets.ReadOnlyModelViewSet):
    """公告视图集"""
    queryset = Announcement.objects.all()
    serializer_class = AnnouncementSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        queryset = Announcement.objects.filter(is_active=True, publish_time__lte=timezone.now())
        return queryset


class AdViewSet(viewsets.ReadOnlyModelViewSet):
    """广告视图集"""
    queryset = Ad.objects.all()
    serializer_class = AdSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        now = timezone.now()
        queryset = Ad.objects.filter(is_active=True)
        
        position = self.request.query_params.get('position')
        if position:
            queryset = queryset.filter(position=position)
        
        queryset = queryset.filter(
            (models.Q(start_time__isnull=True) | models.Q(start_time__lte=now)) &
            (models.Q(end_time__isnull=True) | models.Q(end_time__gte=now))
        )
        
        return queryset

    @action(detail=True, methods=['post'])
    def click(self, request, pk=None):
        """记录广告点击"""
        ad = self.get_object()
        ad.click_count += 1
        ad.save(update_fields=['click_count'])
        return Response({'detail': '点击记录成功'})


@api_view(['GET'])
@permission_classes([permissions.AllowAny])
def get_site_config(request):
    """获取网站配置"""
    configs = SiteConfig.objects.all()
    config_dict = {}
    for config in configs:
        config_dict[config.key] = config.value
    return Response(config_dict)


@api_view(['POST'])
@permission_classes([permissions.AllowAny])
def send_code(request):
    """发送验证码"""
    phone_or_email = request.data.get('phone_or_email')
    code_type = request.data.get('type', 'sms')
    
    if not phone_or_email:
        return Response(
            {'error': '请输入手机号或邮箱'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    success = send_verification_code(phone_or_email, code_type)
    if success:
        return Response({'detail': '验证码已发送'})
    else:
        return Response(
            {'error': '发送失败，请重试'},
            status=status.HTTP_400_BAD_REQUEST
        )
