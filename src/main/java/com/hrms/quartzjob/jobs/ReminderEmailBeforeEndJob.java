package com.hrms.quartzjob.jobs;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.hrms.quartzjob.hrmsdb.enums.InvitationSendStatus;
import com.hrms.quartzjob.hrmsdb.models.ISurveyDto;
import com.hrms.quartzjob.hrmsdb.models.SurveyEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyInvitationHistoryEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyMessageEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveySettingsEntity;
import com.hrms.quartzjob.hrmsdb.models.service.EmailService;
import com.hrms.quartzjob.hrmsdb.repository.SurveyInvitationHistoryRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyMessageRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyParticipantRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveySettingsRepository;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

public class ReminderEmailBeforeEndJob extends QuartzJobBean{
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

    @Autowired
    private SurveyRepository surveyRepository;

    @Value("${survey.ui.domain}")
    String uiDomain;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("---------------------REMINDER EMAIL BEFORE END JOB START----------------------------");
        List<ISurveyDto> existingAllActiveSurvey = surveyRepository.getAllByActive();
        if(!existingAllActiveSurvey.isEmpty())
        {
        for (ISurveyDto survey : existingAllActiveSurvey) {
            Optional<SurveyEntity> existingSurvey = surveyRepository.findById(survey.getId());
            if (existingSurvey.isPresent() && existingSurvey.get().getIsPublished()) {
                SurveyEntity surveyEntity = existingSurvey.get();
                Optional<SurveyMessageEntity> surveyMessage = surveyMessageRepository
                        .findBySurveyId(surveyEntity.getId());
                Optional<SurveySettingsEntity> existingSurveySettings = settingsRepository
                        .findBySurveyId(surveyEntity.getId());
                if (existingSurveySettings.isPresent()) {
                    SurveySettingsEntity surveySettingsEntity = existingSurveySettings.get();
                    List<SurveyParticipantEntity> existingUsers = participantRepository
                            .findBySurveyId(surveyEntity.getId());
                    int surveyEndReminder = surveySettingsEntity.getSurveyEndReminder();
                    LocalDate endDate = surveySettingsEntity.getEndDate();
                    LocalDate currentDate = LocalDate.now();
                    if (currentDate.plusDays(surveyEndReminder).isEqual(endDate)) {
                        for (SurveyParticipantEntity user : existingUsers) {
                            if (surveyMessage.isPresent()) {
                                SurveyMessageEntity surveyMessageEntity = surveyMessage.get();
                                surveyMessageEntity.setReminderEmail(
                                        surveyMessageEntity.getReminderEmail().replace("{{name}}", user.getName()));
                                surveyMessageEntity
                                        .setReminderEmail(surveyMessageEntity.getReminderEmail().replace("{{URL}}",
                                                uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey() + "/"
                                                        + user.getUrlKey()));
                                surveyMessageEntity.setReminderEmail(
                                        surveyMessageEntity.getReminderEmail().replace("{{start_Date}}",
                                                surveySettingsEntity.getStartDate() + " At: "
                                                        + surveySettingsEntity.getStartTime()));
                                surveyMessageEntity.setReminderEmail(
                                        surveyMessageEntity.getReminderEmail().replace("{{end_Date}}",
                                                surveySettingsEntity.getEndDate() + " At: "
                                                        + surveySettingsEntity.getEndTime()));
                                String qrCodeUrl = uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey()
                                        + "/"
                                        + user.getUrlKey();
                                String qrCode = generateQRCode(qrCodeUrl);
                                surveyMessageEntity.setReminderEmail(
                                        surveyMessageEntity.getReminderEmail().replace("{{QR_Code}}", qrCode));
                                try {
                                    saveHistoryAndSendInvitationEmail(user, surveyMessageEntity);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("---------------------REMINDER EMAIL BEFORE END JOB END----------------------------");
    }
}

    public String generateQRCode(String url) {
        ByteArrayOutputStream out = QRCode.from(url).to(ImageType.PNG).stream();
        byte[] qrCodeBytes = out.toByteArray();
        String base64QRCode = Base64.getEncoder().encodeToString(qrCodeBytes);
        return "data:image/png;base64, " + base64QRCode;
    }

    private InvitationSendStatus saveHistoryAndSendInvitationEmail(SurveyParticipantEntity user,
            SurveyMessageEntity template) {
        SurveyInvitationHistoryEntity surveyHistory = new SurveyInvitationHistoryEntity();
        surveyHistory.setSubject("");
        surveyHistory.setBody(template.getReminderEmail()+"end wala ");
        surveyHistory.setStatus(InvitationSendStatus.PENDING);
        surveyHistory.setFailReason("");
        surveyHistory.setEmailTo(user.getEmail());
        invitationHistoryRepository.save(surveyHistory);
        surveyHistory = emailService.sendEmail(surveyHistory);
        invitationHistoryRepository.save(surveyHistory);
        user.setEmailInvitationSent(true);
        participantRepository.save(user);
        return surveyHistory.getStatus();
    }
}
