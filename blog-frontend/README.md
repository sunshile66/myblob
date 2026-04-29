# Blog Frontend

基于 Vue3 + Vite + TypeScript + Element Plus 的博客前端项目

## 技术栈

- Vue 3
- Vite 5
- TypeScript 5
- Vue Router 4
- Pinia 2
- Element Plus 2
- Axios
- Markdown-it

## 项目结构

```
blog-frontend/
├── public/
├── src/
│   ├── api/              # API 接口层
│   │   ├── auth.ts       # 认证接口
│   │   ├── post.ts       # 文章接口
│   │   └── comment.ts    # 评论接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── Navbar.vue
│   │   ├── Footer.vue
│   │   ├── Sidebar.vue
│   │   ├── PostCard.vue
│   │   ├── CommentBox.vue
│   │   ├── CommentList.vue
│   │   └── CommentItem.vue
│   ├── layout/           # 布局组件
│   │   └── DefaultLayout.vue
│   ├── pages/            # 页面
│   │   ├── Home.vue
│   │   ├── PostDetail.vue
│   │   ├── Category.vue
│   │   ├── Tag.vue
│   │   ├── Search.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Profile.vue
│   │   ├── MyPosts.vue
│   │   └── Favorites.vue
│   ├── router/           # 路由配置
│   │   └── index.ts
│   ├── store/            # 状态管理
│   │   ├── user.ts
│   │   └── post.ts
│   ├── types/            # TypeScript 类型
│   │   └── index.ts
│   ├── utils/            # 工具函数
│   │   └── request.ts
│   ├── App.vue
│   ├── main.ts
│   └── vite-env.d.ts
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts
```

## 快速开始

### 安装依赖

```bash
cd blog-frontend
npm install
# 或使用 yarn
yarn install
# 或使用 pnpm
pnpm install
```

### 启动开发服务器

```bash
npm run dev
# 或
yarn dev
# 或
pnpm dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
# 或
yarn build
# 或
pnpm build
```

### 预览生产构建

```bash
npm run preview
# 或
yarn preview
# 或
pnpm preview
```

## 功能特性

- ✅ 用户认证（登录/注册）
- ✅ 文章列表和详情
- ✅ 分类和标签筛选
- ✅ 搜索功能
- ✅ 评论系统（支持树形评论）
- ✅ 点赞和收藏
- ✅ 个人中心
- ✅ Markdown 渲染
- ✅ 响应式设计

## 后端 API

后端使用 Django + Django REST Framework，API 接口配置在 `vite.config.ts` 中通过代理访问：

- `/api` → `http://127.0.0.1:8000/api`
- `/media` → `http://127.0.0.1:8000/media`
- `/static` → `http://127.0.0.1:8000/static`

确保后端服务已在 8000 端口运行。

## 开发说明

### 添加新页面

1. 在 `src/pages/` 中创建新页面组件
2. 在 `src/router/index.ts` 中添加路由配置

### 添加新 API 接口

1. 在 `src/api/` 中创建或修改接口文件
2. 使用封装好的 `request` 工具进行请求

### 状态管理

使用 Pinia 进行状态管理，store 文件在 `src/store/` 目录下。

## License

MIT
