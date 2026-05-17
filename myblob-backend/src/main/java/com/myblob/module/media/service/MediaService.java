package com.myblob.module.media.service;

import com.myblob.common.BusinessException;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.media.entity.MediaAsset;
import com.myblob.module.media.repository.MediaAssetRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;

    @Value("${myblob.upload.path:./media}")
    private String uploadPath;

    @Transactional
    public MediaAsset upload(MultipartFile file, String type) throws IOException {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        String subDir = "image".equals(type) ? "images" : ("video".equals(type) ? "videos" : "files");
        Path uploadDir = Paths.get(uploadPath, subDir);
        Files.createDirectories(uploadDir);
        Files.write(uploadDir.resolve(filename), file.getBytes());

        MediaAsset.MediaType mediaType = MediaAsset.MediaType.FILE;
        if (type != null) {
            try {
                mediaType = MediaAsset.MediaType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        MediaAsset asset = MediaAsset.builder()
                .user(user)
                .file("/media/" + subDir + "/" + filename)
                .mediaType(mediaType)
                .title(originalFilename)
                .build();

        return mediaAssetRepository.save(asset);
    }
}
