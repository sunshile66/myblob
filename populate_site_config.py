import os
import sys
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "Myblob.settings")
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
django.setup()

from core.models import SiteConfig


def populate_site_config():
    print("正在初始化网站配置...")
    
    configs = [
        {
            'key': 'site_name',
            'value': '我的个人网站',
            'description': '网站名称',
        },
        {
            'key': 'site_description',
            'value': '这是一个个人博客网站，分享技术、生活和思考',
            'description': '网站描述',
        },
        {
            'key': 'site_keywords',
            'value': '个人博客,技术分享,生活记录',
            'description': '网站关键词',
        },
        {
            'key': 'site_icp',
            'value': '京ICP备12345678号',
            'description': 'ICP备案号',
        },
        {
            'key': 'user_agreement',
            'value': '''# 用户协议

欢迎使用本网站！

## 1. 服务条款
本网站按照本协议的规定及其不时发布的操作规则提供服务。您在使用本网站提供的各项服务之前，应仔细阅读本协议。如您不同意本协议及/或随时对其的修改，您可以主动取消本网站提供的服务；您一旦使用本网站服务，即视为您已了解并完全同意本协议各项内容。

## 2. 用户账号
- 您需要注册并登录才能使用本网站的部分功能
- 您应妥善保管账号和密码
- 您对账号下的所有行为负责

## 3. 用户行为规范
- 不得发布违法、违规内容
- 不得侵犯他人合法权益
- 不得恶意攻击网站

## 4. 知识产权
本网站内容的知识产权归本网站所有。

## 5. 免责声明
本网站不对因使用本服务而产生的任何损失承担责任。

## 6. 协议修改
本网站有权随时修改本协议。

最后更新：2026年3月
''',
            'description': '用户协议',
        },
        {
            'key': 'privacy_policy',
            'value': '''# 隐私政策

我们非常重视您的隐私保护。

## 1. 信息收集
我们会收集您主动提供的信息，包括但不限于：
- 注册信息（用户名、邮箱等）
- 发布的内容
- 使用记录

## 2. 信息使用
我们会将收集的信息用于：
- 提供和改进服务
- 与您沟通
- 安全保障

## 3. 信息保护
我们采用合理的安全措施保护您的信息。

## 4. 信息共享
除法律法规要求外，我们不会向第三方共享您的个人信息。

## 5. Cookie使用
本网站使用Cookie来提升用户体验。

## 6. 您的权利
您可以访问、修改、删除您的个人信息。

## 7. 政策更新
我们可能会不时更新本隐私政策。

最后更新：2026年3月
''',
            'description': '隐私政策',
        },
    ]
    
    created_count = 0
    updated_count = 0
    
    for config_data in configs:
        config, created = SiteConfig.objects.get_or_create(
            key=config_data['key'],
            defaults={
                'value': config_data['value'],
                'description': config_data['description']
            }
        )
        
        if created:
            created_count += 1
            print(f"  已创建配置: {config.key}")
        else:
            updated_count += 1
            config.value = config_data['value']
            config.description = config_data['description']
            config.save()
            print(f"  已更新配置: {config.key}")
    
    print(f"\n网站配置初始化完成！")
    print(f"  新建配置: {created_count} 个")
    print(f"  更新配置: {updated_count} 个")


if __name__ == '__main__':
    print("=" * 60)
    print("开始初始化网站配置...")
    print("=" * 60)
    print()
    
    populate_site_config()
    
    print()
    print("=" * 60)
    print("网站配置初始化完成！")
    print("=" * 60)
