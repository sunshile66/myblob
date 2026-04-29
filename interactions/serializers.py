from rest_framework import serializers
from .models import PostLike, Favorite, BoardMessage, Notification
from accounts.serializers import UserSerializer
from blog.serializers import PostSerializer


class PostLikeSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    post = PostSerializer(read_only=True)

    class Meta:
        model = PostLike
        fields = "__all__"
        read_only_fields = ["id", "user", "created_at"]


class FavoriteSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    post = PostSerializer(read_only=True)

    class Meta:
        model = Favorite
        fields = "__all__"
        read_only_fields = ["id", "user", "created_at"]


class BoardMessageSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)

    class Meta:
        model = BoardMessage
        fields = "__all__"
        read_only_fields = ["id", "user", "created_at"]


class NotificationSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)

    class Meta:
        model = Notification
        fields = "__all__"
        read_only_fields = ["id", "user", "created_at"]
