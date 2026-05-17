package com.myblob.module.media.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.blog.dto.MediaAssetDTO;
import com.myblob.module.media.entity.MediaAsset;
import com.myblob.module.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/assets/")
    public ResponseEntity<ApiResponse<MediaAssetDTO>> uploadAsset(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "media_type", defaultValue = "image") String type) throws IOException {
        MediaAsset asset = mediaService.upload(file, type);
        MediaAssetDTO dto = MediaAssetDTO.builder()
                .id(asset.getId())
                .file(asset.getFile())
                .mediaType(asset.getMediaType().name().toLowerCase())
                .title(asset.getTitle())
                .altText(asset.getAltText())
                .build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
