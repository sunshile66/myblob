from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.response import Response
from django.db.models import Count
from .models import Comment, CommentLike, CommentReaction, Emoji, Sticker
from .serializers import (
    CommentSerializer, 
    CommentCreateSerializer,
    EmojiSerializer,
    StickerSerializer,
    CommentReactionSerializer
)


class CommentViewSet(viewsets.ModelViewSet):
    queryset = Comment.objects.filter(is_deleted=False, is_approved=True)
    serializer_class = CommentSerializer
    permission_classes = [permissions.AllowAny]

    def get_serializer_class(self):
        if self.action in ["create"]:
            return CommentCreateSerializer
        return CommentSerializer
    
    def get_serializer_context(self):
        context = super().get_serializer_context()
        context['request'] = self.request
        return context

    def get_queryset(self):
        queryset = Comment.objects.filter(is_deleted=False)
        
        if not self.request.user.is_authenticated:
            queryset = queryset.filter(is_approved=True)
        
        post = self.request.query_params.get("post")
        if post:
            queryset = queryset.filter(post_id=post, parent__isnull=True)
        
        return queryset.select_related(
            "user", "post", "reply_to"
        ).prefetch_related(
            "children",
            "children__user",
            "likes",
            "reactions"
        )

    def perform_create(self, serializer):
        comment = serializer.save()
        if self.request.user.is_authenticated:
            comment.user = self.request.user
            comment.is_approved = True
            comment.save()
        
        post = comment.post
        post.comment_count = Comment.objects.filter(
            post=post, is_deleted=False, is_approved=True
        ).count()
        post.save(update_fields=["comment_count"])
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def like(self, request, pk=None):
        """点赞评论"""
        comment = self.get_object()
        like, created = CommentLike.objects.get_or_create(
            comment=comment,
            user=request.user
        )
        
        if created:
            comment.like_count += 1
            comment.save()
            return Response({'detail': '点赞成功', 'like_count': comment.like_count})
        else:
            return Response(
                {'error': '已经点赞过了'},
                status=status.HTTP_400_BAD_REQUEST
            )
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def unlike(self, request, pk=None):
        """取消点赞评论"""
        comment = self.get_object()
        deleted, _ = CommentLike.objects.filter(
            comment=comment,
            user=request.user
        ).delete()
        
        if deleted:
            comment.like_count -= 1
            comment.save()
            return Response({'detail': '取消点赞成功', 'like_count': comment.like_count})
        return Response(
            {'error': '还没有点赞'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def react(self, request, pk=None):
        """添加表情反应"""
        comment = self.get_object()
        emoji = request.data.get('emoji')
        
        if not emoji:
            return Response(
                {'error': '请提供表情'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        valid_emojis = [e[0] for e in CommentReaction.REACTION_EMOJIS]
        if emoji not in valid_emojis:
            return Response(
                {'error': '无效的表情'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        reaction, created = CommentReaction.objects.update_or_create(
            comment=comment,
            user=request.user,
            defaults={'emoji': emoji}
        )
        
        return Response({
            'detail': '表情反应成功',
            'reactions': self.get_reactions_dict(comment)
        })
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def unreact(self, request, pk=None):
        """取消表情反应"""
        comment = self.get_object()
        deleted, _ = CommentReaction.objects.filter(
            comment=comment,
            user=request.user
        ).delete()
        
        if deleted:
            return Response({
                'detail': '取消表情反应成功',
                'reactions': self.get_reactions_dict(comment)
            })
        return Response(
            {'error': '还没有表情反应'},
            status=status.HTTP_400_BAD_REQUEST
        )
    
    def get_reactions_dict(self, comment):
        reactions = {}
        for reaction in comment.reactions.all():
            emoji = reaction.emoji
            if emoji not in reactions:
                reactions[emoji] = 0
            reactions[emoji] += 1
        return reactions


class EmojiViewSet(viewsets.ReadOnlyModelViewSet):
    """表情视图"""
    serializer_class = EmojiSerializer
    permission_classes = [permissions.AllowAny]
    pagination_class = None
    
    def get_queryset(self):
        queryset = Emoji.objects.filter(is_active=True)
        category = self.request.query_params.get('category')
        if category:
            queryset = queryset.filter(category=category)
        return queryset


class StickerViewSet(viewsets.ReadOnlyModelViewSet):
    """贴图视图"""
    serializer_class = StickerSerializer
    permission_classes = [permissions.AllowAny]
    pagination_class = None
    
    def get_queryset(self):
        queryset = Sticker.objects.filter(is_active=True)
        category = self.request.query_params.get('category')
        if category:
            queryset = queryset.filter(category=category)
        is_gif = self.request.query_params.get('is_gif')
        if is_gif is not None:
            queryset = queryset.filter(is_gif=is_gif == 'true')
        return queryset


@api_view(['GET'])
@permission_classes([permissions.AllowAny])
def get_reaction_emojis(request):
    """获取可用的表情反应列表"""
    return Response({
        'emojis': CommentReaction.REACTION_EMOJIS
    })

