# Myblob - 个人博客系统

一个现代化的个人博客系统，采用 Django + Vue3 技术栈，支持小红书风格的笔记发布和管理。

## 技术栈

### 后端
- Django 6.0
- Django REST Framework
- PostgreSQL/MySQL/SQLite
- Redis (缓存)
- Celery (异步任务)

### 前端
- Vue 3
- Vite
- TypeScript
- Pinia (状态管理)
- Vue Router
- Element Plus
- Axios

## 项目结构

```
Myblob/
├── Myblob/                 # Django 项目配置
│   ├── settings/          # 配置文件（dev/prod 分离）
│   ├── urls.py            # 主路由
│   └── ...
├── accounts/              # 用户认证应用
├── blog/                  # 博客核心应用
├── comments/              # 评论系统
├── interactions/          # 互动系统（点赞、收藏、通知）
├── mediaapp/              # 媒体资源管理
├── core/                  # 核心工具
├── blog-frontend/         # Vue3 前端项目
│   ├── src/
│   │   ├── api/          # API 接口
│   │   ├── components/   # 公共组件
│   │   ├── pages/        # 页面组件
│   │   ├── router/       # 路由配置
│   │   ├── store/        # 状态管理
│   │   ├── types/        # TypeScript 类型
│   │   └── utils/        # 工具函数
│   └── ...
├── scripts/               # 脚本文件
├── logs/                  # 日志目录
└── manage.py
```

## 快速开始

### 环境要求
- Python 3.10+
- Node.js 18+
- npm/yarn/pnpm

### 后端启动

1. 创建虚拟环境并激活
```bash
python -m venv .venv
.venv\Scripts\activate  # Windows
source .venv/bin/activate  # Linux/Mac
```

2. 安装依赖
```bash
pip install -r requirements.txt
```

3. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，配置数据库等信息
```

4. 数据库迁移
```bash
python manage.py makemigrations
python manage.py migrate
```

5. 创建超级用户
```bash
python manage.py createsuperuser
```

6. 填充测试数据
```bash
python scripts/populate_test_data.py
```

7. 启动开发服务器
```bash
python manage.py runserver
```

### 前端启动

1. 进入前端目录
```bash
cd blog-frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

4. 构建生产版本
```bash
npm run build
```

## 功能特性

### 用户系统
- 用户注册/登录
- 个人资料编辑
- 头像上传
- 邮箱验证

### 博客系统
- 文章/笔记发布
- 分类管理
- 标签管理
- 草稿/发布状态
- 富文本编辑
- 图片上传

### 互动系统
- 评论系统（支持嵌套）
- 点赞功能
- 收藏功能
- 通知系统

### 性能优化
- Redis 缓存
- 图片懒加载
- 图片压缩
- 请求缓存

## 测试账号

系统默认提供以下测试账号（密码均为：`test123456`）：
- admin (管理员)
- zhangsan (张三)
- lisi (李四)
- wangwu (王五)
- zhaoliu (赵六)
- sunqi (孙七)
- zhouba (周八)
- wujiu (吴九)

## 开发指南

### 后端开发
- API 文档：访问 `http://localhost:8000/api/`
- Admin 后台：访问 `http://localhost:8000/admin/`

### 前端开发
- 开发服务器：`http://localhost:3000/`
- API 代理已配置，请求 `/api` 会自动转发到后端

## 许可证

MIT License
