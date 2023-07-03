package com.hrms.quartzjob.hrmsdb.models;

public interface ISurveyDto {

    Long getId();

    String getName();

    Long getParticipantCount();

    String getDescription();
    
    long getLanguageId();

    Long getAttendeesCount();
}
