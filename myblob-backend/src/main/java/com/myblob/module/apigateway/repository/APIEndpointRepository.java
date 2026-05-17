package com.myblob.module.apigateway.repository;

import com.myblob.module.apigateway.entity.APIEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APIEndpointRepository extends JpaRepository<APIEndpoint, Long> {

    List<APIEndpoint> findByServiceIdAndActiveTrue(Long serviceId);
}
