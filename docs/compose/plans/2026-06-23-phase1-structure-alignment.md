# Phase 1: 项目结构对齐 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 前后端模块 1:1 对齐，新增功能只需"加一个模块目录"

**Architecture:** 前端 API/pages/router 按后端 module 名称统一命名，components 按业务模块组织

**Tech Stack:** Vue 3 + TypeScript + Vue Router + Pinia

---

## 文件变更总览

### 前端 API 文件重命名

| 当前文件 | 目标文件 | 操作 |
|----------|----------|------|
| `src/api/auth.ts` | `src/api/accounts.ts` | 重命名 + 合并 user.ts |
| `src/api/user.ts` | - | 合并到 accounts.ts 后删除 |
| `src/api/post.ts` | `src/api/blog.ts` | 重命名 |
| `src/api/comment.ts` | `src/api/comments.ts` | 重命名 |
| `src/api/notification.ts` | `src/api/interactions.ts` | 重命名 + 扩展 |
| `src/api/news.ts` | `src/api/news.ts` | 保留 + 合并 newsAdmin.ts |
| `src/api/newsAdmin.ts` | - | 合并到 news.ts 后删除 |
| `src/api/core.ts` | `src/api/core.ts` | 保留 |
| `src/api/knowledge.ts` | `src/api/knowledge.ts` | 保留 |
| `src/api/membership.ts` | `src/api/membership.ts` | 保留 |
| `src/api/payments.ts` | `src/api/payments.ts` | 保留 |
| - | `src/api/filemanager.ts` | 新建 |
| - | `src/api/flight.ts` | 新建（从 news.ts 拆出） |
| - | `src/api/apigateway.ts` | 新建 |
| - | `src/api/media.ts` | 新建 |
| - | `src/api/security.ts` | 新建 |

### 前端 pages 目录重组

| 当前位置 | 目标位置 |
|----------|----------|
| `pages/auth/*` | `pages/accounts/*` |
| `pages/user/*` | `pages/accounts/*` |
| `pages/post/*` | `pages/blog/*` |
| `pages/note/*` | `pages/blog/*` |
| `pages/CategoryPage.vue` | `pages/blog/CategoryPage.vue` |
| `pages/TagPage.vue` | `pages/blog/TagPage.vue` |
| `pages/SearchPage.vue` | `pages/search/SearchPage.vue` |
| `pages/PhotoWallPage.vue` | `pages/media/PhotoWallPage.vue` |
| `pages/interaction/*` | `pages/interactions/*` |
| `pages/flights/*` | `pages/flight/*` |
| `pages/settings/ThemeSettings.vue` | `pages/accounts/ThemeSettings.vue` |
| `pages/admin/SecurityIPBlocksPage.vue` | `pages/admin/security/IPBlocksPage.vue` |
| `pages/admin/SecurityRequestLogsPage.vue` | `pages/admin/security/RequestLogsPage.vue` |
| `pages/admin/UserListPage.vue` | `pages/admin/accounts/UserListPage.vue` |
| `pages/admin/SiteConfigPage.vue` | `pages/admin/core/SiteConfigPage.vue` |

### 前端 router/modules 重组

| 当前文件 | 目标文件 | 操作 |
|----------|----------|------|
| `content.ts` | - | 拆分到 blog.ts, knowledge.ts, interactions.ts |
| `user.ts` | `accounts.ts` | 重命名 + 调整 |
| `services.ts` | - | 拆分到 news.ts, filemanager.ts, membership.ts, payments.ts, flight.ts, apigateway.ts |
| `tools.ts` | `tools.ts` | 保留 |
| `legal.ts` | `legal.ts` | 保留 |
| - | `admin.ts` | 新建（聚合管理后台路由） |
| - | `search.ts` | 新建 |
| - | `media.ts` | 新建 |

---

## Task 1: 创建缺失的 API 文件

