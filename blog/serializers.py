from rest_framework import serializers
from .models import Category, Tag, Post, PostRevision
from accounts.serializers import UserSerializer
from mediaapp.serializers import MediaAssetSerializer


class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = "__all__"


class TagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tag
        fields = "__all__"


class PostSerializer(serializers.ModelSerializer):
    author = UserSerializer(read_only=True)
    category = CategorySerializer(read_only=True)
    tags = TagSerializer(many=True, read_only=True)
    video = MediaAssetSerializer(read_only=True)
    is_liked = serializers.SerializerMethodField()
    is_favorited = serializers.SerializerMethodField()

    class Meta:
        model = Post
        fields = "__all__"
        read_only_fields = ["id", "author", "view_count", "comment_count", "like_count", "created_at", "updated_at"]
    
    def get_is_liked(self, obj):
        request = self.context.get("request")
        if request and request.user.is_authenticated:
            from interactions.models import PostLike
            return PostLike.objects.filter(user=request.user, post=obj).exists()
        return False
    
    def get_is_favorited(self, obj):
        request = self.context.get("request")
        if request and request.user.is_authenticated:
            from interactions.models import Favorite
            return Favorite.objects.filter(user=request.user, post=obj).exists()
        return False


class PostRevisionSerializer(serializers.ModelSerializer):
    class Meta:
        model = PostRevision
        fields = "__all__"
        read_only_fields = ["id", "created_at"]
