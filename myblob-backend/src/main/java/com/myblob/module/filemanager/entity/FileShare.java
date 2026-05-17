package com.myblob.module.filemanager.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_share", indexes = {
        @Index(name = "idx_share_file", columnList = "file_id"),
        @Index(name = "idx_share_creator", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileShare extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @Column(name = "share_code", unique = true, nullable = false, length = 100)
    private String shareCode;

    @Column(name = "share_password", length = 100)
    private String sharePassword;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer downloadCount = 0;

    @Column(name = "max_downloads")
    private Integer maxDownloads;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
