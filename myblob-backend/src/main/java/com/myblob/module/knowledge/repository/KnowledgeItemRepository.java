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

    Page<KnowledgeItem> findByCategory(String category, Pageable pageable);

    @Query("SELECT k FROM KnowledgeItem k WHERE k.title LIKE %:search% OR k.content LIKE %:search%")
    Page<KnowledgeItem> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT k.category FROM KnowledgeItem k ORDER BY k.category")
    List<String> findDistinctCategories();

    long countByCategory(String category);

    @Modifying
    @Query("UPDATE KnowledgeItem k SET k.viewCount = k.viewCount + 1 WHERE k.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /** 全文搜索 - 使用 PostgreSQL tsvector */
    @Query(value = """
        SELECT k.*, ts_rank(k.search_vector, query) AS rank
        FROM knowledge_item k, plainto_tsquery('simple', :query) AS query
        WHERE k.search_vector @@ query
        ORDER BY rank DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM knowledge_item k, plainto_tsquery('simple', :query) AS query
        WHERE k.search_vector @@ query
        """,
            nativeQuery = true)
    Page<KnowledgeItem> fullTextSearch(@Param("query") String query, Pageable pageable);

    /** 热门条目查询 */
    Page<KnowledgeItem> findByCategoryOrderByViewCountDesc(String category, Pageable pageable);
}
