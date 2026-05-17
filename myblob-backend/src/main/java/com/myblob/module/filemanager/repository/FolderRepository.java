package com.myblob.module.filemanager.repository;

import com.myblob.module.filemanager.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserIdAndParentIdIsNullOrderByNameAsc(Long userId);

    List<Folder> findByUserIdAndParentIdOrderByNameAsc(Long userId, Long parentId);
}
