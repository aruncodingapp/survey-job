package com.hrms.quartzjob.hrmsdb.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_logs")
public class JobLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "NVARCHAR(100)", nullable = false)
    private String name;

      @Column(columnDefinition = "NVARCHAR(100)", nullable = false)
    private String info;

    @Column(nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;
}
