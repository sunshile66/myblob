from django.contrib import admin
from django.utils import timezone
from .models import SecurityConfig, IPBlock, RequestLog, UserSession
from .models import EmailConfig, SmsConfig, VerificationLog


@admin.register(SecurityConfig)
class SecurityConfigAdmin(admin.ModelAdmin):
    """安全配置管理"""
    list_display = ['id', 'verification_code_enabled', 'verification_code_type', 
                    'verification_code_method', 'anti_spider_enabled', 
                    'sso_enabled', 'traffic_monitor_enabled']
    fieldsets = [
        ('验证码配置', {'fields': [
            'verification_code_enabled', 
            'verification_code_type',
            'verification_code_method',
            'verification_code_length', 
            'verification_code_expire_seconds',
            'verification_code_only_register',
        ]}),
        ('反爬虫配置', {'fields': ['anti_spider_enabled', 'request_limit_per_minute', 
                                     'request_limit_per_hour']}),
        ('单点登录配置', {'fields': ['sso_enabled', 'max_sessions']}),
        ('流量监控配置', {'fields': ['traffic_monitor_enabled', 'auto_block_enabled', 
                                       'block_duration_seconds']}),
    ]
    
    def has_add_permission(self, request):
        return not SecurityConfig.objects.exists()
    
    def has_delete_permission(self, request, obj=None):
        return False


@admin.register(EmailConfig)
class EmailConfigAdmin(admin.ModelAdmin):
    """邮箱配置管理"""
    list_display = ['provider', 'username', 'from_email', 'is_active']
    list_filter = ['provider', 'is_active']
    search_fields = ['username', 'from_email']
    fieldsets = [
        ('基本信息', {'fields': ['provider', 'is_active']}),
        ('📧 邮箱账号登录信息', {
            'fields': ['username', 'password'],
            'description': '• username: 登录邮箱的完整地址，例如：yourname@qq.com\n• password: 邮箱授权码（注意：不是邮箱登录密码！）'
        }),
        ('✉️ 发件人显示信息', {
            'fields': ['from_email'],
            'description': '• from_email: 收件人看到的发件人地址\n• 可以和 username 不同，例如：noreply@yourdomain.com\n• 如果留空，默认使用 username 作为发件人'
        }),
        ('⚙️ 自定义邮箱服务器（可选）', {
            'fields': ['host', 'port', 'use_tls', 'use_ssl'],
            'classes': ['collapse'],
            'description': '仅当 provider 选择「自定义」时需要填写\nQQ/163/Gmail/Outlook 等预定义服务商无需填写'
        }),
    ]
    actions = ['test_email']
    
    def test_email(self, request, queryset):
        """测试邮箱配置"""
        import smtplib
        from email.mime.text import MIMEText
        from email.utils import formataddr
        import logging
        
        logger = logging.getLogger(__name__)
        success_count = 0
        fail_count = 0
        
        provider_settings = {
            'qq': {'host': 'smtp.qq.com', 'port': 587, 'use_tls': True, 'use_ssl': False},
            '163': {'host': 'smtp.163.com', 'port': 587, 'use_tls': True, 'use_ssl': False},
            'gmail': {'host': 'smtp.gmail.com', 'port': 587, 'use_tls': True, 'use_ssl': False},
            'outlook': {'host': 'smtp.office365.com', 'port': 587, 'use_tls': True, 'use_ssl': False},
            'custom': None,
        }
        
        for config in queryset:
            try:
                logger.info(f"[测试邮件] 开始测试配置: {config.username}")
                
                ps = provider_settings.get(config.provider, None)
                
                if config.provider != 'custom' and ps:
                    host = config.host or ps['host']
                    port = config.port or ps['port']
                    use_tls = config.use_tls if config.use_tls is not None else ps['use_tls']
                    use_ssl = config.use_ssl if config.use_ssl is not None else ps['use_ssl']
                else:
                    host = config.host
                    port = config.port
                    use_tls = config.use_tls
                    use_ssl = config.use_ssl
                
                logger.info(f"[测试邮件] 配置 - host:{host}, port:{port}, use_tls:{use_tls}, use_ssl:{use_ssl}")
                
                subject = '✅ MyBlob 邮箱测试邮件'
                message = (
                    f'这是一封来自 MyBlob 管理后台的测试邮件。\n\n'
                    f'测试信息：\n'
                    f'• 邮箱账号：{config.username}\n'
                    f'• 发件人：{config.from_email or config.username}\n'
                    f'• 服务商：{config.get_provider_display()}\n'
                    f'• SMTP服务器：{host}:{port}\n\n'
                    f'如果您收到这封邮件，说明邮箱配置正常！🎉'
                )
                from_email = config.from_email or config.username
                to_email = config.username
                
                logger.info(f"[测试邮件] 使用 smtplib 直接发送到: {to_email}")
                
                msg = MIMEText(message, 'plain', 'utf-8')
                msg['From'] = formataddr(('MyBlob', from_email))
                msg['To'] = formataddr(('收件人', to_email))
                msg['Subject'] = subject
                
                server = None
                try:
                    if use_ssl:
                        server = smtplib.SMTP_SSL(host, port)
                    else:
                        server = smtplib.SMTP(host, port)
                        if use_tls:
                            server.starttls()
                    
                    server.login(config.username, config.password)
                    server.sendmail(from_email, [to_email], msg.as_string())
                    
                    logger.info(f"[测试邮件] smtplib 发送成功")
                    
                finally:
                    if server:
                        try:
                            server.quit()
                        except:
                            pass
                
                success_count += 1
                self.message_user(request, f'✅ 已发送测试邮件到 {to_email}，请查收！')
                logger.info(f"[测试邮件] 发送成功: {to_email}")
                
            except Exception as e:
                fail_count += 1
                error_msg = str(e)
                self.message_user(request, f'❌ 发送到 {config.username} 失败: {error_msg}', level='error')
                logger.error(f"[测试邮件] 发送失败: {config.username}, 错误: {error_msg}", exc_info=True)
        
        if success_count > 0:
            self.message_user(request, f'📧 共成功发送 {success_count} 封测试邮件')
        if fail_count > 0:
            self.message_user(request, f'⚠️ 共失败 {fail_count} 封', level='warning')
    test_email.short_description = '📧 发送测试邮件'


