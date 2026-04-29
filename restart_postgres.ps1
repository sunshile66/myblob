# PostgreSQL 数据库连接数优化脚本
# 以管理员身份运行此脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PostgreSQL 连接数优化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "错误：需要管理员权限" -ForegroundColor Red
    Write-Host "请右键点击脚本，选择'以管理员身份运行'" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ 管理员权限检查通过" -ForegroundColor Green
Write-Host ""

# 1. 查找 PostgreSQL 服务
Write-Host "正在查找 PostgreSQL 服务..." -ForegroundColor Yellow
$postgresService = Get-Service -Name "*postgres*" -ErrorAction SilentlyContinue

if ($null -eq $postgresService) {
    Write-Host "未找到 PostgreSQL 服务" -ForegroundColor Red
    exit 1
}

Write-Host "找到服务：$($postgresService.Name) - $($postgresService.Status)" -ForegroundColor Green
Write-Host ""

# 2. 显示当前连接信息
Write-Host "当前 PostgreSQL 服务状态：" -ForegroundColor Cyan
Write-Host "  服务名：$($postgresService.Name)"
Write-Host "  状态：$($postgresService.Status)"
Write-Host "  显示名：$($postgresService.DisplayName)"
Write-Host ""

# 3. 重启服务
Write-Host "正在重启 PostgreSQL 服务..." -ForegroundColor Yellow
try {
    Write-Host "  [1/3] 停止服务..." -ForegroundColor Yellow
    Stop-Service -Name $postgresService.Name -Force -ErrorAction Stop
    Write-Host "  ✓ 服务已停止" -ForegroundColor Green
    
    Write-Host "  [2/3] 等待 3 秒..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3
    
    Write-Host "  [3/3] 启动服务..." -ForegroundColor Yellow
    Start-Service -Name $postgresService.Name -ErrorAction Stop
    Write-Host "  ✓ 服务已启动" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "PostgreSQL 服务重启成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "现在可以：" -ForegroundColor Cyan
    Write-Host "  1. 重启 Django 服务器" -ForegroundColor White
    Write-Host "  2. 测试网站是否正常工作" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "错误：无法重启 PostgreSQL 服务" -ForegroundColor Red
    Write-Host "详细信息：$($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试手动重启：" -ForegroundColor Yellow
    Write-Host "  1. 打开'服务'管理器 (services.msc)" -ForegroundColor White
    Write-Host "  2. 找到 '$($postgresService.Name)'" -ForegroundColor White
    Write-Host "  3. 右键 -> 重新启动" -ForegroundColor White
    exit 1
}

# 4. 验证服务状态
$finalStatus = Get-Service -Name $postgresService.Name
if ($finalStatus.Status -eq "Running") {
    Write-Host "✓ 服务运行正常" -ForegroundColor Green
} else {
    Write-Host "⚠ 服务未运行，请检查日志" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "脚本执行完成！" -ForegroundColor Green
Write-Host "按任意键退出..." -ForegroundColor Gray
try {
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
} catch {
    # 忽略按键错误，直接退出
}
