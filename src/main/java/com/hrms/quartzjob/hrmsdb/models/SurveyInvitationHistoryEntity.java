package com.hrms.quartzjob.hrmsdb.models;
 

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.hrms.quartzjob.hrmsdb.enums.InvitationSendStatus;

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
@Table(name = "survey_invitation_history")
public class SurveyInvitationHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "NVARCHAR(100)", nullable = false)
    private String subject;

    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @Column(nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;
    
    private InvitationSendStatus status;

    @Column(columnDefinition = "text", nullable = false)
    private String emailTo; 

    @Column(columnDefinition = "VARCHAR(500)", nullable = true)
    private String failReason;

}