**Files:**
- Create: `src/api/filemanager.ts`
- Create: `src/api/flight.ts`
- Create: `src/api/apigateway.ts`
- Create: `src/api/media.ts`
- Create: `src/api/security.ts`

- [ ] **Step 1: 创建 filemanager.ts**

```typescript
import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface FileItem {
  id: number
  filename: string
  file: string
  file_type: string
  file_size: number
  mime_type: string
  is_public: boolean
  folder_id: number | null
  created_at: string
}

export interface Folder {
  id: number
  name: string
  parent_id: number | null
  created_at: string
}

export const getMyFiles = (params?: { folder_id?: number; page?: number; size?: number }) =>
  request.get<PaginatedResponse<FileItem>>('/filemanager/files/', { params })

export const uploadFile = (formData: FormData, folderId?: number) => {
  if (folderId) formData.append('folder_id', String(folderId))
  return request.post<FileItem>('/filemanager/files/upload/', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const deleteFile = (id: number) =>
  request.delete(`/filemanager/files/${id}/`)

export const downloadFile = (id: number) =>
  request.get(`/filemanager/files/${id}/download/`, { responseType: 'blob' })

export const createShareLink = (fileId: number, expiresIn?: number) =>
  request.post<{ link: string; expires_at: string }>('/filemanager/files/share/', { file_id: fileId, expires_in: expiresIn })

export const getFolders = (parentId?: number) =>
  request.get<Folder[]>('/filemanager/folders/', { params: { parent_id: parentId } })

export const createFolder = (name: string, parentId?: number) =>
  request.post<Folder>('/filemanager/folders/', { name, parent_id: parentId })

export const deleteFolder = (id: number) =>
  request.delete(`/filemanager/folders/${id}/`)
```

- [ ] **Step 2: 创建 flight.ts**

```typescript
import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface FlightRoute {
  id: number
  callsign?: string
  flightNumber?: string
  airline?: string
  airlineCode?: string
  originAirport?: string
  destinationAirport?: string
  departureTime?: string
  arrivalTime?: string
  status?: string
  changeType?: string
  altitude?: number
  velocity?: number
  latitude?: number
  longitude?: number
  country?: string
  lastSeen?: string
  createdAt: string
  updatedAt: string
}

export const getFlights = (params?: { page?: number; size?: number; airline?: string; changeType?: string }) =>
  request.get<PaginatedResponse<FlightRoute>>('/flights/', { params })

export const getFlightChanges = (params?: { size?: number }) =>
  request.get<FlightRoute[]>('/flights/changes/', { params })

export const getFlightAirlines = () =>
  request.get<string[]>('/flights/airlines/')

export const triggerFlightFetch = () =>
  request.post<number>('/flights/fetch/')
```

- [ ] **Step 3: 创建 apigateway.ts**

```typescript
import request from '@/utils/request'

export interface APIService {
  id: number
  name: string
  code: string
  description: string
  base_url: string
  timeout: number
  is_active: boolean
}

export interface APIKey {
  id: number
  api_key: string
  name: string
  description: string
  daily_limit: number
  used_count: number
  is_active: boolean
  expired_time: string
  created_at: string
}

export const getServices = () =>
  request.get<APIService[]>('/apigateway/services/')

export const createService = (data: Partial<APIService>) =>
  request.post<APIService>('/apigateway/services/', data)

export const getEndpoints = (serviceId: number) =>
  request.get(`/apigateway/services/${serviceId}/endpoints/`)

export const getMyAPIKeys = () =>
  request.get<APIKey[]>('/apigateway/keys/')

export const createAPIKey = (name: string, description?: string) =>
  request.post<APIKey>('/apigateway/keys/', { name, description })

export const revokeAPIKey = (keyId: number) =>
  request.delete(`/apigateway/keys/${keyId}/`)
```

- [ ] **Step 4: 创建 media.ts**

