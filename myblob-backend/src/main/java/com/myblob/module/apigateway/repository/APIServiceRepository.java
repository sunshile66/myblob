package com.myblob.module.apigateway.repository;

import com.myblob.module.apigateway.entity.APIService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APIServiceRepository extends JpaRepository<APIService, Long> {

    List<APIService> findByActiveTrue();
}
