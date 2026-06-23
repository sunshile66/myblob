# Myblob 项目代码规范

> 基于 Vue.js 官方风格指南、TypeScript ESLint 推荐规范、Spring Boot 最佳实践

---

## 前端规范 (Vue 3 + TypeScript)

### 1. 组件命名

**规则**: 组件名必须为多词（PascalCase），避免与 HTML 元素冲突

```vue
<!-- ✅ 正确 -->
<template>
  <UserProfile />
  <BlogPost />
  <NewsCard />
</template>

<!-- ❌ 错误 -->
<template>
  <Item />
  <Post />
</template>
```

### 2. Props 定义

**规则**: Props 必须详细定义类型，使用 TypeScript 接口

```vue
<script setup lang="ts">
// ✅ 正确 - 使用 TypeScript 接口
interface Props {
  title: string
  count?: number
  items: string[]
}

const props = defineProps<Props>()

// ✅ 带默认值
const props = withDefaults(defineProps<Props>(), {
  count: 0,
  items: () => []
})

// ❌ 错误 - 缺少类型
const props = defineProps(['title', 'count'])
</script>
```

### 3. v-for 必须带 key

**规则**: v-for 指令必须使用唯一 key

```vue
<template>
  <!-- ✅ 正确 -->
  <div v-for="item in items" :key="item.id">
    {{ item.name }}
  </div>

  <!-- ❌ 错误 -->
  <div v-for="item in items">
    {{ item.name }}
  </div>
</template>
```

### 4. 避免 v-if 与 v-for 同时使用

**规则**: 不要在同一元素上使用 v-if 和 v-for

```vue
<script setup lang="ts">
const activeItems = computed(() => items.value.filter(i => i.isActive))
</script>

<template>
  <!-- ✅ 正确 - 使用 computed 过滤 -->
  <div v-for="item in activeItems" :key="item.id">
    {{ item.name }}
  </div>

  <!-- ❌ 错误 -->
  <div v-for="item in items" v-if="item.isActive" :key="item.id">
    {{ item.name }}
  </div>
</template>
```

### 5. 样式作用域

**规则**: 组件必须使用 scoped 样式

```vue
<!-- ✅ 正确 -->
<style scoped>
.component-class {
  color: red;
}
</style>

<!-- ❌ 错误 -->
<style>
.component-class {
  color: red;
}
</style>
```

### 6. 组件文件结构

**规则**: 单文件组件应遵循 `<template>` → `<script>` → `<style>` 顺序

```vue
<template>
  <!-- 1. 模板 -->
</template>

<script setup lang="ts">
// 2. 脚本
</script>

<style scoped>
/* 3. 样式 */
</style>
```

### 7. 响应式数据

**规则**: 使用 ref 和 computed，避免 reactive 的过度使用

```typescript
// ✅ 正确
const count = ref(0)
const doubled = computed(() => count.value * 2)

// ⚠️ 谨慎使用 reactive（复杂对象才用）
const state = reactive({
  loading: false,
  data: null
})
```

### 8. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserProfile.vue` |
| 组合式函数 | camelCase, use 前缀 | `useAuth.ts` |
| 工具函数 | camelCase | `formatDate.ts` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 类型/接口 | PascalCase | `UserProfile`, `ApiResponse` |
| CSS 类名 | kebab-case | `user-profile`, `card-item` |

### 9. 导入顺序

```typescript
// 1. Vue 核心
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 3. 项目类型
import type { User, Post } from '@/types'

// 4. 项目工具
import { formatDate } from '@/utils/date'
import { useUserStore } from '@/store/user'

// 5. 项目组件
import UserProfile from '@/components/user/UserProfile.vue'
```

### 10. 错误处理

```typescript
// ✅ 正确 - 统一错误处理
const fetchData = async () => {
  try {
    loading.value = true
    data.value = await api.getData()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '请求失败')
  } finally {
    loading.value = false
  }
}

// ❌ 错误 - 忽略错误
const fetchData = async () => {
  data.value = await api.getData()
}
```

---

## 后端规范 (Spring Boot 3 + Java 21)

### 1. 包结构

```
com.myblob/
├── common/              # 通用组件
│   ├── exception/       # 异常处理
│   ├── logging/         # 日志相关
│   └── util/            # 工具类
├── config/              # 配置类
├── module/              # 业务模块
│   └── {module}/
│       ├── controller/  # 控制器
│       ├── service/     # 服务层
│       ├── repository/  # 数据访问层
│       ├── entity/      # 实体类
│       └── dto/         # 数据传输对象
└── security/            # 安全相关
```

