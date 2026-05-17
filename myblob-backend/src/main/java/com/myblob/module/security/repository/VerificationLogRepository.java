package com.myblob.module.security.repository;

import com.myblob.module.security.entity.VerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationLogRepository extends JpaRepository<VerificationLog, Long> {
    
    long countByTargetAndCreatedAtAfter(String target, LocalDateTime after);
    
    Optional<VerificationLog> findTopByTargetAndStatusOrderByCreatedAtDesc(String target, String status);
}
