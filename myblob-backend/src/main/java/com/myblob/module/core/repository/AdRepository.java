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

    List<Ad> findByActiveTrueOrderByPositionAscSortAscCreatedAtDesc();

    @Query("SELECT a FROM Ad a WHERE a.active = true " +
           "AND (:position IS NULL OR a.position = :position) " +
           "AND (a.startTime IS NULL OR a.startTime <= :now) " +
           "AND (a.endTime IS NULL OR a.endTime >= :now) " +
           "ORDER BY a.position ASC, a.sort ASC, a.createdAt DESC")
    List<Ad> findActiveAds(@Param("position") String position, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Ad a SET a.clickCount = a.clickCount + 1 WHERE a.id = :adId")
    void incrementClickCount(@Param("adId") Long adId);
}
