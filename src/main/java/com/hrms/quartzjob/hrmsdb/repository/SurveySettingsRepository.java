package com.hrms.quartzjob.hrmsdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.quartzjob.hrmsdb.models.SurveySettingsEntity;


public interface SurveySettingsRepository extends JpaRepository<SurveySettingsEntity,Long> {

    Optional<SurveySettingsEntity> findBySurveyId(long surveyId);
    
}
