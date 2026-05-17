package com.myblob.module.social.repository;

import com.myblob.module.social.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    List<SocialAccount> findByUserId(Long userId);

    Optional<SocialAccount> findByProviderAndOpenid(SocialAccount.Provider provider, String openid);
}
