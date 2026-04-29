from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AnnouncementViewSet, AdViewSet, get_site_config, send_code


router = DefaultRouter()
router.register(r'announcements', AnnouncementViewSet, basename='announcement')
router.register(r'ads', AdViewSet, basename='ad')


urlpatterns = [
    path('', include(router.urls)),
    path('site-config/', get_site_config, name='site-config'),
    path('send-code/', send_code, name='send-code'),
]
