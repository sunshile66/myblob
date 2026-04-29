import time
from django.utils import timezone
from django.core.cache import cache
from django.http import JsonResponse
from django.conf import settings
from .models import SecurityConfig, IPBlock, RequestLog


class SpiderDetectionMiddleware:
    """爬虫检测中间件"""
    
    SPIDER_KEYWORDS = [
        'bot', 'crawler', 'spider', 'scraper', 'curl', 'wget',
        'python', 'java', 'perl', 'php', 'go', 'ruby', 'httpclient',
        'scrapy', 'phantomjs', 'selenium', 'puppeteer'
    ]
    
    def __init__(self, get_response):
        self.get_response = get_response
    
    def is_spider(self, user_agent):
        """检测是否是爬虫"""
        if not user_agent:
            return True
        user_agent_lower = user_agent.lower()
        for keyword in self.SPIDER_KEYWORDS:
            if keyword in user_agent_lower:
                return True
        return False
    
    def __call__(self, request):
        config = SecurityConfig.get_config()
        
        if not config.anti_spider_enabled:
            return self.get_response(request)
        
        user_agent = request.META.get('HTTP_USER_AGENT', '')
        
        if self.is_spider(user_agent):
            return JsonResponse(
                {'error': '检测到异常访问'},
                status=403
            )
        
        return self.get_response(request)


class RateLimitMiddleware:
    """速率限制中间件"""
    
    def __init__(self, get_response):
        self.get_response = get_response
    
    def get_client_ip(self, request):
        """获取客户端真实IP"""
        x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')
        if x_forwarded_for:
            ip = x_forwarded_for.split(',')[0].strip()
        else:
            ip = request.META.get('REMOTE_ADDR')
        return ip
    
    def check_ip_blocked(self, ip):
        """检查IP是否被封禁"""
        try:
            block = IPBlock.objects.get(ip_address=ip, is_active=True)
            if block.is_expired():
                block.is_active = False
                block.save()
                return False
            return True
        except IPBlock.DoesNotExist:
            return False
    
    def block_ip(self, ip, reason):
        """封禁IP"""
        config = SecurityConfig.get_config()
        if not config.auto_block_enabled:
            return
        
        IPBlock.objects.create(
            ip_address=ip,
            reason=reason,
            blocked_until=timezone.now() + timezone.timedelta(seconds=config.block_duration_seconds)
        )
    
    def check_rate_limit(self, ip):
        """检查速率限制"""
        config = SecurityConfig.get_config()
        
        now = int(time.time())
        minute_key = f'rate_limit:minute:{ip}:{now // 60}'
        hour_key = f'rate_limit:hour:{ip}:{now // 3600}'
        
        minute_count = cache.get(minute_key, 0)
        hour_count = cache.get(hour_key, 0)
        
        if minute_count >= config.request_limit_per_minute:
            return True, f'每分钟请求超过{config.request_limit_per_minute}次'
        
        if hour_count >= config.request_limit_per_hour:
            return True, f'每小时请求超过{config.request_limit_per_hour}次'
        
        cache.set(minute_key, minute_count + 1, 60)
        cache.set(hour_key, hour_count + 1, 3600)
        
        return False, None
    
    def __call__(self, request):
        config = SecurityConfig.get_config()
        
        if not config.anti_spider_enabled:
            return self.get_response(request)
        
        ip = self.get_client_ip(request)
        
        if self.check_ip_blocked(ip):
            return JsonResponse(
                {'error': '您的IP已被封禁，请稍后再试'},
                status=403
            )
        
        is_limited, reason = self.check_rate_limit(ip)
        if is_limited:
            self.block_ip(ip, reason)
            return JsonResponse(
                {'error': '请求过于频繁，请稍后再试'},
                status=429
            )
        
        return self.get_response(request)


class TrafficMonitorMiddleware:
    """流量监控中间件"""
    
    def __init__(self, get_response):
        self.get_response = get_response
    
    def get_client_ip(self, request):
        """获取客户端真实IP"""
        x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')
        if x_forwarded_for:
            ip = x_forwarded_for.split(',')[0].strip()
        else:
            ip = request.META.get('REMOTE_ADDR')
        return ip
    
    def __call__(self, request):
        config = SecurityConfig.get_config()
        
        start_time = time.time()
        response = self.get_response(request)
        response_time = int((time.time() - start_time) * 1000)
        
        if config.traffic_monitor_enabled:
            ip = self.get_client_ip(request)
            user_agent = request.META.get('HTTP_USER_AGENT', '')
            user = request.user if request.user.is_authenticated else None
            
            is_spider = any(
                keyword in user_agent.lower() 
                for keyword in ['bot', 'crawler', 'spider', 'scraper']
            )
            
            try:
                RequestLog.objects.create(
                    ip_address=ip,
                    user_agent=user_agent,
                    path=request.path,
                    method=request.method,
                    status_code=response.status_code,
                    response_time_ms=response_time,
                    user=user,
                    is_spider=is_spider
                )
            except Exception:
                pass
        
        return response


class SingleSignOnMiddleware:
    """单点登录中间件"""
    
    def __init__(self, get_response):
        self.get_response = get_response
    
    def __call__(self, request):
        config = SecurityConfig.get_config()
        
        if not config.sso_enabled or not request.user.is_authenticated:
            return self.get_response(request)
        
        from .models import UserSession
        
        session_key = request.session.session_key
        if not session_key:
            return self.get_response(request)
        
        ip = request.META.get('REMOTE_ADDR', '')
        user_agent = request.META.get('HTTP_USER_AGENT', '')
        
        user_sessions = UserSession.objects.filter(
            user=request.user,
            is_active=True
        ).order_by('-created_at')
        
        if user_sessions.count() >= config.max_sessions:
            for old_session in user_sessions[config.max_sessions - 1:]:
                old_session.is_active = False
                old_session.save()
        
        UserSession.objects.update_or_create(
            user=request.user,
            session_key=session_key,
            defaults={
                'ip_address': ip,
                'user_agent': user_agent,
                'is_active': True
            }
        )
        
        return self.get_response(request)
