package com.myblob.module.comments.repository;

import com.myblob.module.comments.entity.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmojiRepository extends JpaRepository<Emoji, Long> {

    List<Emoji> findByActiveTrueOrderBySortOrderAsc();

    List<Emoji> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);
}
