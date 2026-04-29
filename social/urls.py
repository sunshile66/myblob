from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import OAuthAppViewSet, SocialAccountViewSet, oauth_login, oauth_callback


router = DefaultRouter()
router.register(r'apps', OAuthAppViewSet, basename='oauth-app')
router.register(r'accounts', SocialAccountViewSet, basename='social-account')


urlpatterns = [
    path('', include(router.urls)),
    path('oauth/login/', oauth_login, name='oauth-login'),
    path('oauth/callback/', oauth_callback, name='oauth-callback'),
]
