from rest_framework.authentication import TokenAuthentication
from rest_framework.exceptions import AuthenticationFailed


class OptionalTokenAuthentication(TokenAuthentication):
    """
    可选的Token认证：
    - 如果token有效，则进行认证
    - 如果token无效或不存在，不抛出异常，继续让权限类处理
    """
    
    def authenticate(self, request):
        try:
            return super().authenticate(request)
        except AuthenticationFailed:
            return None
