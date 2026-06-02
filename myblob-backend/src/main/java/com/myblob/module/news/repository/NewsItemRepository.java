package com.myblob.module.news.repository;

import com.myblob.module.news.entity.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    Optional<NewsItem> findBySourceUrl(String sourceUrl);

    boolean existsBySourceUrl(String sourceUrl);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false")
    Page<NewsItem> findPublishedNews(Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.category = :category")
    Page<NewsItem> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.sourcePlatform = :source")
    Page<NewsItem> findBySourcePlatform(@Param("source") String source, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.language = :language")
    Page<NewsItem> findByLanguage(@Param("language") String language, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.summary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<NewsItem> searchNews(@Param("search") String search, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.category = :category " +
           "AND n.language = :language")
    Page<NewsItem> findByCategoryAndLanguage(@Param("category") String category,
                                              @Param("language") String language, Pageable pageable);

    @Query("SELECT n FROM NewsItem n ORDER BY n.publishedAt DESC")
    Page<NewsItem> findAllIncludingFiltered(Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.qualityScore >= :minScore ORDER BY n.publishedAt DESC")
    Page<NewsItem> findByMinScore(@Param("minScore") Integer minScore, Pageable pageable);

    List<NewsItem> findByPublishedAtAfter(LocalDateTime dateTime);

    long countByIsFilteredFalse();

    void deleteByFetchedAtBefore(LocalDateTime dateTime);
}