```typescript
import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface MediaAsset {
  id: number
  file: string
  title: string
  alt_text: string
  media_type: 'image' | 'video' | 'audio'
  file_size: number
  created_at: string
}

export const getMediaAssets = (params?: { page?: number; size?: number; type?: string }) =>
  request.get<PaginatedResponse<MediaAsset>>('/media/assets/', { params })

export const uploadMedia = (formData: FormData) =>
  request.post<MediaAsset>('/media/assets/', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

export const deleteMedia = (id: number) =>
  request.delete(`/media/assets/${id}/`)
```

- [ ] **Step 5: 创建 security.ts**

```typescript
import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface IPBlock {
  id: number
  ip: string
  reason: string
  blocked_until: string | null
  is_active: boolean
  created_at: string
}

export interface RequestLog {
  id: number
  user_id: number | null
  ip_address: string
  method: string
  path: string
  status_code: number
  user_agent: string
  response_time: number
  blocked: boolean
  created_at: string
}

export interface UserSession {
  id: number
  session_key: string
  ip_address: string
  user_agent: string
  is_active: boolean
  last_activity: string
  expired_time: string
  created_at: string
}

export const getBlockedIPs = (params?: { page?: number; size?: number }) =>
  request.get<PaginatedResponse<IPBlock>>('/security/blocked-ips/', { params })

export const blockIP = (ip: string, reason: string) =>
  request.post<IPBlock>('/security/blocked-ips/', { ip, reason })

export const unblockIP = (id: number) =>
  request.delete(`/security/blocked-ips/${id}/`)

export const getRequestLogs = (params?: { page?: number; size?: number }) =>
  request.get<PaginatedResponse<RequestLog>>('/security/request-logs/', { params })

export const getMySessions = () =>
  request.get<UserSession[]>('/security/sessions/')

export const deactivateSession = (id: number) =>
  request.post(`/security/sessions/${id}/deactivate/`)
```

- [ ] **Step 6: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add src/api/filemanager.ts src/api/flight.ts src/api/apigateway.ts src/api/media.ts src/api/security.ts
git commit -feat: add missing API files for filemanager, flight, apigateway, media, security modules
```

---

## Task 2: 重命名 API 文件（post.ts → blog.ts）

**Covers:** 前后端模块对齐

**Files:**
- Rename: `src/api/post.ts` → `src/api/blog.ts`
- Modify: 所有引用 post.ts 的文件

- [ ] **Step 1: 重命名文件**

```bash
mv src/api/post.ts src/api/blog.ts
```

- [ ] **Step 2: 更新所有引用**

搜索所有 `from '@/api/post'` 或 `from "@/api/post"` 的导入，替换为 `from '@/api/blog'`。

需要更新的文件（通过 grep 确认）：
- `src/pages/post/PostDetailPage.vue`
- `src/pages/post/PostEditorPage.vue`
- `src/pages/user/MyPostsPage.vue`
- `src/pages/user/FavoritesPage.vue`
- `src/pages/note/SimpleNoteDetailPage.vue`
- `src/pages/note/NoteEditorPage.vue`
- `src/pages/home/SimpleHomePage.vue`
- `src/pages/CategoryPage.vue`
- `src/pages/TagPage.vue`
- `src/pages/SearchPage.vue`
- `src/features/home/pages/HomePage.vue`
- `src/store/post.ts`

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: rename api/post.ts to api/blog.ts for backend module alignment"
```

---

## Task 3: 重命名 API 文件（comment.ts → comments.ts）

**Files:**
- Rename: `src/api/comment.ts` → `src/api/comments.ts`
- Modify: 所有引用 comment.ts 的文件

- [ ] **Step 1: 重命名文件**

```bash
mv src/api/comment.ts src/api/comments.ts
```

- [ ] **Step 2: 更新所有引用**

