package com.myblob.module.apigateway.repository;

import com.myblob.module.apigateway.entity.APIKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface APIKeyRepository extends JpaRepository<APIKey, Long> {

    List<APIKey> findByUserId(Long userId);

    Optional<APIKey> findByApiKey(String apiKey);
}
