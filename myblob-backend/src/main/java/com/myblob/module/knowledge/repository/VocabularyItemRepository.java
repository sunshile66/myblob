package com.myblob.module.knowledge.repository;

import com.myblob.module.knowledge.entity.VocabularyItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyItemRepository extends JpaRepository<VocabularyItem, Long> {

    @Query("SELECT v FROM VocabularyItem v WHERE v.category = :category AND v.deleted = false")
    Page<VocabularyItem> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT v FROM VocabularyItem v WHERE v.difficulty = :difficulty AND v.deleted = false")
    Page<VocabularyItem> findByDifficulty(@Param("difficulty") Integer difficulty, Pageable pageable);

    @Query("SELECT v FROM VocabularyItem v WHERE v.category = :category AND v.difficulty = :difficulty AND v.deleted = false")
    Page<VocabularyItem> findByCategoryAndDifficulty(@Param("category") String category, @Param("difficulty") Integer difficulty, Pageable pageable);

    @Query("SELECT v FROM VocabularyItem v WHERE (v.word LIKE %:search% OR v.definition LIKE %:search%) AND v.deleted = false")
    Page<VocabularyItem> search(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT * FROM vocabulary_item WHERE deleted = false ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<VocabularyItem> findRandom(@Param("count") int count);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VocabularyItem v WHERE v.word = :word AND v.deleted = false")
    boolean existsByWord(@Param("word") String word);

    @Query("SELECT COUNT(v) FROM VocabularyItem v WHERE v.category = :category AND v.deleted = false")
    long countByCategory(@Param("category") String category);
}
