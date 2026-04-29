from rest_framework import viewsets, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from .models import PostLike, Favorite, BoardMessage, Notification
from .serializers import (
    PostLikeSerializer,
    FavoriteSerializer,
    BoardMessageSerializer,
    NotificationSerializer
)


class PostLikeViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = PostLikeSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return PostLike.objects.filter(user=self.request.user)


class FavoriteViewSet(viewsets.ReadOnlyModelViewSet):
    serializer_class = FavoriteSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Favorite.objects.filter(user=self.request.user)


class BoardMessageViewSet(viewsets.ModelViewSet):
    queryset = BoardMessage.objects.filter(is_public=True)
    serializer_class = BoardMessageSerializer
    permission_classes = [permissions.AllowAny]

    def get_queryset(self):
        queryset = BoardMessage.objects.all()
        if not self.request.user.is_authenticated:
            queryset = queryset.filter(is_public=True)
        return queryset

    def perform_create(self, serializer):
        if self.request.user.is_authenticated:
            serializer.save(user=self.request.user)
        else:
            serializer.save()


class NotificationViewSet(viewsets.ModelViewSet):
    serializer_class = NotificationSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Notification.objects.filter(user=self.request.user)

    @action(detail=False, methods=["post"])
    def mark_all_read(self, request):
        self.get_queryset().update(is_read=True)
        return Response({"detail": "已全部标记为已读"})
