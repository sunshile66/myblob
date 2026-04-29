from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    PostLikeViewSet,
    FavoriteViewSet,
    BoardMessageViewSet,
    NotificationViewSet
)


router = DefaultRouter()
router.register(r"post-likes", PostLikeViewSet, basename="postlike")
router.register(r"favorites", FavoriteViewSet, basename="favorite")
router.register(r"board-messages", BoardMessageViewSet, basename="boardmessage")
router.register(r"notifications", NotificationViewSet, basename="notification")


urlpatterns = [
    path("", include(router.urls)),
]
