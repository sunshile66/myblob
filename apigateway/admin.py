from django.contrib import admin
from .models import APIService, APIEndpoint, APIKey, APICallLog


class APIEndpointInline(admin.TabularInline):
    model = APIEndpoint
    extra = 1
    fields = ('path', 'method', 'summary', 'is_active')


@admin.register(APIService)
class APIServiceAdmin(admin.ModelAdmin):
    list_display = ('name', 'slug', 'category', 'status', 'version', 'is_public', 'is_paid', 'created_by', 'created_time')
    list_filter = ('category', 'status', 'is_public', 'is_paid', 'created_time')
    search_fields = ('name', 'slug', 'description', 'created_by__username')
    list_select_related = ('created_by',)
    readonly_fields = ('created_time', 'updated_time')
    inlines = [APIEndpointInline]
    prepopulated_fields = {'slug': ('name',)}


@admin.register(APIEndpoint)
class APIEndpointAdmin(admin.ModelAdmin):
    list_display = ('service', 'method', 'path', 'summary', 'is_active', 'created_time')
    list_filter = ('method', 'is_active', 'created_time')
    search_fields = ('path', 'summary', 'service__name')
    list_select_related = ('service',)
    readonly_fields = ('created_time',)


@admin.register(APIKey)
class APIKeyAdmin(admin.ModelAdmin):
    list_display = ('user', 'name', 'key', 'is_active', 'rate_limit_per_minute', 'rate_limit_per_day', 'created_time', 'expires_time')
    list_filter = ('is_active', 'created_time', 'expires_time')
    search_fields = ('name', 'key', 'user__username')
    list_select_related = ('user',)
    readonly_fields = ('key', 'created_time')


@admin.register(APICallLog)
class APICallLogAdmin(admin.ModelAdmin):
    list_display = ('service', 'endpoint', 'api_key', 'request_method', 'response_status', 'execution_time', 'call_time')
    list_filter = ('request_method', 'response_status', 'call_time')
    search_fields = ('service__name', 'request_url', 'api_key__name')
    list_select_related = ('service', 'endpoint', 'api_key')
    readonly_fields = ('call_time',)
    date_hierarchy = 'call_time'
