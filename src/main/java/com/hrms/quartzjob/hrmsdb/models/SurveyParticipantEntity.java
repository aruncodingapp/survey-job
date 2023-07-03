package com.hrms.quartzjob.hrmsdb.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.hrms.quartzjob.hrmsdb.enums.SurveyStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
@Table(name = "survey_participant")
public class SurveyParticipantEntity extends BaseEntity {

    public SurveyParticipantEntity(){
        this.surveyStatus = SurveyStatus.DRAFT;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true, unique = false)
    private long internalUserId;

    @Column(columnDefinition = "NVARCHAR(100)", nullable = false, unique = false)
    private String name;

    @Column(columnDefinition = "NVARCHAR(250)", nullable = false, unique = false)
    private String email;

    @Column(nullable = true)
    private String mobile;

    @Column(name = "survey_id")
    private long surveyId;

    @Column(nullable = true)
    private SurveyStatus surveyStatus;

    @Column(columnDefinition = "boolean default false") 
    private boolean isWhatsappInvitationSent;

    @Column(columnDefinition = "boolean default false") 
    private boolean isEmailInvitationSent;

    private String urlKey;

}
