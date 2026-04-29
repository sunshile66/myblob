from rest_framework import generics, permissions, status, parsers, viewsets
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.decorators import action
from django.contrib.auth import authenticate
from rest_framework.authtoken.models import Token
from .models import User, UserProfile, Follow
from .serializers import UserSerializer, UserProfileSerializer, RegisterSerializer
from core.verification import verify_verification_code, send_verification_code

try:
    from security.models import SecurityConfig
except ImportError:
    SecurityConfig = None


class RegisterView(generics.CreateAPIView):
    """
    用户注册视图
    允许新用户注册账号，自动创建认证 Token
    """
    queryset = User.objects.all()
    serializer_class = RegisterSerializer
    permission_classes = [permissions.AllowAny]

    def create(self, request, *args, **kwargs):
        """
        创建新用户并返回认证 Token
        """
        phone_or_email = request.data.get('phone_or_email')
        code = request.data.get('code')
        
        if phone_or_email and code:
            if not verify_verification_code(phone_or_email, code, 'sms'):
                return Response(
                    {'error': '验证码错误或已过期'},
                    status=status.HTTP_400_BAD_REQUEST
                )
        
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        token, _ = Token.objects.get_or_create(user=user)
        return Response(
            {"token": token.key, "user": UserSerializer(user).data},
            status=status.HTTP_201_CREATED
        )


class LoginView(APIView):
    """
    用户登录视图
    验证用户名和密码，成功后返回认证 Token 和用户信息
    """
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        """
        处理登录请求
        """
        username = request.data.get("username")
        password = request.data.get("password")
        phone_or_email = request.data.get('phone_or_email')
        code = request.data.get('code')
        
        config = None
        if SecurityConfig:
            config = SecurityConfig.get_config()
        
        only_register = config.verification_code_only_register if config else True
        
        if not only_register and phone_or_email and code:
            if not verify_verification_code(phone_or_email, code, None):
                return Response(
                    {'error': '验证码错误或已过期'},
                    status=status.HTTP_400_BAD_REQUEST
                )
        
        user = authenticate(username=username, password=password)
        if user:
            token, _ = Token.objects.get_or_create(user=user)
            return Response({"token": token.key, "user": UserSerializer(user).data})
        return Response(
            {"detail": "用户名或密码错误"},
            status=status.HTTP_400_BAD_REQUEST
        )


class LogoutView(APIView):
    """
    用户登出视图
    删除当前用户的认证 Token
    """
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        """
        处理登出请求，删除认证 Token
        """
        request.auth.delete()
        return Response({"detail": "已退出登录"})


class ProfileView(generics.RetrieveUpdateAPIView):
    """
    用户个人资料视图
    允许已登录用户查看和更新自己的个人资料
    """
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        """
        获取当前登录用户对象
        """
        return self.request.user


class AvatarUploadView(APIView):
    """
    用户头像上传视图
    允许已登录用户上传自己的头像
    """
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [parsers.MultiPartParser, parsers.FormParser]

    def post(self, request):
        """
        处理头像上传请求
        """
        if 'avatar' not in request.FILES:
            return Response(
                {"detail": "请选择要上传的头像文件"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        user = request.user
        user.avatar = request.FILES['avatar']
        user.save()
        
        return Response({"avatar": user.avatar.url})


class UserViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [permissions.AllowAny]
    
    @action(detail=True, methods=["post"], permission_classes=[permissions.IsAuthenticated])
    def follow(self, request, pk=None):
        user_to_follow = self.get_object()
        if user_to_follow == request.user:
            return Response({"detail": "不能关注自己"}, status=status.HTTP_400_BAD_REQUEST)
        
        follow, created = Follow.objects.get_or_create(follower=request.user, following=user_to_follow)
        if not created:
            return Response({"detail": "已经关注过了"}, status=status.HTTP_400_BAD_REQUEST)  # pyright: ignore[reportUnreachable]
        return Response({"detail": "关注成功"})
    
    @follow.mapping.delete
    def unfollow(self, request, pk=None):
        user_to_unfollow = self.get_object()
        deleted, _ = Follow.objects.filter(follower=request.user, following=user_to_unfollow).delete()
        if deleted:
            return Response({"detail": "取消关注成功"})
        return Response({"detail": "还没有关注"}, status=status.HTTP_400_BAD_REQUEST)


class SendVerificationCodeView(APIView):
    """
    发送验证码视图
    支持发送短信验证码和邮箱验证码
    """
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        """
        发送验证码
        """
        phone_or_email = request.data.get('phone_or_email')
        code_type = request.data.get('type', None)
        
        if not phone_or_email:
            return Response(
                {'error': '请提供手机号或邮箱'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')
        if x_forwarded_for:
            ip_address = x_forwarded_for.split(',')[0]
        else:
            ip_address = request.META.get('REMOTE_ADDR')
        
        success = send_verification_code(phone_or_email, code_type, ip_address)
        
        if success:
            return Response({'detail': '验证码发送成功'})
        else:
            return Response(
                {'error': '验证码发送失败或发送过于频繁'},
                status=status.HTTP_400_BAD_REQUEST
            )


class SecurityConfigView(APIView):
    """
    获取安全配置视图
    """
    permission_classes = [permissions.AllowAny]
    
    def get(self, request):
        """
        获取安全配置
        """
        config = None
        if SecurityConfig:
            config = SecurityConfig.get_config()
        
        return Response({
            'verification_code_enabled': config.verification_code_enabled if config else True,
            'verification_code_type': config.verification_code_type if config else 'email',
            'verification_code_method': config.verification_code_method if config else 'code',
            'verification_code_only_register': config.verification_code_only_register if config else True,
        })
