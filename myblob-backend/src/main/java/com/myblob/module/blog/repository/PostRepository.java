package com.myblob.module.blog.repository;

import com.myblob.module.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

       Optional<Post> findBySlug(String slug);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.author.profile " +
                     "LEFT JOIN FETCH p.category LEFT JOIN FETCH p.tags WHERE p.slug = :slug")
       Optional<Post> findBySlugWithDetails(@Param("slug") String slug);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.status = 'PUBLISHED' ORDER BY p.top DESC, p.publishedAt DESC")
       Page<Post> findPublishedPosts(Pageable pageable);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.status = 'PUBLISHED' AND " +
                     "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<Post> searchPosts(@Param("search") String search, Pageable pageable);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.category.slug = :categorySlug AND p.status = 'PUBLISHED' " +
                     "ORDER BY p.publishedAt DESC")
       Page<Post> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

       @Query("SELECT p FROM Post p JOIN p.tags t LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "WHERE t.slug = :tagSlug AND p.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
       Page<Post> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.author.id = :authorId ORDER BY p.createdAt DESC")
       Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.author.id = :authorId AND p.status = :status ORDER BY p.createdAt DESC")
       Page<Post> findByAuthorIdAndStatus(@Param("authorId") Long authorId, @Param("status") Post.Status status,
                     Pageable pageable);

       /**
        * 按状态查询文章
        */
       @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.category " +
                     "LEFT JOIN FETCH p.tags WHERE p.status = :status ORDER BY p.publishedAt DESC")
       Page<Post> findByStatus(@Param("status") Post.Status status, Pageable pageable);

       /**
        * 原子递增文章浏览量
        */
       @Modifying
       @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
       void incrementViewCount(@Param("postId") Long postId);
}
