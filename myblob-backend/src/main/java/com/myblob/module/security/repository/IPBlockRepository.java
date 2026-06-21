package com.myblob.module.security.repository;

import com.myblob.module.security.entity.IPBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPBlockRepository extends JpaRepository<IPBlock, Long> {

    Optional<IPBlock> findByIpAndActiveTrue(String ip);

    Page<IPBlock> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
