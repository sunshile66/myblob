---
name: swagger-docs
description: Spring Boot OpenAPI/Swagger 文档配置规范。当配置 API 文档、添加接口注解、调试 Swagger UI 时使用此 skill。
---

# Swagger/OpenAPI 文档配置规范

## 触发条件

- 配置 SpringDoc OpenAPI
- 添加 Controller 接口注解
- 调试 Swagger UI
- 生成 API 文档

---

## Part 1: 依赖配置

### Maven（Spring Boot 3.x）

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

### application.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## Part 2: SecurityConfig 放行路径

```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
```

---

## Part 3: Controller 注解规范

```java
@RestController
@RequestMapping("/api/blog")
@Tag(name = "博客管理", description = "文章相关接口")
public class BlogController {

    @GetMapping("/posts/")
    @Operation(summary = "获取文章列表", description = "支持分类、标签、搜索筛选")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getPosts(...) { }

    @PostMapping("/posts/")
    @Operation(summary = "创建文章")
    @ApiResponse(responseCode = "201", description = "创建成功")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(...) { }
}
```

---

## Part 4: DTO 注解

```java
@Schema(description = "文章创建请求")
public class CreatePostRequest {
    @Schema(description = "文章标题", example = "Spring Boot入门")
    @NotBlank
    private String title;
}
```

---

## Part 5: 访问地址

- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`
