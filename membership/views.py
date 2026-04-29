from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.utils import timezone
from .models import MembershipPlan, UserMembership, MembershipBenefit
from .serializers import (
    MembershipPlanSerializer, UserMembershipSerializer, MembershipBenefitSerializer
)


class MembershipPlanViewSet(viewsets.ReadOnlyModelViewSet):
    """会员套餐视图"""
    queryset = MembershipPlan.objects.filter(is_active=True).order_by('sort', '-created_at')
    serializer_class = MembershipPlanSerializer
    permission_classes = [permissions.AllowAny]
    pagination_class = None


class UserMembershipViewSet(viewsets.ReadOnlyModelViewSet):
    """用户会员视图"""
    serializer_class = UserMembershipSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return UserMembership.objects.filter(user=self.request.user)

    @action(detail=False, methods=['get'])
    def my_membership(self, request):
        """获取当前用户的会员信息"""
        membership, created = UserMembership.objects.get_or_create(user=request.user)
        serializer = self.get_serializer(membership)
        return Response(serializer.data)