搜索所有 `from '@/api/comment'` 的导入，替换为 `from '@/api/comments'`。

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: rename api/comment.ts to api/comments.ts for backend module alignment"
```

---

## Task 4: 合并 auth.ts + user.ts → accounts.ts

**Files:**
- Create: `src/api/accounts.ts`
- Delete: `src/api/auth.ts`
- Delete: `src/api/user.ts`
- Modify: 所有引用 auth.ts 和 user.ts 的文件

- [ ] **Step 1: 创建 accounts.ts**

合并 `auth.ts` 和 `user.ts` 的所有导出到 `accounts.ts`，保持所有接口和函数不变。

- [ ] **Step 2: 更新所有引用**

搜索所有 `from '@/api/auth'` 和 `from '@/api/user'` 的导入，替换为 `from '@/api/accounts'`。

- [ ] **Step 3: 删除旧文件**

```bash
rm src/api/auth.ts src/api/user.ts
```

- [ ] **Step 4: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: merge api/auth.ts and api/user.ts into api/accounts.ts"
```

---

## Task 5: 合并 notification.ts → interactions.ts

**Files:**
- Create: `src/api/interactions.ts`
- Delete: `src/api/notification.ts`
- Modify: 所有引用 notification.ts 的文件

- [ ] **Step 1: 创建 interactions.ts**

将 `notification.ts` 的内容合并到 `interactions.ts`，并添加留言板和收藏相关的 API。

- [ ] **Step 2: 更新所有引用**

搜索所有 `from '@/api/notification'` 的导入，替换为 `from '@/api/interactions'`。

- [ ] **Step 3: 删除旧文件**

```bash
rm src/api/notification.ts
```

- [ ] **Step 4: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: merge api/notification.ts into api/interactions.ts"
```

---

## Task 6: 合并 newsAdmin.ts → news.ts

**Files:**
- Modify: `src/api/news.ts`
- Delete: `src/api/newsAdmin.ts`
- Modify: 所有引用 newsAdmin.ts 的文件

- [ ] **Step 1: 合并内容**

将 `newsAdmin.ts` 的所有导出合并到 `news.ts` 的末尾（用注释分隔 admin 部分）。

- [ ] **Step 2: 更新所有引用**

搜索所有 `from '@/api/newsAdmin'` 的导入，替换为 `from '@/api/news'`。

- [ ] **Step 3: 删除旧文件**

```bash
rm src/api/newsAdmin.ts
```

- [ ] **Step 4: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: merge api/newsAdmin.ts into api/news.ts"
```

---

## Task 7: 移动 pages 目录（auth → accounts）

**Files:**
- Move: `src/pages/auth/*` → `src/pages/accounts/*`
- Move: `src/pages/user/*` → `src/pages/accounts/*`
- Move: `src/pages/settings/ThemeSettings.vue` → `src/pages/accounts/ThemeSettings.vue`
- Modify: `src/app/router/modules/user.ts`（更新 import 路径）

- [ ] **Step 1: 创建目标目录并移动文件**

```bash
mkdir -p src/pages/accounts
mv src/pages/auth/* src/pages/accounts/
mv src/pages/user/* src/pages/accounts/
mv src/pages/settings/ThemeSettings.vue src/pages/accounts/
rmdir src/pages/auth src/pages/user src/pages/settings
```

- [ ] **Step 2: 更新 router/modules/user.ts 中的 import 路径**

