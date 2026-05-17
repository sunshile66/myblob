package com.myblob.module.filemanager.repository;

import com.myblob.module.filemanager.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Page<FileEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<FileEntity> findByUserIdAndFolderIdOrderByCreatedAtDesc(Long userId, Long folderId, Pageable pageable);

    Page<FileEntity> findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
