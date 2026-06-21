package com.myblob.module.core.repository;

import com.myblob.module.core.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    List<Ad> findByActiveTrueAndPositionAndStartTimeBeforeAndEndTimeAfterOrActiveTrueAndPositionAndStartTimeIsNullAndEndTimeIsNull(
            String position, LocalDateTime now1, LocalDateTime now2, String position2);

    List<Ad> findByActiveTrueOrderByPositionAscSortAscCreatedAtDesc();

    @Modifying
    @Query("UPDATE Ad a SET a.clickCount = a.clickCount + 1 WHERE a.id = :adId")
    void incrementClickCount(@Param("adId") Long adId);
}
