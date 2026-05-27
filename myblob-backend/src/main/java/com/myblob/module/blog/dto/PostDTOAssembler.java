package com.myblob.module.blog.dto;

import com.myblob.module.accounts.dto.UserDTO;
import com.myblob.module.accounts.dto.UserDTOAssembler;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.blog.entity.Category;
import com.myblob.module.blog.entity.Post;
import com.myblob.module.blog.entity.Tag;

import java.util.List;
import java.util.Set;

/**
 * PostDTO 组装器 —— 统一 Post → PostDTO 的转换逻辑。
 */
public final class PostDTOAssembler {

    private PostDTOAssembler() {
    }

    /**
     * 转换为基本 DTO（不含作者、分类、标签、互动状态）。
     * 适用于收藏列表等只需摘要信息的场景。
     */
    public static PostDTO toBasicDTO(Post post) {
        if (post == null)
            return null;
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .cover(post.getCover())
                .postType(post.getPostType() != null ? post.getPostType().name().toLowerCase() : null)
                .status(post.getStatus() != null ? post.getStatus().name().toLowerCase() : null)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 转换为完整 DTO（含作者、分类、标签、视频、互动状态）。
     */
    public static PostDTO toFullDTO(Post post, Long currentUserId,
            Set<Long> likedPostIds, Set<Long> favoritedPostIds) {
        PostDTO dto = toBasicDTO(post);
        if (dto == null)
            return null;

        dto.setContent(post.getContent());
        dto.setTop(post.getTop());
        dto.setAllowComment(post.getAllowComment());
        dto.setOriginal(post.getOriginal());
        dto.setAuthor(toUserDTO(post.getAuthor()));
        dto.setCategory(post.getCategory() != null ? toCategoryDTO(post.getCategory()) : null);
        dto.setTags(post.getTags() != null
                ? post.getTags().stream().map(PostDTOAssembler::toTagDTO).toList()
                : List.of());
        dto.setVideo(post.getVideo() != null ? MediaAssetDTO.builder()
                .id(post.getVideo().getId())
                .file(post.getVideo().getFile())
                .mediaType(post.getVideo().getMediaType() != null
                        ? post.getVideo().getMediaType().name().toLowerCase()
                        : null)
                .title(post.getVideo().getTitle())
                .altText(post.getVideo().getAltText())
                .build() : null);
        dto.setIsLiked(likedPostIds.contains(post.getId()));
        dto.setIsFavorited(favoritedPostIds.contains(post.getId()));
        return dto;
    }

    public static UserDTO toUserDTO(User user) {
        return UserDTOAssembler.toBasicDTO(user);
    }

    public static CategoryDTO toCategoryDTO(Category c) {
        if (c == null)
            return null;
        return CategoryDTO.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).sort(c.getSort()).active(c.getActive())
                .build();
    }

    public static TagDTO toTagDTO(Tag t) {
        if (t == null)
            return null;
        return TagDTO.builder().id(t.getId()).name(t.getName()).slug(t.getSlug()).build();
    }
}
