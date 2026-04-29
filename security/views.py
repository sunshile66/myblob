from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, permissions
from django.http import HttpResponse
from .captcha import create_captcha, verify_captcha, create_slider_captcha, verify_slider_captcha, NoCaptcha
import base64


class CaptchaImageView(APIView):
    """获取图形验证码图片"""
    permission_classes = [permissions.AllowAny]
    
    def get(self, request):
        captcha_id = request.query_params.get('id')
        captcha_id, image_buffer = create_captcha(captcha_id)
        
        image_base64 = base64.b64encode(image_buffer.getvalue()).decode('utf-8')
        
        return Response({
            'captcha_id': captcha_id,
            'image': f'data:image/png;base64,{image_base64}'
        })


class CaptchaVerifyView(APIView):
    """验证图形验证码"""
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        captcha_id = request.data.get('captcha_id')
        code = request.data.get('code')
        
        if not captcha_id or not code:
            return Response(
                {'error': '请提供验证码ID和验证码'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        if verify_captcha(captcha_id, code):
            return Response({'valid': True})
        else:
            return Response(
                {'valid': False, 'error': '验证码错误或已过期'},
                status=status.HTTP_400_BAD_REQUEST
            )


class SliderCaptchaView(APIView):
    """获取滑块验证码"""
    permission_classes = [permissions.AllowAny]
    
    def get(self, request):
        captcha_id = request.query_params.get('id')
        captcha_id, data = create_slider_captcha(captcha_id)
        
        image_base64 = base64.b64encode(data['image'].getvalue()).decode('utf-8')
        
        return Response({
            'captcha_id': captcha_id,
            'image': f'data:image/png;base64,{image_base64}',
            'target_y': data['target_y'],
            'block_size': data['block_size']
        })


class SliderCaptchaVerifyView(APIView):
    """验证滑块验证码"""
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        captcha_id = request.data.get('captcha_id')
        x = request.data.get('x')
        
        if not captcha_id or x is None:
            return Response(
                {'error': '请提供验证码ID和位置'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        if verify_slider_captcha(captcha_id, x):
            return Response({'valid': True})
        else:
            return Response(
                {'valid': False, 'error': '验证失败'},
                status=status.HTTP_400_BAD_REQUEST
            )


class NoCaptchaSessionView(APIView):
    """创建无感验证会话"""
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        session_id = NoCaptcha.create_session()
        return Response({'session_id': session_id})


class NoCaptchaVerifyView(APIView):
    """无感验证"""
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        session_id = request.data.get('session_id')
        user_data = request.data.get('user_data')
        
        if not session_id:
            return Response(
                {'error': '请提供会话ID'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        if NoCaptcha.verify(session_id, user_data):
            return Response({'valid': True})
        else:
            return Response(
                {'valid': False, 'error': '验证失败'},
                status=status.HTTP_400_BAD_REQUEST
            )
