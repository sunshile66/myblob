package com.myblob.module.core.repository;

import com.myblob.module.core.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByActiveTrueAndPublishTimeBeforeOrderByPinnedDescSortAscPublishTimeDesc(LocalDateTime now);

    @Query("SELECT a FROM Announcement a WHERE a.active = true " +
           "AND (a.publishTime IS NULL OR a.publishTime <= :now) " +
           "ORDER BY a.pinned DESC, a.sort ASC, a.publishTime DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now);
}
