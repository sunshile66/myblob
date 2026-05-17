package com.myblob.module.apigateway.repository;

import com.myblob.module.apigateway.entity.APICallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface APICallLogRepository extends JpaRepository<APICallLog, Long> {

    Page<APICallLog> findByApiKeyIdOrderByCreatedAtDesc(Long apiKeyId, Pageable pageable);
}
