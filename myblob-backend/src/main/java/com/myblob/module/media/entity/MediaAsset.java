package com.myblob.module.media.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "media_asset", indexes = {
        @Index(name = "idx_media_user_type", columnList = "user_id, media_type"),
        @Index(name = "idx_media_type", columnList = "media_type"),
        @Index(name = "idx_media_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MediaAsset extends BaseEntity {

    public enum MediaType {
        IMAGE, FILE, VIDEO
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String file;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(length = 100)
    private String title;

    @Column(name = "alt_text", length = 150)
    private String altText;
}
