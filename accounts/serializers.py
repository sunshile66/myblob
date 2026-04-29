from rest_framework import serializers
from .models import User, UserProfile


class UserSerializer(serializers.ModelSerializer):
    bio = serializers.CharField(source='profile.bio', allow_blank=True, required=False)
    website = serializers.URLField(source='profile.website', allow_blank=True, required=False)
    company = serializers.CharField(source='profile.company', allow_blank=True, required=False)
    profession = serializers.CharField(source='profile.profession', allow_blank=True, required=False)
    location = serializers.CharField(source='profile.location', allow_blank=True, required=False)
    phone = serializers.CharField(source='profile.phone', allow_blank=True, required=False)
    wechat = serializers.CharField(source='profile.wechat', allow_blank=True, required=False)
    qq = serializers.CharField(source='profile.qq', allow_blank=True, required=False)
    weibo = serializers.CharField(source='profile.weibo', allow_blank=True, required=False)
    is_following = serializers.SerializerMethodField()
    follower_count = serializers.SerializerMethodField()
    following_count = serializers.SerializerMethodField()
    post_count = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = [
            "id", "username", "email", "nickname", "avatar", "is_email_verified",
            "bio", "website", "company", "profession", "location",
            "phone", "wechat", "qq", "weibo",
            "is_following", "follower_count", "following_count", "post_count"
        ]
        read_only_fields = ["id", "is_email_verified"]
    
    def get_is_following(self, obj):
        request = self.context.get("request")
        if request and request.user.is_authenticated:
            from .models import Follow
            return Follow.objects.filter(follower=request.user, following=obj).exists()
        return False
    
    def get_follower_count(self, obj):
        return obj.followers.count()
    
    def get_following_count(self, obj):
        return obj.following.count()
    
    def get_post_count(self, obj):
        return obj.posts.filter(status="published").count()

    def update(self, instance, validated_data):
        profile_data = validated_data.pop('profile', {})
        
        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()
        
        profile, created = UserProfile.objects.get_or_create(user=instance)
        for attr, value in profile_data.items():
            setattr(profile, attr, value)
        profile.save()
        
        return instance


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = "__all__"
        read_only_fields = ["id", "user"]


class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=6)

    class Meta:
        model = User
        fields = ["username", "email", "nickname", "password"]

    def create(self, validated_data):
        password = validated_data.pop("password")
        user = User.objects.create(**validated_data)
        user.set_password(password)
        user.save()
        UserProfile.objects.get_or_create(user=user)
        return user
