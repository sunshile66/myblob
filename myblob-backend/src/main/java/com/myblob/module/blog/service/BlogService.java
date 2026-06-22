package com.myblob.module.blog.service;

import com.myblob.common.BusinessException;
import com.myblob.common.IpUtil;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.blog.dto.*;
import com.myblob.module.blog.entity.*;
import com.myblob.module.blog.repository.*;
import com.myblob.module.media.repository.MediaAssetRepository;
import com.myblob.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRevisionRepository postRevisionRepository;
    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final PostInteractionService postInteractionService;
    private final ViewCountService viewCountService;
    private final HttpServletRequest httpServletRequest;

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getPosts(int page, int size, String category, String tag,
            String search, String status, String ordering) {
        Sort sort = parseOrdering(ordering);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts;

        Post.Status statusEnum = parseStatusSafe(status);

        if (search != null && !search.isBlank()) {
            posts = postRepository.searchPosts(search, pageable);
        } else if (category != null && !category.isBlank()) {
            posts = postRepository.findByCategorySlug(category, pageable);
        } else if (tag != null && !tag.isBlank()) {
            posts = postRepository.findByTagSlug(tag, pageable);
        } else if (statusEnum != null) {
            posts = postRepository.findByStatus(statusEnum, pageable);
        } else {
            posts = postRepository.findPublishedPosts(pageable);
        }

        // 批量查询当前用户的点赞/收藏状态，避免 N+1
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        Set<Long> likedPostIds = Set.of();
        Set<Long> favoritedPostIds = Set.of();
        if (currentUserId != null && posts.hasContent()) {
            List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
            likedPostIds = postInteractionService.getLikedPostIds(currentUserId, postIds);
            favoritedPostIds = postInteractionService.getFavoritedPostIds(currentUserId, postIds);
        }

        final Set<Long> finalLiked = likedPostIds;
        final Set<Long> finalFavorited = favoritedPostIds;
        Page<PostDTO> dtoPage = posts
                .map(p -> PostDTOAssembler.toFullDTO(p, currentUserId, finalLiked, finalFavorited));
        return PageResponse.of(dtoPage);
    }

    @Transactional
    public PostDTO getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));

        // IP 级别浏览量去重（30分钟内同一IP不计重复浏览）
        // recordView 内部已调用 postRepository.incrementViewCount()，此处只更新内存对象
        String clientIp = IpUtil.getClientIp(httpServletRequest);
        if (viewCountService.recordView(post.getId(), clientIp)) {
            post.setViewCount(post.getViewCount() + 1);
        }

        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        Set<Long> likedPostIds = currentUserId != null
                ? postInteractionService.getLikedPostIds(currentUserId, List.of(post.getId()))
                : Set.of();
        Set<Long> favoritedPostIds = currentUserId != null
                ? postInteractionService.getFavoritedPostIds(currentUserId, List.of(post.getId()))
                : Set.of();
        return PostDTOAssembler.toFullDTO(post, currentUserId, likedPostIds, favoritedPostIds);
    }

    @Transactional
    public PostDTO createPost(CreatePostRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User author = userRepository.getReferenceById(userId);

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = generateSlug(request.getTitle());
        }

        Post.PostType postType = Post.PostType.NOTE;
        if (request.getPostType() != null) {
            try {
                postType = Post.PostType.valueOf(request.getPostType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的文章类型: " + request.getPostType());
            }
        }

        Post.Status postStatus = Post.Status.DRAFT;
        if (request.getStatus() != null) {
            try {
                postStatus = Post.Status.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的文章状态: " + request.getStatus());
            }
        }

        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .cover(request.getCover())
                .postType(postType)
                .status(postStatus)
                .allowComment(request.getAllowComment() != null ? request.getAllowComment() : true)
                .original(request.getOriginal() != null ? request.getOriginal() : true)
                .build();

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(post::setCategory);
        }

        if (request.getVideoId() != null) {
            mediaAssetRepository.findById(request.getVideoId()).ifPresent(post::setVideo);
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = request.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder()
                                    .name(tagName)
                                    .slug(tagName.toLowerCase().replaceAll("\\s+", "-"))
                                    .build())))
                    .collect(Collectors.toSet());
            post.setTags(tags);
        }

        if (postStatus == Post.Status.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }
        post = postRepository.save(post);

        log.info("文章创建成功: id={}, title={}, author={}", post.getId(), post.getTitle(), userId);

        // 新创建的文章，当前用户尚未点赞/收藏
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        return PostDTOAssembler.toFullDTO(post, currentUserId, Set.of(), Set.of());
    }

    @Transactional
    public PostDTO updatePost(String slug, UpdatePostRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw BusinessException.forbidden("无权编辑此文章");
        }

        if (request.getTitle() != null)
            post.setTitle(request.getTitle());
        if (request.getSlug() != null)
            post.setSlug(request.getSlug());
        if (request.getSummary() != null)
            post.setSummary(request.getSummary());
        if (request.getContent() != null)
            post.setContent(request.getContent());
        if (request.getCover() != null)
            post.setCover(request.getCover());
        if (request.getAllowComment() != null)
            post.setAllowComment(request.getAllowComment());
        if (request.getOriginal() != null)
            post.setOriginal(request.getOriginal());
        if (request.getTop() != null)
            post.setTop(request.getTop());

        if (request.getPostType() != null) {
            try {
                post.setPostType(Post.PostType.valueOf(request.getPostType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的文章类型: " + request.getPostType());
            }
        }

        if (request.getStatus() != null) {
            try {
                Post.Status newStatus = Post.Status.valueOf(request.getStatus().toUpperCase());
                if (newStatus == Post.Status.PUBLISHED && post.getPublishedAt() == null) {
                    post.setPublishedAt(LocalDateTime.now());
                }
                post.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的文章状态: " + request.getStatus());
            }
        }

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(post::setCategory);
        }

        if (request.getVideoId() != null) {
            mediaAssetRepository.findById(request.getVideoId()).ifPresent(post::setVideo);
        }

        if (request.getTags() != null) {
            Set<Tag> tags = request.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder()
                                    .name(tagName)
                                    .slug(tagName.toLowerCase().replaceAll("\\s+", "-"))
                                    .build())))
                    .collect(Collectors.toSet());
            post.setTags(tags);
        }

        // 保存版本历史
        PostRevision revision = PostRevision.builder()
                .post(post)
                .editor(userRepository.getReferenceById(userId))
                .title(post.getTitle())
                .summary(post.getSummary())
                .content(post.getContent())
                .deleted(false)
                .build();
        postRevisionRepository.save(revision);

        postRepository.save(post);

        // 查询当前用户对更新后文章的点赞/收藏状态
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        Set<Long> likedPostIds = currentUserId != null
                ? postInteractionService.getLikedPostIds(currentUserId, List.of(post.getId()))
                : Set.of();
        Set<Long> favoritedPostIds = currentUserId != null
                ? postInteractionService.getFavoritedPostIds(currentUserId, List.of(post.getId()))
                : Set.of();
        return PostDTOAssembler.toFullDTO(post, currentUserId, likedPostIds, favoritedPostIds);
    }

    @Transactional
    public void deletePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlugActive(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!post.getAuthor().getId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此文章");
        }
        // 使用软删除，与 BaseEntity.deleted 字段保持一致
        post.setDeleted(true);
        postRepository.save(post);

        log.info("文章软删除: slug={}, operator={}", slug, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getMyPosts(int page, int size, String status) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (status != null && !status.isBlank()) {
            try {
                Post.Status s = Post.Status.valueOf(status.toUpperCase());
                posts = postRepository.findByAuthorIdAndStatus(userId, s, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的文章状态: " + status);
            }
        } else {
            posts = postRepository.findByAuthorId(userId, pageable);
        }

        // 批量查询点赞/收藏状态
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        Set<Long> likedPostIds = Set.of();
        Set<Long> favoritedPostIds = Set.of();
        if (currentUserId != null && posts.hasContent()) {
            List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
            likedPostIds = postInteractionService.getLikedPostIds(currentUserId, postIds);
            favoritedPostIds = postInteractionService.getFavoritedPostIds(currentUserId, postIds);
        }

        final Set<Long> finalLiked = likedPostIds;
        final Set<Long> finalFavorited = favoritedPostIds;
        return PageResponse
                .of(posts.map(p -> PostDTOAssembler.toFullDTO(p, currentUserId, finalLiked, finalFavorited)));
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findByActiveTrueOrderBySortAsc().stream()
                .map(PostDTOAssembler::toCategoryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getTags() {
        return tagRepository.findAll().stream()
                .map(PostDTOAssembler::toTagDTO)
                .toList();
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > 200)
            slug = slug.substring(0, 200);
        return slug + "-" + System.currentTimeMillis();
    }

    /**
     * 安全解析排序参数
     */
    private Sort parseOrdering(String ordering) {
        if (ordering == null || ordering.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        }
        return switch (ordering.toLowerCase()) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "publishedAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "publishedAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "most_liked" -> Sort.by(Sort.Direction.DESC, "likeCount");
            default -> Sort.by(Sort.Direction.DESC, "publishedAt");
        };
    }

    /**
     * 获取文章版本历史列表
     */
    @Transactional(readOnly = true)
    public List<PostRevisionDTO> getRevisions(Long postId) {
        List<PostRevision> revisions = postRevisionRepository.findByPostIdAndDeletedFalseOrderByCreatedAtDesc(postId);
        return revisions.stream()
                .map(r -> PostRevisionDTO.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .summary(r.getSummary())
                        .content(r.getContent())
                        .editorName(r.getEditor() != null ? r.getEditor().getNickname() : null)
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * 安全解析状态参数，无效值时返回null
     */
    private Post.Status parseStatusSafe(String status) {
        if (status == null || status.isBlank())
            return null;
        try {
            return Post.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