### 2. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserService`, `BlogController` |
| 方法名 | camelCase | `getUserById`, `createPost` |
| 变量名 | camelCase | `userName`, `createTime` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | 全小写 | `com.myblob.module.blog` |
| 表名 | snake_case | `blog_post`, `user_profile` |
| 字段名 | snake_case | `created_at`, `is_deleted` |

### 3. RESTful API 设计

```java
@RestController
@RequestMapping("/api/blog/posts")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    // GET /api/blog/posts - 列表
    @GetMapping
    public PageResponse<PostDTO> list(@Valid PostQueryRequest request) {
        return blogService.getPosts(request);
    }

    // GET /api/blog/posts/{id} - 详情
    @GetMapping("/{id}")
    public PostDTO detail(@PathVariable Long id) {
        return blogService.getPostById(id);
    }

    // POST /api/blog/posts - 创建
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDTO create(@Valid @RequestBody CreatePostRequest request) {
        return blogService.createPost(request);
    }

    // PUT /api/blog/posts/{id} - 更新
    @PutMapping("/{id}")
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        return blogService.updatePost(id, request);
    }

    // DELETE /api/blog/posts/{id} - 删除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        blogService.deletePost(id);
    }
}
```

### 4. 服务层规范

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BlogService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 获取文章列表
     * @param request 查询参数
     * @return 分页结果
     */
    public PageResponse<PostDTO> getPosts(PostQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Post> posts = postRepository.findByStatus(Post.Status.PUBLISHED, pageable);
        return PageResponse.of(posts.map(this::toDTO));
    }

    /**
     * 创建文章
     * @param request 创建请求
     * @return 创建的文章
     */
    @Transactional
    public PostDTO createPost(CreatePostRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User author = userRepository.getReferenceById(userId);
        
        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        
        post = postRepository.save(post);
        log.info("文章创建成功: id={}, title={}", post.getId(), post.getTitle());
        
        return toDTO(post);
    }
}
```

### 5. 异常处理

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("参数校验失败")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
```

### 6. 日志规范

```java
@Service
@Slf4j
public class UserService {

    public UserDTO getUserById(Long id) {
        log.debug("查询用户: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("用户不存在: id={}", id);
                    return BusinessException.notFound("用户");
                });
        
        log.info("查询用户成功: id={}, username={}", id, user.getUsername());
        return toDTO(user);
    }
}
```

### 7. 事务规范

```java
@Service
@Transactional(readOnly = true)  // 默认只读
public class PostService {

    // 只读查询
    public PostDTO getPost(Long id) {
        // ...
    }

    // 写操作
    @Transactional
    public PostDTO createPost(CreatePostRequest request) {
        // ...
    }
}
```

---

## 工具配置

### ESLint (前端)

```javascript
// eslint.config.mjs
import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import vue from 'eslint-plugin-vue'

export default [
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...vue.configs['flat/recommended'],
  {
    rules: {
      'vue/multi-word-component-names': 'error',
      'vue/require-v-for-key': 'error',
      'vue/no-v-for-template-key': 'error',
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }]
    }
  }
]
```

### Checkstyle (后端)

```xml
<!-- checkstyle.xml -->
<module name="Checker">
  <module name="TreeWalker">
    <module name="ConstantName"/>
    <module name="LocalFinalVariableName"/>
    <module name="LocalVariableName"/>
    <module name="MemberName"/>
    <module name="MethodName"/>
    <module name="PackageName"/>
    <module name="ParameterName"/>
    <module name="StaticVariableName"/>
    <module name="TypeName"/>
  </module>
</module>
```

---

## Git 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**:
```
feat(blog): 添加文章搜索功能

- 支持标题和内容全文搜索
- 添加搜索结果高亮
- 优化搜索性能

Closes #123
```

---

## 代码审查清单

### 前端
- [ ] 组件名为多词
- [ ] Props 有类型定义
- [ ] v-for 有 key
- [ ] 无 v-if + v-for 同用
- [ ] 样式有 scoped
- [ ] 无 console.log
- [ ] 错误已处理
- [ ] 无 any 类型滥用

### 后端
- [ ] 方法有 Javadoc
- [ ] 事务注解正确
- [ ] 日志级别合适
- [ ] 异常统一处理
- [ ] SQL 无 N+1
- [ ] 敏感信息外置
- [ ] 接口有参数校验
