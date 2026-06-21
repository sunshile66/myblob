package com.myblob.module.knowledge.repository;

import com.myblob.module.knowledge.entity.KnowledgeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {

    @Query("SELECT k FROM KnowledgeItem k WHERE k.category = :category AND k.deleted = false")
    Page<KnowledgeItem> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT k FROM KnowledgeItem k WHERE (k.title LIKE %:search% OR k.content LIKE %:search%) AND k.deleted = false")
    Page<KnowledgeItem> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT k.category FROM KnowledgeItem k WHERE k.deleted = false ORDER BY k.category")
    List<String> findDistinctCategories();

    @Query("SELECT COUNT(k) FROM KnowledgeItem k WHERE k.category = :category AND k.deleted = false")
    long countByCategory(@Param("category") String category);

    @Modifying
    @Query("UPDATE KnowledgeItem k SET k.viewCount = k.viewCount + 1 WHERE k.id = :id AND k.deleted = false")
    void incrementViewCount(@Param("id") Long id);

    @Query(value = """
        SELECT k.*, ts_rank(k.search_vector, query) AS rank
        FROM knowledge_item k, plainto_tsquery('simple', :query) AS query
        WHERE k.search_vector @@ query AND k.deleted = false
        ORDER BY rank DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM knowledge_item k, plainto_tsquery('simple', :query) AS query
        WHERE k.search_vector @@ query AND k.deleted = false
        """,
            nativeQuery = true)
    Page<KnowledgeItem> fullTextSearch(@Param("query") String query, Pageable pageable);

    @Query("SELECT k FROM KnowledgeItem k WHERE k.category = :category AND k.deleted = false ORDER BY k.viewCount DESC")
    Page<KnowledgeItem> findByCategoryOrderByViewCountDesc(@Param("category") String category, Pageable pageable);
}
