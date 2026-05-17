package com.myblob.module.comments.repository;

import com.myblob.module.comments.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    List<CommentReaction> findByCommentId(Long commentId);

    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}
