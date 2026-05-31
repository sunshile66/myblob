package com.myblob.module.news.repository;

import com.myblob.module.news.entity.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {

    List<NewsSource> findByEnabledTrueOrderByPriorityAsc();

    List<NewsSource> findAllByOrderByPlatformNameAsc();

    List<NewsSource> findByCategory(String category);

    List<NewsSource> findByLanguage(String language);

    List<NewsSource> findByPlatformName(String platformName);

    long countByCategory(String category);
}
