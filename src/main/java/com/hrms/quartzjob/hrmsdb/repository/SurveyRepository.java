package com.hrms.quartzjob.hrmsdb.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.hrms.quartzjob.hrmsdb.models.ISurveyDto;

import com.hrms.quartzjob.hrmsdb.models.SurveyEntity;


public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

    @Query(value ="SELECT s.id AS id, s.name AS name, st.description, COUNT(sp.name) AS participantCount, 1 AS languageId FROM surveydb.survey s JOIN surveydb.survey_settings ss ON s.id = ss.survey_id AND s.is_published = true AND s.is_suspended = false AND NOW() < STR_TO_DATE(CONCAT(ss.start_date, ' ', ss.start_time), '%Y-%m-%d %H:%i:%s') LEFT JOIN surveydb.survey_participant sp ON s.id = sp.survey_id JOIN surveydb.survey_translation st ON s.id = st.survey_id GROUP BY s.id, s.name",nativeQuery = true)
    List<ISurveyDto> getAllByScheduled();
    
     @Query(value = "SELECT s.id as id, s.name as name, st.description, count(sp.name) as participantCount, count(spa.survey_id) as attendeesCount, 1 AS languageId FROM surveydb.survey s JOIN surveydb.survey_settings ss ON s.id = ss.survey_id AND s.is_published = true AND s.is_suspended = false AND NOW() BETWEEN STR_TO_DATE(CONCAT(ss.start_date, ' ', ss.start_time), '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE(CONCAT(ss.end_date, ' ', ss.end_time), '%Y-%m-%d %H:%i:%s') left JOIN surveydb.survey_participant sp on s.id = sp.survey_id left JOIN surveydb.survey_participant_attendees spa on s.id = spa.survey_id AND sp.id = spa.participant_id JOIN surveydb.survey_translation st ON s.id = st.survey_id group by s.id, s.name", nativeQuery = true)
    List<ISurveyDto> getAllByActive();
}
