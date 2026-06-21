package com.myblob.module.core.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcement", indexes = {
        @Index(name = "idx_announcement_active_pinned", columnList = "is_active, is_pinned")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Announcement extends BaseEntity {

    public enum AnnouncementType {
        BAR, MODAL
    }

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "announcement_type", nullable = false, length = 20)
    @Builder.Default
    private AnnouncementType announcementType = AnnouncementType.BAR;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean pinned = false;

    @Column(nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer sort = 0;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "show_delay", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("5")
    private Integer showDelay = 5;

    @Column(name = "auto_close", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean autoClose = false;

    @Column(name = "auto_close_time", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("30")
    private Integer autoCloseTime = 30;
}
