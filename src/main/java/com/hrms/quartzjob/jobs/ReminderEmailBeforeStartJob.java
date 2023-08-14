package com.hrms.quartzjob.jobs;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.google.gson.JsonObject;
import com.hrms.quartzjob.config.URLRepository;
import com.hrms.quartzjob.hrmsdb.enums.InvitationSendStatus;
import com.hrms.quartzjob.hrmsdb.models.ISurveyDto;
import com.hrms.quartzjob.hrmsdb.models.SurveyEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyInvitationHistoryEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyMessageEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveySettingsEntity;
import com.hrms.quartzjob.hrmsdb.models.service.EmailService;
import com.hrms.quartzjob.hrmsdb.models.service.RestApi;
import com.hrms.quartzjob.hrmsdb.repository.SurveyInvitationHistoryRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyMessageRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyParticipantRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveySettingsRepository;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@DisallowConcurrentExecution
public class ReminderEmailBeforeStartJob extends QuartzJobBean {

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

    @Value("${survey.authToken}")
    String authToken;

    String qrCodeAttachment;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("---------------------REMINDER EMAIL BEFORE START JOB START----------------------------");
        List<ISurveyDto> existingAllScheduledSurvey = surveyRepository.getAllByScheduled();
        if (!existingAllScheduledSurvey.isEmpty()) {
            for (ISurveyDto survey : existingAllScheduledSurvey) {
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
                        int surveyStartReminder = surveySettingsEntity.getSurveyStartReminder();
                        LocalDate startDate = surveySettingsEntity.getStartDate();
                        LocalDate currentDate = LocalDate.now();
                        if (currentDate.plusDays(surveyStartReminder).isEqual(startDate)) {
                            for (SurveyParticipantEntity user : existingUsers) {
                                if (surveyMessage.isPresent()) {
                                    SurveyMessageEntity msgEntity = surveyMessage.get();
                                    SurveyMessageEntity surveyMessageEntity = new SurveyMessageEntity();
                                    surveyMessageEntity.setSurveyId(surveyEntity.getId());
                                    surveyMessageEntity
                                            .setReminderStartEmailSubject(msgEntity.getReminderStartEmailSubject());
                                    surveyMessageEntity.setReminderStartEmail(msgEntity.getReminderStartEmail());
                                    surveyMessageEntity.setReminderStartEmail(
                                            surveyMessageEntity.getReminderStartEmail().replace("{{name}}",
                                                    user.getName()));
                                    surveyMessageEntity
                                            .setReminderStartEmail(surveyMessageEntity.getReminderStartEmail().replace(
                                                    "{{URL}}",
                                                    uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey()
                                                            + "/"
                                                            + user.getUrlKey()));
                                    surveyMessageEntity.setReminderStartEmail(
                                            surveyMessageEntity.getReminderStartEmail().replace("{{start_Date}}",
                                                    surveySettingsEntity.getStartDate() + " At: "
                                                            + surveySettingsEntity.getStartTime()));
                                    surveyMessageEntity.setReminderStartEmail(
                                            surveyMessageEntity.getReminderStartEmail().replace("{{end_Date}}",
                                                    surveySettingsEntity.getEndDate() + " At: "
                                                            + surveySettingsEntity.getEndTime()));
                                    String qrCodeUrl = uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey()
                                            + "/"
                                            + user.getUrlKey();
                                    String qrCode = generateQRCode(qrCodeUrl);
                                    surveyMessageEntity.setReminderStartEmail(
                                            surveyMessageEntity.getReminderStartEmail().replace("{{QR_Code}}", qrCode));
                                    try {
                                        saveHistoryAndSendReminderEmail(user, surveyMessageEntity);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("---------------------REMINDER EMAIL BEFORE START JOB END----------------------------");
        }
    }

    public String generateQRCode(String url) {
        ByteArrayOutputStream out = QRCode.from(url).to(ImageType.PNG).stream();
        byte[] qrCodeBytes = out.toByteArray();
        String base64QRCode = Base64.getEncoder().encodeToString(qrCodeBytes);
        qrCodeAttachment = base64QRCode;
        return "data:image/png;base64, " + base64QRCode;
    }

    private InvitationSendStatus saveHistoryAndSendReminderEmail(SurveyParticipantEntity user,
            SurveyMessageEntity template) {
        SurveyInvitationHistoryEntity surveyHistory = new SurveyInvitationHistoryEntity();
        surveyHistory.setSubject(template.getReminderStartEmailSubject());
        surveyHistory.setBody(template.getReminderStartEmail());
        surveyHistory.setStatus(InvitationSendStatus.PENDING);
        surveyHistory.setFailReason("");
        surveyHistory.setEmailTo(user.getEmail());
        surveyHistory.setSurveyId(template.getSurveyId());
        sendWhatsAppNotification(user);
        invitationHistoryRepository.save(surveyHistory);
        if (user.getEmail().endsWith("@gmail.com")) {
            surveyHistory = emailService.sendEmailWithAttachment(surveyHistory, qrCodeAttachment);
        } else {
            surveyHistory = emailService.sendEmail(surveyHistory);
        }
        invitationHistoryRepository.save(surveyHistory);
        user.setEmailInvitationSent(true);
        participantRepository.save(user);
        return surveyHistory.getStatus();
    }

    private void sendWhatsAppNotification(SurveyParticipantEntity user) {
        RestApi restApi = new RestApi();
        String baseUrl = URLRepository.whatsAppUrl;

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messaging_product", "whatsapp");
        jsonObject.addProperty("recipient_type", "individual");
        jsonObject.addProperty("to", user.getMobile());
        jsonObject.addProperty("type", "template");

        JsonObject templateObject = new JsonObject();
        templateObject.addProperty("name", "survey_reminder");

        JsonObject languageObject = new JsonObject();
        languageObject.addProperty("code", "en_GB");

        templateObject.add("language", languageObject);
        jsonObject.add("template", templateObject);

        restApi.whatsAppSendNotification(baseUrl, jsonObject, HttpMethod.POST, authToken);
    }
}
