package com.myblob.module.blog.repository;

import com.myblob.module.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("SELECT t FROM Tag t WHERE t.slug = :slug AND t.deleted = false")
    Optional<Tag> findBySlug(@Param("slug") String slug);

    @Query("SELECT t FROM Tag t WHERE t.name = :name AND t.deleted = false")
    Optional<Tag> findByName(@Param("name") String name);
}
