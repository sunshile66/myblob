package com.myblob.module.news.repository;

import com.myblob.module.news.entity.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    @Query("SELECT n FROM NewsItem n WHERE n.sourceUrl = :url AND n.deleted = false")
    Optional<NewsItem> findBySourceUrl(@Param("url") String sourceUrl);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NewsItem n WHERE n.sourceUrl = :url AND n.deleted = false")
    boolean existsBySourceUrl(@Param("url") String sourceUrl);

    @Query("SELECT n.sourceUrl FROM NewsItem n WHERE n.sourceUrl IN :urls AND n.deleted = false")
    List<String> findExistingSourceUrls(@Param("urls") List<String> urls);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false")
    Page<NewsItem> findPublishedNews(Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false AND n.category = :category")
    Page<NewsItem> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false AND n.sourcePlatform = :source")
    Page<NewsItem> findBySourcePlatform(@Param("source") String source, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false AND n.language = :language")
    Page<NewsItem> findByLanguage(@Param("language") String language, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.summary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<NewsItem> searchNews(@Param("search") String search, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false AND n.category = :category " +
           "AND n.language = :language")
    Page<NewsItem> findByCategoryAndLanguage(@Param("category") String category,
                                              @Param("language") String language, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.deleted = false ORDER BY n.publishedAt DESC")
    Page<NewsItem> findAllIncludingFiltered(Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.deleted = false AND n.qualityScore >= :minScore ORDER BY n.publishedAt DESC")
    Page<NewsItem> findByMinScore(@Param("minScore") Integer minScore, Pageable pageable);

    @Query("SELECT n FROM NewsItem n WHERE n.deleted = false AND n.publishedAtAfter = :dateTime")
    List<NewsItem> findByPublishedAtAfter(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT COUNT(n) FROM NewsItem n WHERE n.isFiltered = false AND n.deleted = false")
    long countByIsFilteredFalse();

    @Modifying
    @Query("DELETE FROM NewsItem n WHERE n.fetchedAt < :dateTime")
    void deleteByFetchedAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
