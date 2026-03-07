import email
from django.db import models
from django.utils import timezone
from accounts.models import MyUser
"""
Created on Fri Oct 27 09:05:05 2017
留言板
"""

# Create your models here.

class Board(models.Model):
    id=models.AutoField(primary_key=True)
    name=models.CharField(max_length=20,verbose_name='留言板名称')
    email=models.EmailField(max_length=20,verbose_name='邮箱')
    content=models.TextField(verbose_name='内容')
    create_time=models.DateTimeField(default=timezone.now,verbose_name='创建时间')
    user=models.ForeignKey(MyUser,verbose_name='用户')

    def __str__(self):
        return self.email

    class Meta:
        verbose_name='留言板'
        verbose_name_plural=verbose_name