from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import RegisterView, LoginView, LogoutView, ProfileView, AvatarUploadView, UserViewSet, SendVerificationCodeView, SecurityConfigView


router = DefaultRouter()
router.register(r"users", UserViewSet, basename="user")

urlpatterns = [
    path("", include(router.urls)),
    path("register/", RegisterView.as_view(), name="register"),
    path("login/", LoginView.as_view(), name="login"),
    path("logout/", LogoutView.as_view(), name="logout"),
    path("profile/", ProfileView.as_view(), name="profile"),
    path("avatar/", AvatarUploadView.as_view(), name="avatar-upload"),
    path("send-verification-code/", SendVerificationCodeView.as_view(), name="send-verification-code"),
    path("security-config/", SecurityConfigView.as_view(), name="security-config"),
]
