import os
import django
from django.utils import timezone

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Myblob.settings")

import sys
sys.path.insert(0, r'd:\code\Myblob')

django.setup()

from core.models import Announcement

def populate_modal_announcement():
    print("正在创建弹窗公告...")
    
    announcement, created = Announcement.objects.get_or_create(
        title="📋 用户协议与隐私政策",
        defaults={
            'content': """欢迎使用我们的网站！

在使用本站服务之前，请您仔细阅读以下用户协议和隐私政策：

1. 用户协议
   - 您需要注册账号才能发布内容
   - 请遵守社区规范，发布优质内容
   - 尊重他人，禁止发布侵权内容

2. 隐私政策
   - 我们会保护您的个人信息安全
   - 您可以随时查看和修改个人资料
   - 我们不会向第三方泄露您的信息

点击"我已阅读"表示您同意以上条款。""",
            'announcement_type': 'modal',
            'is_active': True,
            'is_pinned': True,
            'sort': 1,
            'show_delay': 5,
            'auto_close': False,
            'auto_close_time': 30,
        }
    )
    
    if created:
        print(f"  创建弹窗公告: {announcement.title}")
    else:
        print(f"  弹窗公告已存在: {announcement.title}")
    
    print("弹窗公告创建完成！")

if __name__ == '__main__':
    populate_modal_announcement()
