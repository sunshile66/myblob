package com.myblob.module.security.repository;

import com.myblob.module.security.entity.IPBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPBlockRepository extends JpaRepository<IPBlock, Long> {

    Optional<IPBlock> findByIpAndActiveTrue(String ip);
}
