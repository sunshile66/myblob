package com.myblob.module.core.repository;

import com.myblob.module.core.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByActiveTrueAndPublishTimeBeforeOrderByPinnedDescSortAscPublishTimeDesc(LocalDateTime now);
}
