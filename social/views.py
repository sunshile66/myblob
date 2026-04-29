from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.response import Response
from django.contrib.auth import get_user_model
from django.utils import timezone
from .models import SocialAccount, OAuthApp, SocialLoginLog
from .serializers import SocialAccountSerializer, OAuthAppSerializer
import secrets
import hashlib

User = get_user_model()


def generate_state():
    """生成state参数，防止CSRF攻击"""
    return secrets.token_urlsafe(32)


class OAuthAppViewSet(viewsets.ReadOnlyModelViewSet):
    """OAuth应用配置视图"""
    serializer_class = OAuthAppSerializer
    permission_classes = [permissions.AllowAny]
    pagination_class = None

    def get_queryset(self):
        return OAuthApp.objects.filter(is_active=True)


class SocialAccountViewSet(viewsets.ReadOnlyModelViewSet):
    """第三方账号视图"""
    serializer_class = SocialAccountSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return SocialAccount.objects.filter(user=self.request.user)

    @action(detail=False, methods=['get'])
    def my_accounts(self, request):
        """获取当前用户的第三方账号"""
        accounts = self.get_queryset()
        serializer = self.get_serializer(accounts, many=True)
        return Response(serializer.data)


@api_view(['POST'])
@permission_classes([permissions.AllowAny])
def oauth_login(request):
    """获取OAuth登录URL"""
    provider = request.data.get('provider')
    
    if not provider:
        return Response(
            {'error': '请提供登录方式'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    try:
        app = OAuthApp.objects.get(provider=provider, is_active=True)
    except OAuthApp.DoesNotExist:
        return Response(
            {'error': '不支持的登录方式'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    state = generate_state()
    
    auth_urls = {
        'wechat': f'https://open.weixin.qq.com/connect/qrconnect?appid={app.app_id}&redirect_uri={app.redirect_uri}&response_type=code&scope={app.scope}&state={state}',
        'qq': f'https://graph.qq.com/oauth2.0/authorize?client_id={app.app_id}&redirect_uri={app.redirect_uri}&response_type=code&scope={app.scope}&state={state}',
        'weibo': f'https://api.weibo.com/oauth2/authorize?client_id={app.app_id}&redirect_uri={app.redirect_uri}&response_type=code&scope={app.scope}&state={state}',
        'github': f'https://github.com/login/oauth/authorize?client_id={app.app_id}&redirect_uri={app.redirect_uri}&scope={app.scope}&state={state}',
    }
    
    auth_url = auth_urls.get(provider)
    if not auth_url:
        return Response(
            {'error': '不支持的登录方式'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    return Response({
        'auth_url': auth_url,
        'state': state,
        'provider': provider
    })


@api_view(['POST'])
@permission_classes([permissions.AllowAny])
def oauth_callback(request):
    """OAuth回调处理（模拟）"""
    from rest_framework.authtoken.models import Token
    from accounts.serializers import UserSerializer
    
    provider = request.data.get('provider')
    code = request.data.get('code')
    
    if not provider or not code:
        return Response(
            {'error': '参数错误'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    try:
        app = OAuthApp.objects.get(provider=provider, is_active=True)
    except OAuthApp.DoesNotExist:
        return Response(
            {'error': '不支持的登录方式'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    openid = hashlib.md5(f"{provider}_{code}".encode()).hexdigest()[:32]
    
    try:
        social_account = SocialAccount.objects.get(provider=provider, openid=openid)
        user = social_account.user
        
        SocialLoginLog.objects.create(
            user=user,
            provider=provider,
            openid=openid,
            ip_address=request.META.get('REMOTE_ADDR', ''),
            user_agent=request.META.get('HTTP_USER_AGENT', ''),
            status='success'
        )
        
        token, _ = Token.objects.get_or_create(user=user)
        
        return Response({
            'token': token.key,
            'user': UserSerializer(user).data,
            'is_new': False
        })
        
    except SocialAccount.DoesNotExist:
        nickname = f"{app.get_provider_display()}_用户_{openid[-8:]}"
        username = f"{provider}_{openid[-12:]}"
        
        user = User.objects.create_user(
            username=username,
            email=f"{openid}@{provider}.com",
            nickname=nickname
        )
        
        social_account = SocialAccount.objects.create(
            user=user,
            provider=provider,
            openid=openid,
            nickname=nickname
        )
        
        SocialLoginLog.objects.create(
            user=user,
            provider=provider,
            openid=openid,
            ip_address=request.META.get('REMOTE_ADDR', ''),
            user_agent=request.META.get('HTTP_USER_AGENT', ''),
            status='success'
        )
        
        token, _ = Token.objects.get_or_create(user=user)
        
        return Response({
            'token': token.key,
            'user': UserSerializer(user).data,
            'is_new': True
        }, status=status.HTTP_201_CREATED)
