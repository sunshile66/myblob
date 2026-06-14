package com.myblob.module.media.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 定期清理临时文件（图片编辑器缓存、上传临时文件等）。
 * 默认每小时执行一次，删除超过 1 小时的临时文件。
 */
@Slf4j
@Service
public class TempFileCleanupService {

    @Value("${myblob.upload-path:./media}")
    private String uploadPath;

    /** 临时文件最大保留时间（分钟） */
    private static final long MAX_AGE_MINUTES = 60;

    @Scheduled(fixedRate = 3600000, initialDelay = 300000) // 每小时，启动后5分钟首次
    public void cleanupTempFiles() {
        Path tempDir = Paths.get(uploadPath, "temp");
        if (!Files.exists(tempDir)) return;

        Instant cutoff = Instant.now().minus(MAX_AGE_MINUTES, ChronoUnit.MINUTES);
        try {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.lastModifiedTime().toInstant().isBefore(cutoff)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(tempDir) && isEmpty(dir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Temp file cleanup failed: {}", e.getMessage());
        }
    }

    private boolean isEmpty(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.findAny().isEmpty();
        }
    }
}
