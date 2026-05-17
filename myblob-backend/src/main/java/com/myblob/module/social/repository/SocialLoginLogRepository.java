package com.myblob.module.social.repository;

import com.myblob.module.social.entity.SocialLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialLoginLogRepository extends JpaRepository<SocialLoginLog, Long> {
}
