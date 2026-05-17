package com.myblob.module.comments.repository;

import com.myblob.module.comments.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    List<Sticker> findByActiveTrueOrderBySortOrderAsc();

    List<Sticker> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);

    List<Sticker> findByGifTrueAndActiveTrueOrderBySortOrderAsc();
}
