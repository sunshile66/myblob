from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db.models import Q
from .models import APIService, APIEndpoint, APIKey, APICallLog
from .serializers import (
    APIServiceSerializer, APIEndpointSerializer, 
    APIKeySerializer, APICallLogSerializer
)


class APIServiceViewSet(viewsets.ModelViewSet):
    serializer_class = APIServiceSerializer
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]

    def get_queryset(self):
        user = self.request.user
        if user.is_authenticated:
            return APIService.objects.filter(
                Q(is_public=True) | Q(created_by=user)
            ).distinct()
        return APIService.objects.filter(is_public=True, status='published')

    def perform_create(self, serializer):
        serializer.save(created_by=self.request.user)


class APIEndpointViewSet(viewsets.ModelViewSet):
    serializer_class = APIEndpointSerializer
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]

    def get_queryset(self):
        service_id = self.kwargs.get('service_pk')
        if service_id:
            return APIEndpoint.objects.filter(service_id=service_id, is_active=True)
        return APIEndpoint.objects.filter(is_active=True)

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [permissions.IsAuthenticated()]
        return [permissions.AllowAny()]


class APIKeyViewSet(viewsets.ModelViewSet):
    serializer_class = APIKeySerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return APIKey.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class APICallLogViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = APICallLogSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return APICallLog.objects.filter(api_key__user=self.request.user)
