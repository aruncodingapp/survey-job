package com.hrms.quartzjob.hrmsdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantAttendeesEntity;


public interface SurveyParticipantAttendeesRepository extends JpaRepository<SurveyParticipantAttendeesEntity, Long> {

    SurveyParticipantAttendeesEntity findByParticipantId(long participantId);
       
}
