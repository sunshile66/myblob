from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import OrderViewSet, WalletViewSet, WalletTransactionViewSet

router = DefaultRouter()
router.register(r'orders', OrderViewSet, basename='order')
router.register(r'wallet', WalletViewSet, basename='wallet')
router.register(r'transactions', WalletTransactionViewSet, basename='wallet-transaction')

urlpatterns = [
    path('', include(router.urls)),
]
