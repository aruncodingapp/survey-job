package com.hrms.quartzjob.hrmsdb.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Aditya
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "smtp")
public class SmtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String userName;
    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String password;
    @Column( nullable = false)
    private int port;
    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String host;
    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String fromEmail;
 
    @Column(nullable = true, updatable = false)
    @CreationTimestamp
    @JsonIgnore
    private LocalDateTime createdOn;
    @JsonIgnore
    @Column(nullable = true)
    private long createdBy;

    @Column(nullable = true)
    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime modifiedOn;
    @JsonIgnore
    @Column(nullable = true)
    long modifiedBy;

    @Column(columnDefinition = "NVARCHAR(500)", nullable = true)
    private String whatsAppKey;
}