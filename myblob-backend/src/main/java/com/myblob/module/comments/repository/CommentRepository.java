package com.myblob.module.comments.repository;

import com.myblob.module.comments.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replyTo " +
           "WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user " +
           "WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 一次性加载该文章的所有评论（含用户信息），避免递归 N+1
     */
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replyTo LEFT JOIN FETCH c.parent " +
           "WHERE c.post.id = :postId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostIdWithUser(@Param("postId") Long postId);

    long countByPostIdAndDeletedFalseAndApprovedTrue(Long postId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = GREATEST(c.likeCount - 1, 0) WHERE c.id = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);
}
