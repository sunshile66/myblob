package com.myblob.module.filemanager.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "file_folder",
        indexes = {
                @Index(name = "idx_folder_user", columnList = "user_id"),
                @Index(name = "idx_folder_parent", columnList = "parent_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_folder_name_parent", columnNames = {"user_id", "name", "parent_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Folder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;
}
