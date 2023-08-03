package com.hrms.quartzjob;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.quartzjob.hrmsdb.models.service.EmailService;
import com.hrms.quartzjob.hrmsdb.repository.SurveyInvitationHistoryRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyMessageRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyParticipantRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveySettingsRepository;
import com.hrms.quartzjob.jobs.SendInvitationJob;

@RestController
public class QuartzController {

    private Scheduler scheduler;
    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SurveyMessageRepository surveyMessageRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SurveyInvitationHistoryRepository invitationHistoryRepository;

    @Autowired
    private SurveyParticipantRepository participantRepository;

    @Autowired
    private SurveySettingsRepository settingsRepository;

    @Value("${survey.ui.domain}")
    String uiDomain;

    @Value ("${survey.authToken}")
    String authToken;

    public QuartzController() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();

        scheduler.start();
    }

    @PostMapping("/trigger-invitation/{number}")
    public void triggerQuartzJob(@PathVariable long number) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("number", number);

        JobDetail jobDetail = JobBuilder.newJob(SendInvitationJob.class)
                .withIdentity("myJob_" + System.currentTimeMillis(), "group1")
                .usingJobData(dataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger_" + System.currentTimeMillis(), "group1")
                .startNow()
                .build();
        scheduler.getContext().put("survey_repo", surveyRepository);
        scheduler.getContext().put("msg_repo", surveyMessageRepository);
        scheduler.getContext().put("email_service", emailService);
        scheduler.getContext().put("invitationHistory_repo", invitationHistoryRepository);
        scheduler.getContext().put("participant_repo", participantRepository);
        scheduler.getContext().put("settings_repo", settingsRepository);
        scheduler.getContext().put("ui_domain_key", uiDomain);
        scheduler.getContext().put("auth_token", authToken);
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