@admin.register(SmsConfig)
class SmsConfigAdmin(admin.ModelAdmin):
    """短信配置管理"""
    list_display = ['provider', 'sign_name', 'template_code', 'is_active', 
                    'daily_limit', 'hourly_limit', 'minute_limit']
    list_filter = ['provider', 'is_active']
    search_fields = ['sign_name', 'template_code']
    fieldsets = [
        ('基本信息', {'fields': ['provider', 'is_active']}),
        ('阿里云短信配置', {'fields': ['access_key_id', 'access_key_secret', 
                                         'sign_name', 'template_code']}),
        ('防盗刷配置（重要）', {
            'fields': ['daily_limit', 'hourly_limit', 'minute_limit', 
                       'same_ip_limit', 'same_receiver_limit'],
            'description': '设置发送限制，防止盗刷'
        }),
    ]


@admin.register(VerificationLog)
class VerificationLogAdmin(admin.ModelAdmin):
    """验证码发送记录管理"""
    list_display = ['receiver', 'code', 'code_type', 'status', 'ip_address', 
                    'created_at', 'sent_at']
    list_filter = ['code_type', 'status', 'created_at']
    search_fields = ['receiver', 'ip_address', 'code']
    readonly_fields = ['created_at', 'sent_at']
    date_hierarchy = 'created_at'
    actions = ['resend_selected']
    
    def has_add_permission(self, request):
        return False
    
    def has_change_permission(self, request, obj=None):
        return False
    
    def resend_selected(self, request, queryset):
        """重新发送选中的验证码"""
        from core.verification import send_verification_code
        count = 0
        for log in queryset:
            if send_verification_code(log.receiver, log.code_type):
                count += 1
        self.message_user(request, f'已重新发送 {count} 条验证码')
    resend_selected.short_description = '重新发送选中的验证码'


@admin.register(IPBlock)
class IPBlockAdmin(admin.ModelAdmin):
    """IP封禁管理"""
    list_display = ['ip_address', 'reason', 'blocked_at', 'blocked_until', 'is_active', 'is_expired']
    list_filter = ['is_active', 'blocked_at']
    search_fields = ['ip_address', 'reason']
    readonly_fields = ['blocked_at', 'blocked_by']
    actions = ['unblock_selected', 'extend_block']
    
    def is_expired(self, obj):
        return obj.is_expired()
    is_expired.boolean = True
    is_expired.short_description = '已过期'
    
    def unblock_selected(self, request, queryset):
        """解除选中的封禁"""
        count = queryset.update(is_active=False)
        self.message_user(request, f'已成功解除 {count} 个IP的封禁')
    unblock_selected.short_description = '解除选中的封禁'
    
    def extend_block(self, request, queryset):
        """延长封禁时间24小时"""
        for block in queryset:
            if block.is_active:
                block.blocked_until = timezone.now() + timezone.timedelta(hours=24)
                block.save()
        self.message_user(request, f'已成功延长 {queryset.count()} 个IP的封禁时间')
    extend_block.short_description = '延长封禁24小时'
    
    def save_model(self, request, obj, form, change):
        if not change:
            obj.blocked_by = request.user
        super().save_model(request, obj, form, change)


@admin.register(RequestLog)
class RequestLogAdmin(admin.ModelAdmin):
    """请求日志管理"""
    list_display = ['ip_address', 'path', 'method', 'status_code', 'response_time_ms', 
                    'user', 'created_at', 'is_spider']
    list_filter = ['method', 'status_code', 'is_spider', 'created_at']
    search_fields = ['ip_address', 'path', 'user__username']
    readonly_fields = ['created_at']
    date_hierarchy = 'created_at'
    
    def has_add_permission(self, request):
        return False
    
    def has_change_permission(self, request, obj=None):
        return False


@admin.register(UserSession)
class UserSessionAdmin(admin.ModelAdmin):
    """用户会话管理"""
    list_display = ['user', 'ip_address', 'created_at', 'last_activity', 'is_active']
    list_filter = ['is_active', 'created_at']
    search_fields = ['user__username', 'ip_address']
    readonly_fields = ['created_at', 'last_activity']
    actions = ['terminate_selected']
    
    def terminate_selected(self, request, queryset):
        """终止选中的会话"""
        count = queryset.update(is_active=False)
        self.message_user(request, f'已成功终止 {count} 个会话')
    terminate_selected.short_description = '终止选中的会话'
    
    def has_add_permission(self, request):
        return False
