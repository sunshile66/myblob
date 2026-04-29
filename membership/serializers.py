from rest_framework import serializers
from .models import MembershipPlan, UserMembership, MembershipBenefit, PlanBenefitRelation


class MembershipBenefitSerializer(serializers.ModelSerializer):
    class Meta:
        model = MembershipBenefit
        fields = ['id', 'name', 'code', 'benefit_type', 'description', 'icon', 'is_active']


class PlanBenefitRelationSerializer(serializers.ModelSerializer):
    benefit = MembershipBenefitSerializer(read_only=True)

    class Meta:
        model = PlanBenefitRelation
        fields = ['id', 'benefit', 'value']


class MembershipPlanSerializer(serializers.ModelSerializer):
    benefit_relations = PlanBenefitRelationSerializer(many=True, read_only=True)

    class Meta:
        model = MembershipPlan
        fields = [
            'id', 'name', 'description', 'price', 'duration_days',
            'features', 'is_active', 'sort', 'is_popular', 'badge_text',
            'benefit_relations', 'created_at', 'updated_at'
        ]


class UserMembershipSerializer(serializers.ModelSerializer):
    plan = MembershipPlanSerializer(read_only=True)
    is_valid = serializers.BooleanField(read_only=True)
    remaining_days = serializers.SerializerMethodField()

    class Meta:
        model = UserMembership
        fields = [
            'id', 'user', 'plan', 'start_time', 'end_time',
            'is_active', 'is_lifetime', 'is_valid', 'remaining_days',
            'created_at', 'updated_at'
        ]
        read_only_fields = ['user']

    def get_remaining_days(self, obj):
        from django.utils import timezone
        if obj.is_lifetime:
            return -1
        if not obj.end_time:
            return 0
        remaining = (obj.end_time - timezone.now()).days
        return max(0, remaining)
