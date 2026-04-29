from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import MembershipPlanViewSet, UserMembershipViewSet

router = DefaultRouter()
router.register(r'plans', MembershipPlanViewSet, basename='membership-plan')
router.register(r'user', UserMembershipViewSet, basename='user-membership')

urlpatterns = [
    path('', include(router.urls)),
]
