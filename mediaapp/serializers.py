from rest_framework import serializers
from .models import MediaAsset
from accounts.serializers import UserSerializer


class MediaAssetSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)

    class Meta:
        model = MediaAsset
        fields = "__all__"
        read_only_fields = ["id", "user", "created_at"]
