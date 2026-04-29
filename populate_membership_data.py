import os
import sys
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Myblob.settings")
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
django.setup()

from django.utils import timezone
from membership.models import MembershipPlan, MembershipBenefit, PlanBenefitRelation
from social.models import OAuthApp


def populate_membership_benefits():
    print("正在创建会员权益...")
    
    benefits = [
        {
            'name': '无限制发布',
            'code': 'unlimited_post',
            'benefit_type': 'feature',
            'description': '无限制发布文章和笔记',
            'icon': 'Edit',
        },
        {
            'name': '高清图片上传',
            'code': 'hd_image',
            'benefit_type': 'feature',
            'description': '支持高清图片上传',
            'icon': 'Picture',
        },
        {
            'name': '视频发布',
            'code': 'video_post',
            'benefit_type': 'feature',
            'description': '支持视频内容发布',
            'icon': 'VideoCamera',
        },
        {
            'name': '优先审核',
            'code': 'priority_review',
            'benefit_type': 'feature',
            'description': '内容优先审核',
            'icon': 'Timer',
        },
        {
            'name': '专属客服',
            'code': 'vip_service',
            'benefit_type': 'feature',
            'description': '专属客服支持',
            'icon': 'Service',
        },
        {
            'name': '每日发布配额',
            'code': 'daily_quota',
            'benefit_type': 'quota',
            'description': '每日可发布内容数量',
            'icon': 'Document',
        },
        {
            'name': '存储空间',
            'code': 'storage_space',
            'benefit_type': 'quota',
            'description': '可用存储空间',
            'icon': 'Folder',
        },
        {
            'name': '专属徽章',
            'code': 'vip_badge',
            'benefit_type': 'feature',
            'description': '显示VIP专属徽章',
            'icon': 'Medal',
        },
    ]
    
    created_count = 0
    for benefit_data in benefits:
        benefit, created = MembershipBenefit.objects.get_or_create(
            code=benefit_data['code'],
            defaults=benefit_data
        )
        if created:
            created_count += 1
            print(f"  已创建权益: {benefit.name}")
    
    print(f"权益创建完成，共创建 {created_count} 个新权益\n")
    return MembershipBenefit.objects.all()


def populate_membership_plans(benefits):
    print("正在创建会员套餐...")
    
    benefit_map = {b.code: b for b in benefits}
    
    plans = [
        {
            'name': '月度会员',
            'description': '适合短期使用的用户',
            'price': 29.00,
            'duration_days': 30,
            'features': ['无限制发布内容', '高清图片上传', '专属客服支持'],
            'is_active': True,
            'sort': 1,
            'is_popular': False,
            'benefit_codes': ['unlimited_post', 'hd_image', 'vip_service', 'daily_quota'],
            'benefit_values': {'daily_quota': '10篇/天'},
        },
        {
            'name': '季度会员',
            'description': '性价比之选，推荐购买',
            'price': 79.00,
            'duration_days': 90,
            'features': ['所有月度会员权益', '支持视频发布', '优先审核', '专属徽章'],
            'is_active': True,
            'sort': 2,
            'is_popular': True,
            'badge_text': '推荐',
            'benefit_codes': ['unlimited_post', 'hd_image', 'video_post', 'priority_review', 'vip_service', 'vip_badge', 'daily_quota', 'storage_space'],
            'benefit_values': {'daily_quota': '30篇/天', 'storage_space': '10GB'},
        },
        {
            'name': '年度会员',
            'description': '最优惠的长期方案',
            'price': 199.00,
            'duration_days': 365,
            'features': ['所有季度会员权益', '无限存储空间', '专属活动优先参与', '更多惊喜权益'],
            'is_active': True,
            'sort': 3,
            'is_popular': False,
            'benefit_codes': ['unlimited_post', 'hd_image', 'video_post', 'priority_review', 'vip_service', 'vip_badge', 'daily_quota', 'storage_space'],
            'benefit_values': {'daily_quota': '无限', 'storage_space': '无限'},
        },
        {
            'name': '永久会员',
            'description': '一次购买，终身享用',
            'price': 999.00,
            'duration_days': 0,
            'features': ['所有年度会员权益', '永久有效', '专属标识', '未来所有新功能'],
            'is_active': True,
            'sort': 4,
            'is_popular': False,
            'badge_text': '终身',
            'benefit_codes': ['unlimited_post', 'hd_image', 'video_post', 'priority_review', 'vip_service', 'vip_badge', 'daily_quota', 'storage_space'],
            'benefit_values': {'daily_quota': '无限', 'storage_space': '无限'},
        },
    ]
    
    created_count = 0
    for plan_data in plans:
        benefit_codes = plan_data.pop('benefit_codes', [])
        benefit_values = plan_data.pop('benefit_values', {})
        
        plan, created = MembershipPlan.objects.get_or_create(
            name=plan_data['name'],
            defaults=plan_data
        )
        
        if created:
            created_count += 1
            print(f"  已创建套餐: {plan.name} - ¥{plan.price}")
            
            for code in benefit_codes:
                if code in benefit_map:
                    PlanBenefitRelation.objects.create(
                        plan=plan,
                        benefit=benefit_map[code],
                        value=benefit_values.get(code, '')
                    )
    
    print(f"会员套餐创建完成，共创建 {created_count} 个新套餐\n")


def populate_oauth_apps():
    print("正在创建OAuth应用配置...")
    
    apps = [
        {
            'provider': 'wechat',
            'app_id': 'wx1234567890abcdef',
            'app_secret': 'test_secret_wechat',
            'redirect_uri': 'http://localhost:3000/auth/wechat/callback',
            'scope': 'snsapi_login',
            'is_active': True,
        },
        {
            'provider': 'qq',
            'app_id': '123456789',
            'app_secret': 'test_secret_qq',
            'redirect_uri': 'http://localhost:3000/auth/qq/callback',
            'scope': 'get_user_info',
            'is_active': True,
        },
        {
            'provider': 'github',
            'app_id': 'test_github_client_id',
            'app_secret': 'test_github_client_secret',
            'redirect_uri': 'http://localhost:3000/auth/github/callback',
            'scope': 'user:email',
            'is_active': True,
        },
        {
            'provider': 'weibo',
            'app_id': '1234567890',
            'app_secret': 'test_secret_weibo',
            'redirect_uri': 'http://localhost:3000/auth/weibo/callback',
            'scope': 'email',
            'is_active': True,
        },
    ]
    
    created_count = 0
    for app_data in apps:
        app, created = OAuthApp.objects.get_or_create(
            provider=app_data['provider'],
            defaults=app_data
        )
        if created:
            created_count += 1
            print(f"  已创建OAuth应用: {app.get_provider_display()}")
    
    print(f"OAuth应用创建完成，共创建 {created_count} 个新应用\n")


if __name__ == '__main__':
    print("=" * 60)
    print("开始填充会员和社交登录测试数据...")
    print("=" * 60)
    print()
    
    benefits = populate_membership_benefits()
    populate_membership_plans(benefits)
    populate_oauth_apps()
    
    print("=" * 60)
    print("测试数据填充完成！")
    print("=" * 60)
