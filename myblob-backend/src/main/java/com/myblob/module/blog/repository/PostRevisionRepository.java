package com.myblob.module.blog.repository;

import com.myblob.module.blog.entity.PostRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRevisionRepository extends JpaRepository<PostRevision, Long> {

    List<PostRevision> findByPostIdOrderByCreatedAtDesc(Long postId);
}
