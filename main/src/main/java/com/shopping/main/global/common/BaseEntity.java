package com.shopping.main.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime regTime; // 등록시간

    @LastModifiedDate
    private LocalDateTime updateTime; // 수정시간

    @CreatedBy
    @Column(updatable = false)
    private String createdBy; // 등록자 (ID 또는 이메일)

    @LastModifiedBy
    private String modifiedBy; // 수정자 (ID 또는 이메일)
}