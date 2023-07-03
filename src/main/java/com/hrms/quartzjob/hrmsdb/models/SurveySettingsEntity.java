package com.hrms.quartzjob.hrmsdb.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hrms.quartzjob.hrmsdb.enums.SurveyStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="survey_settings")
public class SurveySettingsEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    private long surveyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SurveyStatus status;
    private int surveyStartReminder;
    private int surveyEndReminder;
}
