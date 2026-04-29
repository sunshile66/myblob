import random
import re
from django.utils import timezone
from django.core.cache import cache
from django.conf import settings
from django.core.mail import send_mail

try:
    from security.models import SecurityConfig, EmailConfig, SmsConfig, VerificationLog
except ImportError:
    SecurityConfig = None
    EmailConfig = None
    SmsConfig = None
    VerificationLog = None


def get_security_config():
    """获取安全配置"""
    if SecurityConfig:
        return SecurityConfig.get_config()
    return None


def get_email_config():
    """获取邮箱配置"""
    if EmailConfig:
        return EmailConfig.get_config()
    return None


def get_sms_config():
    """获取短信配置"""
    if SmsConfig:
        return SmsConfig.get_config()
    return None


def is_verification_code_enabled():
    """检查验证码是否启用"""
    config = get_security_config()
    if config:
        return config.verification_code_enabled
    return True


def generate_verification_code(length=None):
    """生成验证码"""
    config = get_security_config()
    if config and length is None:
        length = config.verification_code_length
    elif length is None:
        length = 6
    return ''.join([str(random.randint(0, 9)) for _ in range(length)])


def is_email(address):
    """判断是否是邮箱"""
    email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(email_pattern, address) is not None


def is_phone(number):
    """判断是否是手机号（中国手机号）"""
    phone_pattern = r'^1[3-9]\d{9}$'
    return re.match(phone_pattern, number) is not None


def check_rate_limit(receiver, code_type, ip_address=None):
    """检查发送频率限制（防盗刷）"""
    sms_config = get_sms_config()
    if not sms_config:
        return True, None
    
    now = timezone.now()
    today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
    hour_start = now.replace(minute=0, second=0, microsecond=0)
    minute_start = now.replace(second=0, microsecond=0)
    
    if VerificationLog:
        daily_count = VerificationLog.objects.filter(
            receiver=receiver,
            code_type=code_type,
            created_at__gte=today_start,
            status='success'
        ).count()
        
        if daily_count >= sms_config.daily_limit:
            return False, f'今日已达到发送上限 {sms_config.daily_limit} 次'
        
        hourly_count = VerificationLog.objects.filter(
            receiver=receiver,
            code_type=code_type,
            created_at__gte=hour_start,
            status='success'
        ).count()
        
        if hourly_count >= sms_config.hourly_limit:
            return False, f'每小时已达到发送上限 {sms_config.hourly_limit} 次'
        
        minute_count = VerificationLog.objects.filter(
            receiver=receiver,
            code_type=code_type,
            created_at__gte=minute_start,
            status='success'
        ).count()
        
        if minute_count >= sms_config.minute_limit:
            return False, f'每分钟已达到发送上限 {sms_config.minute_limit} 次'
        
        receiver_count = VerificationLog.objects.filter(
            receiver=receiver,
            code_type=code_type,
            created_at__gte=today_start,
            status='success'
        ).count()
        
        if receiver_count >= sms_config.same_receiver_limit:
            return False, f'同一接收者今日已达到发送上限 {sms_config.same_receiver_limit} 次'
        
        if ip_address:
            ip_count = VerificationLog.objects.filter(
                ip_address=ip_address,
                code_type=code_type,
                created_at__gte=today_start,
                status='success'
            ).count()
            
            if ip_count >= sms_config.same_ip_limit:
                return False, f'同一IP今日已达到发送上限 {sms_config.same_ip_limit} 次'
    
    return True, None


def send_email_verification_code(email, code):
    """发送邮箱验证码"""
    email_config = get_email_config()
    
    try:
        subject = '验证码 - MyBlob'
        message = f'您的验证码是：{code}\n请在5分钟内使用。'
        
        if email_config:
            from_email = email_config.from_email
            recipient_list = [email]
            
            import django
            original_settings = {}
            if email_config.host:
                original_settings['EMAIL_HOST'] = getattr(settings, 'EMAIL_HOST', '')
                settings.EMAIL_HOST = email_config.host
            if email_config.port:
                original_settings['EMAIL_PORT'] = getattr(settings, 'EMAIL_PORT', 587)
                settings.EMAIL_PORT = email_config.port
            if email_config.username:
                original_settings['EMAIL_HOST_USER'] = getattr(settings, 'EMAIL_HOST_USER', '')
                settings.EMAIL_HOST_USER = email_config.username
            if email_config.password:
                original_settings['EMAIL_HOST_PASSWORD'] = getattr(settings, 'EMAIL_HOST_PASSWORD', '')
                settings.EMAIL_HOST_PASSWORD = email_config.password
            if email_config.use_tls is not None:
                original_settings['EMAIL_USE_TLS'] = getattr(settings, 'EMAIL_USE_TLS', True)
                settings.EMAIL_USE_TLS = email_config.use_tls
            if email_config.use_ssl is not None:
                original_settings['EMAIL_USE_SSL'] = getattr(settings, 'EMAIL_USE_SSL', False)
                settings.EMAIL_USE_SSL = email_config.use_ssl
            
            send_mail(subject, message, from_email, recipient_list)
            
            for key, value in original_settings.items():
                setattr(settings, key, value)
        else:
            from_email = settings.DEFAULT_FROM_EMAIL
            recipient_list = [email]
            send_mail(subject, message, from_email, recipient_list)
        
        print(f"[邮件发送] 向 {email} 发送验证码: {code}")
        return True
    except Exception as e:
        print(f"[邮件发送失败] {str(e)}")
        return False


