package com.myblob.module.blog.repository;

import com.myblob.module.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderBySortAsc();

    @Query("SELECT c FROM Category c WHERE c.slug = :slug AND c.deleted = false")
    Optional<Category> findBySlug(@Param("slug") String slug);
}
