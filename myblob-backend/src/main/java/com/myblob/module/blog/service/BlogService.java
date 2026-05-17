package com.myblob.module.blog.service;

import com.myblob.common.BusinessException;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.dto.UserDTO;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.blog.dto.*;
import com.myblob.module.blog.entity.*;
import com.myblob.module.blog.repository.*;
import com.myblob.module.interactions.entity.Favorite;
import com.myblob.module.interactions.entity.PostLike;
import com.myblob.module.interactions.repository.FavoriteRepository;
import com.myblob.module.interactions.repository.PostLikeRepository;
import com.myblob.module.media.entity.MediaAsset;
import com.myblob.module.media.repository.MediaAssetRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostRevisionRepository postRevisionRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final FavoriteRepository favoriteRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getPosts(int page, int size, String category, String tag,
                                          String search, String status, String ordering) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (search != null && !search.isBlank()) {
            posts = postRepository.searchPosts(search, pageable);
        } else if (category != null && !category.isBlank()) {
            posts = postRepository.findByCategorySlug(category, pageable);
        } else if (tag != null && !tag.isBlank()) {
            posts = postRepository.findByTagSlug(tag, pageable);
        } else {
            posts = postRepository.findPublishedPosts(pageable);
        }

        Page<PostDTO> dtoPage = posts.map(this::toDTO);
        return PageResponse.of(dtoPage);
    }

    @Transactional
    public PostDTO getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return toDTO(post);
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
            } catch (IllegalArgumentException ignored) {}
        }

        Post.Status postStatus = Post.Status.DRAFT;
        if (request.getStatus() != null) {
            try {
                postStatus = Post.Status.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
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

        post = postRepository.save(post);

        if (postStatus == Post.Status.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
            postRepository.save(post);
        }

        return toDTO(post);
    }

    @Transactional
    public PostDTO updatePost(String slug, UpdatePostRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw BusinessException.forbidden("无权编辑此文章");
        }

        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getSlug() != null) post.setSlug(request.getSlug());
        if (request.getSummary() != null) post.setSummary(request.getSummary());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getCover() != null) post.setCover(request.getCover());
        if (request.getAllowComment() != null) post.setAllowComment(request.getAllowComment());
        if (request.getOriginal() != null) post.setOriginal(request.getOriginal());
        if (request.getTop() != null) post.setTop(request.getTop());

        if (request.getPostType() != null) {
            try {
                post.setPostType(Post.PostType.valueOf(request.getPostType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (request.getStatus() != null) {
            Post.Status newStatus = Post.Status.valueOf(request.getStatus().toUpperCase());
            if (newStatus == Post.Status.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            post.setStatus(newStatus);
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

        postRepository.save(post);
        return toDTO(post);
    }

    @Transactional
    public void deletePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!post.getAuthor().getId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此文章");
        }
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDTO> getMyPosts(int page, int size, String status) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (status != null && !status.isBlank()) {
            Post.Status s = Post.Status.valueOf(status.toUpperCase());
            posts = postRepository.findByAuthorIdAndStatus(userId, s, pageable);
        } else {
            posts = postRepository.findByAuthorId(userId, pageable);
        }

        return PageResponse.of(posts.map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories() {
        return categoryRepository.findByActiveTrueOrderBySortAsc().stream()
                .map(this::toCategoryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getTags() {
        return tagRepository.findAll().stream()
                .map(this::toTagDTO)
                .toList();
    }

    @Transactional
    public void likePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (postLikeRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("已经点赞过了");
        }
        PostLike like = PostLike.builder()
                .user(userRepository.getReferenceById(userId))
                .post(post)
                .build();
        postLikeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!postLikeRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("还没有点赞");
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, post.getId());
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    @Transactional
    public void favoritePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (favoriteRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("已经收藏过了");
        }
        Favorite fav = Favorite.builder()
                .user(userRepository.getReferenceById(userId))
                .post(post)
                .build();
        favoriteRepository.save(fav);
    }

    @Transactional
    public void unfavoritePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!favoriteRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("还没有收藏");
        }
        favoriteRepository.deleteByUserIdAndPostId(userId, post.getId());
    }

    private PostDTO toDTO(Post post) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();

        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .cover(post.getCover())
                .postType(post.getPostType() != null ? post.getPostType().name().toLowerCase() : null)
                .status(post.getStatus() != null ? post.getStatus().name().toLowerCase() : null)
                .top(post.getTop())
                .allowComment(post.getAllowComment())
                .original(post.getOriginal())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .author(toUserDTO(post.getAuthor()))
                .category(post.getCategory() != null ? toCategoryDTO(post.getCategory()) : null)
                .tags(post.getTags() != null ? post.getTags().stream().map(this::toTagDTO).toList() : List.of())
                .video(post.getVideo() != null ? MediaAssetDTO.builder()
                        .id(post.getVideo().getId())
                        .file(post.getVideo().getFile())
                        .mediaType(post.getVideo().getMediaType() != null ? post.getVideo().getMediaType().name().toLowerCase() : null)
                        .title(post.getVideo().getTitle())
                        .altText(post.getVideo().getAltText())
                        .build() : null)
                .isLiked(currentUserId != null && postLikeRepository.existsByUserIdAndPostId(currentUserId, post.getId()))
                .isFavorited(currentUserId != null && favoriteRepository.existsByUserIdAndPostId(currentUserId, post.getId()))
                .build();
    }

    private CategoryDTO toCategoryDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).sort(c.getSort()).active(c.getActive())
                .build();
    }

    private TagDTO toTagDTO(Tag t) {
        return TagDTO.builder().id(t.getId()).name(t.getName()).slug(t.getSlug()).build();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > 200) slug = slug.substring(0, 200);
        return slug + "-" + System.currentTimeMillis();
    }
}
