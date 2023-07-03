package com.hrms.quartzjob.hrmsdb.models;

import java.time.LocalDateTime;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "survey_participant_attendees")
public class SurveyParticipantAttendeesEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long participantId;
    private long surveyId;
    private LocalDateTime submissionStartDateTime;
    private LocalDateTime submittedOn;
    private long languageId;
    @Column(columnDefinition = "boolean default false") 
    private Boolean isSubmitted;
}
