from django.db import models
from django.contrib.auth.models import AbstractUser


# Create your models here.

class MyUser(AbstractUser):
    """
    自定义用户模型类
    """
    name = models.CharField(max_length=20, verbose_name='姓名', default="匿名用户")
    introduce = models.TextField(verbose_name='简介', default="")
    company = models.CharField(max_length=100, verbose_name='公司', default="")
    profession = models.CharField(max_length=100, verbose_name='职业', default="")
    address = models.CharField(max_length=100, verbose_name='地址', default="")
    telephone = models.CharField(max_length=11, verbose_name='手机号', default="")
    wx = models.CharField(max_length=100, verbose_name='微信号', default="")
    qq = models.CharField(max_length=100, verbose_name='QQ号', default="")
    wb = models.CharField(max_length=100, verbose_name='微博号', default="")
    photo = models.ImageField(upload_to='images/user/', verbose_name='头像', default="")

    class Meta:
        verbose_name = '用户'
        verbose_name_plural = verbose_name
        db_table = 'user'

    def __str__(self):
        return self.name


