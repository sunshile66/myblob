from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/', include('accounts.urls')),
    path('api/blog/', include('blog.urls')),
    path('api/comments/', include('comments.urls')),
    path('api/interactions/', include('interactions.urls')),
    path('api/media/', include('mediaapp.urls')),
    path('api/core/', include('core.urls')),
    path('api/membership/', include('membership.urls')),
    path('api/payments/', include('payments.urls')),
    path('api/social/', include('social.urls')),
    path('api/security/', include('security.urls')),
    path('api/filemanager/', include('filemanager.urls')),
    path('api/apigateway/', include('apigateway.urls')),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)

