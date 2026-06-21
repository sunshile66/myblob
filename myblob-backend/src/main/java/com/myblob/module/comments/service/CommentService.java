package com.myblob.module.comments.service;

import com.myblob.common.BusinessException;
import com.myblob.module.accounts.dto.UserDTO;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.blog.entity.Post;
import com.myblob.module.blog.repository.PostRepository;
import com.myblob.module.comments.dto.CommentDTO;
import com.myblob.module.comments.dto.CreateCommentRequest;
import com.myblob.module.comments.entity.*;
import com.myblob.module.comments.repository.*;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final EmojiRepository emojiRepository;
    private final StickerRepository stickerRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPost(Long postId) {
        // 一次性加载该文章的所有评论（含用户信息），避免递归 N+1
        List<Comment> allComments = commentRepository.findAllByPostIdWithUser(postId);
        
        // 按 parentId 分组，构建子评论映射
        Map<Long, List<Comment>> childrenMap = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));
        
        // 获取根评论（无父评论）
        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        
        // 构建树结构
        return rootComments.stream()
                .map(root -> buildTree(root, childrenMap))
                .toList();
    }
    
    /**
     * 递归构建评论树
     */
    private CommentDTO buildTree(Comment comment, Map<Long, List<Comment>> childrenMap) {
        CommentDTO dto = toDTO(comment);
        List<Comment> children = childrenMap.getOrDefault(comment.getId(), List.of());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(child -> buildTree(child, childrenMap))
                    .toList());
        }
        return dto;
    }

    @Transactional
    public CommentDTO createComment(CreateCommentRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> BusinessException.notFound("文章"));

        Long userId = SecurityUtil.getCurrentUserIdOrNull();
        User user = userId != null ? userRepository.getReferenceById(userId) : null;

        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .approved(userId != null)
                .deleted(false)
                .likeCount(0);

        if (request.getNickname() != null) {
            builder.nickname(request.getNickname());
        } else if (user != null && user.getNickname() != null) {
            builder.nickname(user.getNickname());
        } else if (user != null) {
            builder.nickname(user.getUsername());
        }
        if (request.getEmail() != null) {
            builder.email(request.getEmail());
        } else if (user != null && user.getEmail() != null) {
            builder.email(user.getEmail());
        }

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("父评论"));
            builder.parent(parent);
        }

        if (request.getReplyToId() != null) {
            Comment replyTo = commentRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> BusinessException.notFound("被回复评论"));
            builder.replyTo(replyTo);
            if (builder.build().getNickname() == null && replyTo.getUser() != null) {
                builder.nickname(replyTo.getUser().getNickname());
            }
        }

        Comment comment = commentRepository.save(builder.build());

        postRepository.incrementCommentCount(post.getId());

        return toDTO(comment);
    }

    @Transactional
    public void likeComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("评论"));
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException("已经点赞过了");
        }
        CommentLike like = CommentLike.builder().comment(comment).user(userRepository.getReferenceById(userId)).build();
        commentLikeRepository.save(like);
        commentRepository.incrementLikeCount(commentId);
    }

    @Transactional
    public void unlikeComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException("还没有点赞");
        }
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        commentRepository.decrementLikeCount(commentId);
    }

    @Transactional
    public Map<String, Integer> reactToComment(Long commentId, String emoji) {
        Long userId = SecurityUtil.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("评论"));
        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(userRepository.getReferenceById(userId))
                .emoji(emoji)
                .build();
        commentReactionRepository.save(reaction);
        return getReactions(commentId);
    }

    @Transactional
    public void unreactToComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        commentReactionRepository.deleteByCommentIdAndUserId(commentId, userId);
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("评论"));
        if (comment.getUser() == null || !comment.getUser().getId().equals(userId)) {
            throw new BusinessException("无权编辑此评论");
        }
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        return toDTO(saved);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("评论"));
        // Author or Admin can delete
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (comment.getUser() != null && !comment.getUser().getId().equals(userId) && !isAdmin) {
            throw new BusinessException("无权删除此评论");
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getReactions(Long commentId) {
        return commentReactionRepository.findByCommentId(commentId).stream()
                .collect(Collectors.groupingBy(CommentReaction::getEmoji, Collectors.summingInt(r -> 1)));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmojis(String category) {
        List<Emoji> emojis = category != null
                ? emojiRepository.findByCategoryAndActiveTrueOrderBySortOrderAsc(category)
                : emojiRepository.findByActiveTrueOrderBySortOrderAsc();
        return emojis.stream().map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("name", e.getName());
            map.put("code", e.getCode());
            map.put("image_url", e.getImageUrl());
            map.put("category", e.getCategory());
            return map;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStickers(String category, Boolean isGif) {
        List<Sticker> stickers;
        if (Boolean.TRUE.equals(isGif)) {
            stickers = stickerRepository.findByGifTrueAndActiveTrueOrderBySortOrderAsc();
        } else if (category != null) {
            stickers = stickerRepository.findByCategoryAndActiveTrueOrderBySortOrderAsc(category);
        } else {
            stickers = stickerRepository.findByActiveTrueOrderBySortOrderAsc();
        }
        return stickers.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("image_url", s.getImageUrl());
            map.put("thumbnail_url", s.getThumbnailUrl());
            map.put("is_gif", s.getGif());
            map.put("category", s.getCategory());
            return map;
        }).toList();
    }

    private CommentDTO toDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .user(comment.getUser() != null ? toUserDTO(comment.getUser()) : null)
                .author(comment.getUser() != null ? toUserDTO(comment.getUser()) : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replyTo(comment.getReplyTo() != null && comment.getReplyTo().getUser() != null
                        ? toUserDTO(comment.getReplyTo().getUser())
                        : null)
                .nickname(comment.getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .isApproved(comment.getApproved())
                .isDeleted(comment.getDeleted())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}
