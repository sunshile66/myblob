from rest_framework import serializers
from .models import Comment, CommentLike, CommentReaction, Emoji, Sticker
from accounts.serializers import UserSerializer
from blog.serializers import PostSerializer


class EmojiSerializer(serializers.ModelSerializer):
    class Meta:
        model = Emoji
        fields = '__all__'


class StickerSerializer(serializers.ModelSerializer):
    class Meta:
        model = Sticker
        fields = '__all__'


class CommentReactionSerializer(serializers.ModelSerializer):
    class Meta:
        model = CommentReaction
        fields = '__all__'


class CommentSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    post = PostSerializer(read_only=True)
    children = serializers.SerializerMethodField()
    reactions = serializers.SerializerMethodField()
    is_liked = serializers.SerializerMethodField()
    my_reaction = serializers.SerializerMethodField()

    class Meta:
        model = Comment
        fields = "__all__"
        read_only_fields = ["id", "user", "is_deleted", "created_at", "like_count"]

    def get_children(self, obj):
        if obj.children.exists():
            return CommentSerializer(obj.children.all(), many=True).data
        return []
    
    def get_reactions(self, obj):
        reactions = {}
        for reaction in obj.reactions.all():
            emoji = reaction.emoji
            if emoji not in reactions:
                reactions[emoji] = 0
            reactions[emoji] += 1
        return reactions
    
    def get_is_liked(self, obj):
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            return obj.likes.filter(user=request.user).exists()
        return False
    
    def get_my_reaction(self, obj):
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            try:
                reaction = obj.reactions.get(user=request.user)
                return reaction.emoji
            except CommentReaction.DoesNotExist:
                return None
        return None


class CommentCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Comment
        fields = ["post", "content", "parent", "reply_to", "nickname", "email"]

