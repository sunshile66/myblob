package com.myblob.module.comments.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "emoji")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Emoji extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String category;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;
}
