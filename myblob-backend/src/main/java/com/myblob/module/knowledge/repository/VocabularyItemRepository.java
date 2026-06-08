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

    Page<VocabularyItem> findByCategory(String category, Pageable pageable);

    Page<VocabularyItem> findByDifficulty(Integer difficulty, Pageable pageable);

    Page<VocabularyItem> findByCategoryAndDifficulty(String category, Integer difficulty, Pageable pageable);

    @Query("SELECT v FROM VocabularyItem v WHERE v.word LIKE %:search% OR v.definition LIKE %:search%")
    Page<VocabularyItem> search(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT * FROM vocabulary_item ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<VocabularyItem> findRandom(@Param("count") int count);

    boolean existsByWord(String word);

    long countByCategory(String category);
}
