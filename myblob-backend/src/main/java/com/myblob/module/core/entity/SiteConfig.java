package com.myblob.module.core.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "site_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SiteConfig extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(length = 200)
    private String description;
}
