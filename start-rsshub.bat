@echo off
REM ============================================
REM RSSHub Local Instance Startup Script
REM Port: 1200
REM Mode: dev (uses tsx to load routes from source)
REM ============================================

echo Starting RSSHub on port 1200 (dev mode)...

cd /d "%~dp0rsshub-instance"

if not exist "node_modules" (
    echo [RSSHub] Installing dependencies...
    call npm install --legacy-peer-deps
    if errorlevel 1 (
        echo [ERROR] Install failed!
        pause
        exit /b 1
    )
)

echo [RSSHub] Starting server...
REM === 代理配置 (让RSSHub可以访问YouTube/Twitter等被墙平台) ===
REM 修改下面的端口为你的代理软件端口: Clash=7890, V2Ray=10809, Shadowsocks=1080
set PROXY_HOST=http://127.0.0.1
set PROXY_PORT=7890
set PROXY_STRATEGY=on_retry
set NODE_ENV=dev
npx tsx lib/index.ts
pause
