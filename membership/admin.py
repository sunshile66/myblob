from django.contrib import admin
from .models import MembershipPlan, UserMembership, MembershipBenefit, PlanBenefitRelation


@admin.register(MembershipPlan)
class MembershipPlanAdmin(admin.ModelAdmin):
    list_display = ['name', 'price', 'duration_days', 'is_active', 'is_popular', 'sort', 'created_at']
    list_filter = ['is_active', 'is_popular']
    search_fields = ['name', 'description']
    list_editable = ['sort', 'is_active', 'is_popular']


@admin.register(UserMembership)
class UserMembershipAdmin(admin.ModelAdmin):
    list_display = ['user', 'plan', 'start_time', 'end_time', 'is_active', 'is_lifetime']
    list_filter = ['is_active', 'is_lifetime', 'plan']
    search_fields = ['user__username', 'user__email']
    raw_id_fields = ['user', 'plan']


@admin.register(MembershipBenefit)
class MembershipBenefitAdmin(admin.ModelAdmin):
    list_display = ['name', 'code', 'benefit_type', 'is_active', 'created_at']
    list_filter = ['benefit_type', 'is_active']
    search_fields = ['name', 'code', 'description']


@admin.register(PlanBenefitRelation)
class PlanBenefitRelationAdmin(admin.ModelAdmin):
    list_display = ['plan', 'benefit', 'value', 'created_at']
    list_filter = ['plan', 'benefit']
    search_fields = ['plan__name', 'benefit__name']
    raw_id_fields = ['plan', 'benefit']
