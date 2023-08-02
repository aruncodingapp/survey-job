package com.hrms.quartzjob.hrmsdb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hrms.quartzjob.hrmsdb.models.SurveyInvitationHistoryEntity;



@Repository
public interface SurveyInvitationHistoryRepository extends CrudRepository<SurveyInvitationHistoryEntity, Long> {
 
    List<SurveyInvitationHistoryEntity> findBySurveyId(long surveyId);
}