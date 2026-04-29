from rest_framework import generics, permissions, viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db.models import Count, Q
from .models import Category, Tag, Post, PostRevision
from .serializers import CategorySerializer, TagSerializer, PostSerializer, PostRevisionSerializer


class CategoryViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Category.objects.filter(is_active=True).order_by("sort")
    serializer_class = CategorySerializer
    permission_classes = [permissions.AllowAny]


class TagViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer
    permission_classes = [permissions.AllowAny]


class PostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
    permission_classes = [permissions.AllowAny]
    lookup_field = "slug"

    def get_queryset(self):
        queryset = Post.objects.select_related("author", "category").prefetch_related("tags")
        
        if not self.request.user.is_authenticated:
            queryset = queryset.filter(status="published")
        
        category = self.request.query_params.get("category")
        if category:
            queryset = queryset.filter(category__slug=category)
        
        tag = self.request.query_params.get("tag")
        if tag:
            queryset = queryset.filter(tags__slug=tag)
        
        search = self.request.query_params.get("search")
        if search:
            queryset = queryset.filter(
                Q(title__icontains=search) | Q(content__icontains=search)
            )
        
        status_filter = self.request.query_params.get("status")
        if status_filter:
            queryset = queryset.filter(status=status_filter)
        
        ordering = self.request.query_params.get("ordering", "-published_at")
        queryset = queryset.order_by(ordering)
        
        return queryset

    def perform_create(self, serializer):
        serializer.save(author=self.request.user)

    @action(detail=True, methods=["post"], permission_classes=[permissions.IsAuthenticated])
    def like(self, request, slug=None):
        post = self.get_object()
        from interactions.models import PostLike
        
        post_like, created = PostLike.objects.get_or_create(user=request.user, post=post)
        if not created:
            return Response({"detail": "已经点赞过了"}, status=status.HTTP_400_BAD_REQUEST)
        
        post.like_count += 1
        post.save(update_fields=["like_count"])
        return Response({"detail": "点赞成功"})

    @like.mapping.delete
    def unlike(self, request, slug=None):
        post = self.get_object()
        from interactions.models import PostLike
        
        deleted, _ = PostLike.objects.filter(user=request.user, post=post).delete()
        if deleted:
            post.like_count -= 1
            post.save(update_fields=["like_count"])
            return Response({"detail": "取消点赞成功"})
        return Response({"detail": "还没有点赞"}, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=True, methods=["post"], permission_classes=[permissions.IsAuthenticated])
    def favorite(self, request, slug=None):
        post = self.get_object()
        from interactions.models import Favorite
        
        favorite, created = Favorite.objects.get_or_create(user=request.user, post=post)
        if not created:
            return Response({"detail": "已经收藏过了"}, status=status.HTTP_400_BAD_REQUEST)
        return Response({"detail": "收藏成功"})

    @favorite.mapping.delete
    def unfavorite(self, request, slug=None):
        post = self.get_object()
        from interactions.models import Favorite
        
        deleted, _ = Favorite.objects.filter(user=request.user, post=post).delete()
        if deleted:
            return Response({"detail": "取消收藏成功"})
        return Response({"detail": "还没有收藏"}, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=False, methods=["get"], permission_classes=[permissions.IsAuthenticated])
    def my_posts(self, request):
        queryset = Post.objects.filter(author=request.user).select_related("author", "category").prefetch_related("tags")
        
        status_filter = self.request.query_params.get("status")
        if status_filter:
            queryset = queryset.filter(status=status_filter)
        
        ordering = self.request.query_params.get("ordering", "-created_at")
        queryset = queryset.order_by(ordering)
        
        page = self.paginate_queryset(queryset)
        if page is not None:
            serializer = self.get_serializer(page, many=True)
            return self.get_paginated_response(serializer.data)
        
        serializer = self.get_serializer(queryset, many=True)
        return Response(serializer.data)

    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        instance.view_count += 1
        instance.save(update_fields=["view_count"])
        serializer = self.get_serializer(instance)
        return Response(serializer.data)
