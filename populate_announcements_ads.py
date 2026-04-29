import os
import django
from django.utils import timezone
import random

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Myblob.settings")

import sys
sys.path.insert(0, r'd:\code\Myblob')

django.setup()

from core.models import Announcement, Ad

def populate_announcements():
    print("正在创建公告数据...")
    
    announcements = [
        {
            'title': '🎉 网站全新升级，更多功能等你来体验！',
            'content': '我们对网站进行了全面升级，新增了开发工具模块，包括图片编辑器、加密解密工具、编码解码工具、curl转requests等实用功能。快来体验吧！',
            'is_active': True,
            'is_pinned': True,
            'sort': 1,
        },
        {
            'title': '📚 博客文章分类优化',
            'content': '为了更好地组织内容，我们优化了文章分类系统。现在您可以更方便地浏览和搜索感兴趣的内容。',
            'is_active': True,
            'is_pinned': False,
            'sort': 2,
        },
        {
            'title': '👥 用户关注功能上线',
            'content': '现在您可以关注其他用户，第一时间获取他们发布的最新文章。快来关注你感兴趣的作者吧！',
            'is_active': True,
            'is_pinned': False,
            'sort': 3,
        },
    ]
    
    for data in announcements:
        announcement, created = Announcement.objects.get_or_create(
            title=data['title'],
            defaults=data
        )
        if created:
            print(f"  创建公告: {data['title']}")
        else:
            print(f"  公告已存在: {data['title']}")
    
    print(f"公告数据创建完成！共 {len(announcements)} 条公告。")

def populate_ads():
    print("正在创建广告数据...")
    
    ads = [
        {
            'title': '学习Python编程',
            'link': 'https://www.python.org',
            'position': 'sidebar',
            'is_active': True,
            'sort': 1,
        },
        {
            'title': '探索Django框架',
            'link': 'https://www.djangoproject.com',
            'position': 'sidebar',
            'is_active': True,
            'sort': 2,
        },
        {
            'title': 'Vue.js官方文档',
            'link': 'https://vuejs.org',
            'position': 'sidebar',
            'is_active': True,
            'sort': 3,
        },
    ]
    
    for data in ads:
        ad, created = Ad.objects.get_or_create(
            title=data['title'],
            defaults=data
        )
        if created:
            print(f"  创建广告: {data['title']}")
        else:
            print(f"  广告已存在: {data['title']}")
    
    print(f"广告数据创建完成！共 {len(ads)} 条广告。")

if __name__ == '__main__':
    populate_announcements()
    populate_ads()
    print("\n所有数据填充完成！")
