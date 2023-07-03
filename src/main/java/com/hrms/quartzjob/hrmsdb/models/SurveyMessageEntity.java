package com.hrms.quartzjob.hrmsdb.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "survey_message_template")
public class SurveyMessageEntity extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String invitationEmail;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String reminderEmail;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String thankYouEmail;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String invitationWhatsapp;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String reminderWhatsapp;

    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String thankYouWhatsapp;
    
    @Column(columnDefinition = "text", nullable = false, unique = false)
    private String thankYouMessage;

    private long surveyId;
}
