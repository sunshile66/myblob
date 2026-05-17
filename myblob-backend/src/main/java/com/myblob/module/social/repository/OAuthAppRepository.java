package com.myblob.module.social.repository;

import com.myblob.module.social.entity.OAuthApp;
import com.myblob.module.social.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthAppRepository extends JpaRepository<OAuthApp, Long> {

    List<OAuthApp> findByActiveTrue();

    Optional<OAuthApp> findByProviderAndActiveTrue(SocialAccount.Provider provider);
}
