package com.myblob.module.security.repository;

import com.myblob.module.security.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    Optional<UserSession> findBySessionKey(String sessionKey);
}
