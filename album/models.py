from django.db import models
from account.models import MyUser

# Create your models here.
class AlbumInfo(models.Model):
    id = models.AutoField(primary_key=True)
    user = models.ForeignKey(MyUser, on_delete=models.CASCADE,verbose_name='用户')
    title = models.CharField(max_length=50,verbose_name='标题',blank=True)
    introduce = models.TextField(verbose_name='描述',max_length=200,blank=True)
    photo = models.ImageField(upload_to='images/album/',verbose_name='图片',blank=True)

    def __str__(self):
        return self.id
    class Meta:
        verbose_name = '图片墙管理'
        verbose_name_plural = '图片墙管理'

