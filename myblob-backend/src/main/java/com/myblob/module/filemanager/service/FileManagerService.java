package com.myblob.module.filemanager.service;

import com.myblob.common.BusinessException;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.filemanager.entity.FileEntity;
import com.myblob.module.filemanager.entity.FileShare;
import com.myblob.module.filemanager.entity.Folder;
import com.myblob.module.filemanager.repository.FileRepository;
import com.myblob.module.filemanager.repository.FileShareRepository;
import com.myblob.module.filemanager.repository.FolderRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileManagerService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileShareRepository fileShareRepository;
    private final UserRepository userRepository;

    @Value("${myblob.upload.path:./uploads}")
    private String uploadPath;

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getMyFiles(Long folderId, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<FileEntity> files;
        if (folderId != null) {
            files = fileRepository.findByUserIdAndFolderIdOrderByCreatedAtDesc(userId, folderId, pageable);
        } else {
            files = fileRepository.findByUserIdAndFolderIdIsNullOrderByCreatedAtDesc(userId, pageable);
        }
        return PageResponse.of(files.map(this::toFileMap));
    }

    @Transactional
    public Map<String, Object> uploadFile(MultipartFile multipartFile, Long folderId) throws IOException {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        String originalFilename = multipartFile.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String storedName = UUID.randomUUID() + ext;

        Path userDir = Paths.get(uploadPath, "files", String.valueOf(userId));
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve(storedName);
        multipartFile.transferTo(filePath.toFile());

        String fileType = determineFileType(multipartFile.getContentType());

        FileEntity entity = FileEntity.builder()
                .user(user)
                .file("files/" + userId + "/" + storedName)
                .filename(originalFilename != null ? originalFilename : storedName)
                .fileType(fileType)
                .fileSize(multipartFile.getSize())
                .mimeType(multipartFile.getContentType())
                .isPublic(false)
                .build();

        if (folderId != null) {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> BusinessException.notFound("文件夹"));
            entity.setFolder(folder);
        }

        return toFileMap(fileRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyFolders(Long parentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Folder> folders = parentId != null
                ? folderRepository.findByUserIdAndParentIdOrderByNameAsc(userId, parentId)
                : folderRepository.findByUserIdAndParentIdIsNullOrderByNameAsc(userId);
        return folders.stream().map(this::toFolderMap).toList();
    }

    @Transactional
    public Map<String, Object> createFolder(String name, Long parentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);
        Folder.FolderBuilder builder = Folder.builder().user(user).name(name);
        if (parentId != null) {
            Folder parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> BusinessException.notFound("父文件夹"));
            builder.parent(parent);
        }
        return toFolderMap(folderRepository.save(builder.build()));
    }

    @Transactional
    public void deleteFile(Long fileId) {
        Long userId = SecurityUtil.getCurrentUserId();
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> BusinessException.notFound("文件"));
        if (!file.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此文件");
        }
        fileRepository.delete(file);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> BusinessException.notFound("文件夹"));
        if (!folder.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此文件夹");
        }
        folderRepository.delete(folder);
    }

    private Map<String, Object> toFileMap(FileEntity file) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", file.getId());
        map.put("file", "/media/" + file.getFile());
        map.put("filename", file.getFilename());
        map.put("file_type", file.getFileType());
        map.put("file_size", file.getFileSize());
        map.put("mime_type", file.getMimeType());
        map.put("folder_id", file.getFolder() != null ? file.getFolder().getId() : null);
        map.put("is_public", file.getIsPublic());
        map.put("created_at", file.getCreatedAt());
        map.put("updated_at", file.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toFolderMap(Folder folder) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", folder.getId());
        map.put("name", folder.getName());
        map.put("parent_id", folder.getParent() != null ? folder.getParent().getId() : null);
        map.put("created_at", folder.getCreatedAt());
        map.put("updated_at", folder.getUpdatedAt());
        return map;
    }

    private String determineFileType(String contentType) {
        if (contentType == null) return "other";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.startsWith("audio/")) return "audio";
        if (contentType.contains("pdf") || contentType.contains("document") || contentType.contains("text")) return "document";
        return "other";
    }
}
