package com.hrms.quartzjob.hrmsdb.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {
  @Column(nullable = true, updatable = false)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdOn;

  @Column(nullable = true, updatable = true)
  @JsonIgnore
  private LocalDateTime modifiedOn;

  private Long createdBy;

  @Column(nullable = true)
  private Long modifiedBy;

  @Column(columnDefinition = "boolean default false")
  @JsonIgnore
  private boolean isDeleted;

  @PrePersist
  public void onInsert() {
    createdOn = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    modifiedOn = LocalDateTime.now();
  }

  @Column(columnDefinition = "integer default 1")
  private long companyId;


  
}