def send_sms_verification_code(phone, code):
    """发送短信验证码（阿里云）"""
    sms_config = get_sms_config()
    
    try:
        import json
        from aliyunsdkcore.client import AcsClient
        from aliyunsdkcore.request import CommonRequest
        
        access_key_id = ''
        access_key_secret = ''
        sign_name = 'MyBlob'
        template_code = ''
        
        if sms_config:
            access_key_id = sms_config.access_key_id
            access_key_secret = sms_config.access_key_secret
            sign_name = sms_config.sign_name
            template_code = sms_config.template_code
        
        if not all([access_key_id, access_key_secret, template_code]):
            print(f"[短信模拟] 向 {phone} 发送验证码: {code}（未配置阿里云）")
            return True
        
        client = AcsClient(access_key_id, access_key_secret, 'cn-hangzhou')
        
        request = CommonRequest()
        request.set_accept_format('json')
        request.set_domain('dysmsapi.aliyuncs.com')
        request.set_method('POST')
        request.set_protocol_type('https')
        request.set_version('2017-05-25')
        request.set_action_name('SendSms')
        
        request.add_query_param('PhoneNumbers', phone)
        request.add_query_param('SignName', sign_name)
        request.add_query_param('TemplateCode', template_code)
        request.add_query_param('TemplateParam', json.dumps({'code': code}))
        
        response = client.do_action_with_exception(request)
        response_json = json.loads(response.decode('utf-8'))
        
        if response_json.get('Code') == 'OK':
            print(f"[短信发送] 向 {phone} 发送验证码: {code}")
            return True
        else:
            print(f"[短信发送失败] {response_json}")
            return False
            
    except ImportError:
        print(f"[短信模拟] 向 {phone} 发送验证码: {code}（未安装阿里云SDK）")
        return True
    except Exception as e:
        print(f"[短信发送失败] {str(e)}")
        return False


def send_verification_code(phone_or_email, code_type=None, ip_address=None):
    """发送验证码"""
    if not is_verification_code_enabled():
        return True
    
    code = generate_verification_code()
    config = get_security_config()
    timeout = config.verification_code_expire_seconds if config else 300
    
    if code_type is None:
        if is_email(phone_or_email):
            code_type = 'email'
        elif is_phone(phone_or_email):
            code_type = 'sms'
        else:
            code_type = 'sms'
    
    rate_ok, rate_error = check_rate_limit(phone_or_email, code_type, ip_address)
    if not rate_ok:
        if VerificationLog:
            VerificationLog.objects.create(
                receiver=phone_or_email,
                code=code,
                code_type=code_type,
                status='failed',
                ip_address=ip_address,
                error_message=rate_error
            )
        print(f"[发送限制] {phone_or_email}: {rate_error}")
        return False
    
    log = None
    if VerificationLog:
        log = VerificationLog.objects.create(
            receiver=phone_or_email,
            code=code,
            code_type=code_type,
            status='pending',
            ip_address=ip_address
        )
    
    cache_key = f'verification_code:{code_type}:{phone_or_email}'
    cache.set(cache_key, code, timeout=timeout)
    
    success = False
    error_msg = ''
    
    try:
        if code_type == 'email' and is_email(phone_or_email):
            success = send_email_verification_code(phone_or_email, code)
        elif code_type == 'sms' and is_phone(phone_or_email):
            success = send_sms_verification_code(phone_or_email, code)
        else:
            print(f"[模拟发送] 向 {phone_or_email} 发送验证码: {code}")
            success = True
    except Exception as e:
        error_msg = str(e)
        print(f"[发送异常] {str(e)}")
    
    if log:
        log.status = 'success' if success else 'failed'
        log.sent_at = timezone.now()
        if error_msg:
            log.error_message = error_msg
        log.save()
    
    return success


def verify_verification_code(phone_or_email, code, code_type=None):
    """验证验证码"""
    if not is_verification_code_enabled():
        return True
    
    if code_type is None:
        if is_email(phone_or_email):
            code_type = 'email'
        elif is_phone(phone_or_email):
            code_type = 'sms'
        else:
            code_type = 'sms'
    
    cache_key = f'verification_code:{code_type}:{phone_or_email}'
    stored_code = cache.get(cache_key)
    
    if stored_code and stored_code == code:
        cache.delete(cache_key)
        return True
    
    return False
