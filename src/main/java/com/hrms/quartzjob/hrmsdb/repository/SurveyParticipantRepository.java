package com.hrms.quartzjob.hrmsdb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantEntity;



@Repository
public interface SurveyParticipantRepository extends CrudRepository<SurveyParticipantEntity, Long> {
   
    List<SurveyParticipantEntity> findBySurveyId(long surveyId);
}   
