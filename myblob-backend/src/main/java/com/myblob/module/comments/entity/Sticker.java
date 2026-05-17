package com.myblob.module.comments.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sticker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sticker extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(length = 50)
    private String category;

    @Column(name = "is_gif", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean gif = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;
}
