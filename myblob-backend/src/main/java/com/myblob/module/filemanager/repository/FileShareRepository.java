package com.myblob.module.filemanager.repository;

import com.myblob.module.filemanager.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    Optional<FileShare> findByShareCodeAndDeletedFalse(String shareCode);
}
