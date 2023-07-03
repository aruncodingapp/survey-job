package com.hrms.quartzjob.hrmsdb.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hrms.quartzjob.hrmsdb.models.SurveyMessageEntity;


public interface SurveyMessageRepository extends JpaRepository<SurveyMessageEntity,Long>{
    
    @Query("SELECT s FROM SurveyMessageEntity s WHERE s.surveyId=:surveyId AND s.companyId=:companyId")
    SurveyMessageEntity getAll(long surveyId, Long companyId);

    Optional<SurveyMessageEntity> findBySurveyId(long surveyId);
}
