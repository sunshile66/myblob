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
        List<Comment> rootComments = commentRepository.findRootCommentsByPostId(postId);
        return rootComments.stream().map(this::toDTOWithChildren).toList();
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

        if (request.getNickname() != null) builder.nickname(request.getNickname());
        if (request.getEmail() != null) builder.email(request.getEmail());

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

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

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
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    @Transactional
    public void unlikeComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("评论"));
        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new BusinessException("还没有点赞");
        }
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        commentRepository.save(comment);
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
                        ? toUserDTO(comment.getReplyTo().getUser()) : null)
                .nickname(comment.getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .isApproved(comment.getApproved())
                .isDeleted(comment.getDeleted())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private CommentDTO toDTOWithChildren(Comment comment) {
        CommentDTO dto = toDTO(comment);
        List<Comment> children = commentRepository.findChildrenByParentId(comment.getId());
        if (children != null && !children.isEmpty()) {
            dto.setChildren(children.stream().map(this::toDTOWithChildren).toList());
        }
        return dto;
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
