from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import APIServiceViewSet, APIEndpointViewSet, APIKeyViewSet, APICallLogViewSet

router = DefaultRouter()
router.register(r'services', APIServiceViewSet, basename='apiservice')
router.register(r'endpoints', APIEndpointViewSet, basename='apiendpoint')
router.register(r'keys', APIKeyViewSet, basename='apikey')
router.register(r'logs', APICallLogViewSet, basename='apicalllog')

urlpatterns = [
    path('', include(router.urls)),
]
