package com.myblob.module.interactions.repository;

import com.myblob.module.interactions.entity.BoardMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardMessageRepository extends JpaRepository<BoardMessage, Long> {

    Page<BoardMessage> findByIsPublicTrueOrderByIdDesc(Pageable pageable);
}
