from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CommentViewSet, EmojiViewSet, StickerViewSet, get_reaction_emojis


router = DefaultRouter()
router.register(r"comments", CommentViewSet)
router.register(r"emojis", EmojiViewSet, basename='emoji')
router.register(r"stickers", StickerViewSet, basename='sticker')


urlpatterns = [
    path("", include(router.urls)),
    path("reaction-emojis/", get_reaction_emojis, name="reaction-emojis"),
]