将所有 `@/pages/auth/` 替换为 `@/pages/accounts/`
将所有 `@/pages/user/` 替换为 `@/pages/accounts/`
将 `@/pages/settings/ThemeSettings.vue` 替换为 `@/pages/accounts/ThemeSettings.vue`

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move pages/auth and pages/user to pages/accounts"
```

---

## Task 8: 移动 pages 目录（post → blog）

**Files:**
- Move: `src/pages/post/*` → `src/pages/blog/*`
- Move: `src/pages/note/*` → `src/pages/blog/*`
- Move: `src/pages/CategoryPage.vue` → `src/pages/blog/CategoryPage.vue`
- Move: `src/pages/TagPage.vue` → `src/pages/blog/TagPage.vue`
- Modify: `src/app/router/modules/content.ts`（更新 import 路径）

- [ ] **Step 1: 创建目标目录并移动文件**

```bash
mkdir -p src/pages/blog
mv src/pages/post/* src/pages/blog/
mv src/pages/note/* src/pages/blog/
mv src/pages/CategoryPage.vue src/pages/blog/
mv src/pages/TagPage.vue src/pages/blog/
rmdir src/pages/post src/pages/note
```

- [ ] **Step 2: 更新 router/modules/content.ts 中的 import 路径**

将所有 `@/pages/post/` 替换为 `@/pages/blog/`
将所有 `@/pages/note/` 替换为 `@/pages/blog/`
将 `@/pages/CategoryPage.vue` 替换为 `@/pages/blog/CategoryPage.vue`
将 `@/pages/TagPage.vue` 替换为 `@/pages/blog/TagPage.vue`

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: move pages/post and pages/note to pages/blog"
```

---

## Task 9: 移动其他 pages 目录

**Files:**
- Move: `src/pages/SearchPage.vue` → `src/pages/search/SearchPage.vue`
- Move: `src/pages/PhotoWallPage.vue` → `src/pages/media/PhotoWallPage.vue`
- Move: `src/pages/interaction/*` → `src/pages/interactions/*`
- Move: `src/pages/flights/*` → `src/pages/flight/*`

- [ ] **Step 1: 移动文件**

```bash
mkdir -p src/pages/search src/pages/media src/pages/flight
mv src/pages/SearchPage.vue src/pages/search/
mv src/pages/PhotoWallPage.vue src/pages/media/
mv src/pages/interaction/* src/pages/interactions/
mv src/pages/flights/* src/pages/flight/
rmdir src/pages/interaction src/pages/flights
```

- [ ] **Step 2: 更新 router 中的 import 路径**

- `content.ts`: `@features/search/pages/SearchPage.vue` → `@/pages/search/SearchPage.vue`
- `content.ts`: `@/pages/PhotoWallPage.vue` → `@/pages/media/PhotoWallPage.vue`
- `content.ts`: `@/pages/interaction/BoardPage.vue` → `@/pages/interactions/BoardPage.vue`
- `tools.ts`: `@/pages/flights/FlightTracker.vue` → `@/pages/flight/FlightTracker.vue`

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: reorganize pages directory to match backend modules"
```

---

## Task 10: 整理 admin 目录

**Files:**
- Move: `src/pages/admin/SecurityIPBlocksPage.vue` → `src/pages/admin/security/IPBlocksPage.vue`
- Move: `src/pages/admin/SecurityRequestLogsPage.vue` → `src/pages/admin/security/RequestLogsPage.vue`
- Move: `src/pages/admin/UserListPage.vue` → `src/pages/admin/accounts/UserListPage.vue`
- Move: `src/pages/admin/SiteConfigPage.vue` → `src/pages/admin/core/SiteConfigPage.vue`

- [ ] **Step 1: 创建子目录并移动文件**

```bash
mkdir -p src/pages/admin/security src/pages/admin/accounts src/pages/admin/core
mv src/pages/admin/SecurityIPBlocksPage.vue src/pages/admin/security/IPBlocksPage.vue
mv src/pages/admin/SecurityRequestLogsPage.vue src/pages/admin/security/RequestLogsPage.vue
mv src/pages/admin/UserListPage.vue src/pages/admin/accounts/UserListPage.vue
mv src/pages/admin/SiteConfigPage.vue src/pages/admin/core/SiteConfigPage.vue
```

- [ ] **Step 2: 更新 router 中的 import 路径**

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: organize admin pages by backend module"
```

---

## Task 11: 重组 router/modules

**Files:**
- Modify: `src/app/router/index.ts`
- Modify: `src/app/router/modules/` (all files)
- Create: `src/app/router/modules/blog.ts`
- Create: `src/app/router/modules/accounts.ts`
- Create: `src/app/router/modules/news.ts`
- Create: `src/app/router/modules/knowledge.ts`
- Create: `src/app/router/modules/interactions.ts`
- Create: `src/app/router/modules/payments.ts`
- Create: `src/app/router/modules/membership.ts`
- Create: `src/app/router/modules/filemanager.ts`
- Create: `src/app/router/modules/flight.ts`
- Create: `src/app/router/modules/apigateway.ts`
- Create: `src/app/router/modules/media.ts`
- Create: `src/app/router/modules/admin.ts`
- Create: `src/app/router/modules/search.ts`
- Delete: `src/app/router/modules/content.ts`
- Delete: `src/app/router/modules/user.ts`
- Delete: `src/app/router/modules/services.ts`

- [ ] **Step 1: 创建新的路由模块文件**

按后端模块拆分路由，每个文件对应一个后端模块。

- [ ] **Step 2: 更新 src/app/router/index.ts**

```typescript
import { createRouter, createWebHistory } from "vue-router";
import { accountRoutes } from "./modules/accounts";
import { blogRoutes } from "./modules/blog";
import { newsRoutes } from "./modules/news";
import { knowledgeRoutes } from "./modules/knowledge";
import { interactionRoutes } from "./modules/interactions";
import { paymentRoutes } from "./modules/payments";
import { membershipRoutes } from "./modules/membership";
import { filemanagerRoutes } from "./modules/filemanager";
import { flightRoutes } from "./modules/flight";
import { apigatewayRoutes } from "./modules/apigateway";
import { mediaRoutes } from "./modules/media";
import { toolRoutes } from "./modules/tools";
import { adminRoutes } from "./modules/admin";
import { searchRoutes } from "./modules/search";
import { legalRoutes } from "./modules/legal";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...accountRoutes,
    ...blogRoutes,
    ...newsRoutes,
    ...knowledgeRoutes,
    ...interactionRoutes,
    ...paymentRoutes,
    ...membershipRoutes,
    ...filemanagerRoutes,
    ...flightRoutes,
    ...apigatewayRoutes,
    ...mediaRoutes,
    ...toolRoutes,
    ...adminRoutes,
    ...searchRoutes,
    ...legalRoutes,
  ],
});

// ... beforeEach 和 afterEach 保持不变

export default router;
```

- [ ] **Step 3: 删除旧的路由模块文件**

```bash
rm src/app/router/modules/content.ts src/app/router/modules/user.ts src/app/router/modules/services.ts
```

- [ ] **Step 4: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: reorganize router modules to match backend modules"
```

---

## Task 12: 重组 components 目录

**Files:**
- Move: `src/components/comment/*` → `src/components/comments/*`
- Move: `src/components/post/*` → `src/components/blog/*`
- Move: `src/components/home/*` → `src/components/blog/*` (或 `src/components/common/*`)
- Modify: 所有引用旧路径的文件

- [ ] **Step 1: 移动组件文件**

```bash
mv src/components/comment src/components/comments
mv src/components/post src/components/blog
# home 组件根据内容决定归属
```

- [ ] **Step 2: 更新所有引用**

搜索所有 `@/components/comment/` 替换为 `@/components/comments/`
搜索所有 `@/components/post/` 替换为 `@/components/blog/`

- [ ] **Step 3: 验证类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: reorganize components by business module"
```

---

## Task 13: 最终验证

- [ ] **Step 1: 完整类型检查**

Run: `pnpm typecheck`
Expected: PASS

- [ ] **Step 2: 开发服务器启动**

Run: `pnpm dev`
Expected: 服务器正常启动，页面可访问

- [ ] **Step 3: 构建验证**

Run: `pnpm build`
Expected: 构建成功

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore: verify phase 1 structure alignment complete"
```

---

## 验证清单

- [ ] 所有 API 文件与后端 module 1:1 对应
- [ ] 所有 pages 目录与后端 module 1:1 对应
- [ ] 所有 router/modules 与后端 module 1:1 对应
- [ ] `pnpm typecheck` 通过
- [ ] `pnpm dev` 正常启动
- [ ] `pnpm build` 构建成功
