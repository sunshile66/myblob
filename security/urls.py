from django.urls import path
from .views import (
    CaptchaImageView, CaptchaVerifyView,
    SliderCaptchaView, SliderCaptchaVerifyView,
    NoCaptchaSessionView, NoCaptchaVerifyView
)

urlpatterns = [
    path('captcha/image/', CaptchaImageView.as_view(), name='captcha-image'),
    path('captcha/verify/', CaptchaVerifyView.as_view(), name='captcha-verify'),
    path('slider-captcha/image/', SliderCaptchaView.as_view(), name='slider-captcha-image'),
    path('slider-captcha/verify/', SliderCaptchaVerifyView.as_view(), name='slider-captcha-verify'),
    path('nocaptcha/session/', NoCaptchaSessionView.as_view(), name='nocaptcha-session'),
    path('nocaptcha/verify/', NoCaptchaVerifyView.as_view(), name='nocaptcha-verify'),
]
