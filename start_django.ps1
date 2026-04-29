# Django 服务器快速启动脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Django 服务器启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 进入项目目录
Set-Location "d:\code\Myblob"
Write-Host "✓ 进入项目目录：d:\code\Myblob" -ForegroundColor Green
Write-Host ""

# 激活虚拟环境
Write-Host "正在激活虚拟环境..." -ForegroundColor Yellow
& ".\.venv\Scripts\Activate.ps1"
Write-Host "✓ 虚拟环境已激活" -ForegroundColor Green
Write-Host ""

# 检查数据库连接
Write-Host "正在测试数据库连接..." -ForegroundColor Yellow
try {
    python manage.py check --database default
    Write-Host "✓ 数据库连接正常" -ForegroundColor Green
} catch {
    Write-Host "⚠ 数据库连接可能有问题，继续启动..." -ForegroundColor Yellow
}
Write-Host ""

# 启动 Django 服务器
Write-Host "正在启动 Django 服务器..." -ForegroundColor Yellow
Write-Host "访问地址：http://127.0.0.1:8000" -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止服务器" -ForegroundColor Gray
Write-Host ""

python manage.py runserver
