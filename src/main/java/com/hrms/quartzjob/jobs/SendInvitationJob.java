package com.hrms.quartzjob.jobs;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hrms.quartzjob.config.URLRepository;
import com.hrms.quartzjob.hrmsdb.enums.InvitationSendStatus;
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

@Component
public class SendInvitationJob extends QuartzJobBean {

    String authToken;

    private SurveyMessageRepository surveyMessageRepository;

    private EmailService emailService;

    private SurveyInvitationHistoryRepository invitationHistoryRepository;

    private SurveyParticipantRepository participantRepository;

    private SurveySettingsRepository settingsRepository;

    private SurveyRepository surveyRepository;

    String uiDomain;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("---------------------Invitation EMAIL JOB START----------------------------");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        try {
            surveyRepository = (SurveyRepository) context.getScheduler().getContext().get("survey_repo");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            settingsRepository = (SurveySettingsRepository) context.getScheduler().getContext().get("settings_repo");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            participantRepository = (SurveyParticipantRepository) context.getScheduler().getContext()
                    .get("participant_repo");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            invitationHistoryRepository = (SurveyInvitationHistoryRepository) context.getScheduler().getContext()
                    .get("invitationHistory_repo");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            emailService = (EmailService) context.getScheduler().getContext().get("email_service");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            surveyMessageRepository = (SurveyMessageRepository) context.getScheduler().getContext().get("msg_repo");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            uiDomain = (String) context.getScheduler().getContext().get("ui_domain_key");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            authToken = (String) context.getScheduler().getContext().get("auth_token");
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long number = dataMap.getLong("number");
        Optional<SurveyEntity> existingSurveyEntity = this.surveyRepository.findById(number);
        if (existingSurveyEntity.isPresent()) {
            SurveyEntity surveyEntity = existingSurveyEntity.get();
            List<SurveyParticipantEntity> existingParticipants = participantRepository.findBySurveyId(number);
            Optional<SurveyMessageEntity> existingSurveyMessage = surveyMessageRepository.findBySurveyId(number);
            Optional<SurveySettingsEntity> existingSurveySettings = settingsRepository.findBySurveyId(number);
            SurveySettingsEntity surveySettingsEntity = existingSurveySettings.get();
            if (existingSurveySettings.isPresent()) {
                for (SurveyParticipantEntity user : existingParticipants) {
                    if (existingSurveyMessage.isPresent()) {
                        SurveyMessageEntity msgEntity = existingSurveyMessage.get();
                        SurveyMessageEntity surveyMessageEntity = new SurveyMessageEntity();
                        surveyMessageEntity.setSurveyId(surveyEntity.getId());
                        surveyMessageEntity.setInvitationEmailSubject(msgEntity.getInvitationEmailSubject());
                        surveyMessageEntity.setInvitationEmail(msgEntity.getInvitationEmail());
                        surveyMessageEntity.setInvitationEmail(
                                surveyMessageEntity.getInvitationEmail().replace("{{name}}", user.getName()));
                        surveyMessageEntity
                                .setInvitationEmail(surveyMessageEntity.getInvitationEmail().replace("{{URL}}",
                                        uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey() + "/"
                                                + user.getUrlKey()));
                        surveyMessageEntity.setInvitationEmail(surveyMessageEntity.getInvitationEmail().replace(
                                "{{start_Date}}",
                                surveySettingsEntity.getStartDate() + " At: " + surveySettingsEntity.getStartTime()));
                        surveyMessageEntity.setInvitationEmail(surveyMessageEntity.getInvitationEmail().replace(
                                "{{end_Date}}",
                                surveySettingsEntity.getEndDate() + " At: " + surveySettingsEntity.getEndTime()));
                        String qrCodeUrl = uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey() + "/"
                                + user.getUrlKey();
                        String qrCode = generateQRCode(qrCodeUrl);
                        surveyMessageEntity.setInvitationEmail(
                                surveyMessageEntity.getInvitationEmail().replace("{{QR_Code}}", qrCode));
                        try {
                            saveHistoryAndSendInvitationEmail(user, surveyMessageEntity, surveyEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                       
                    }
                }
            }
        }
        System.out.println("---------------------Invitation EMAIL JOB END----------------------------");
    }

    public String generateQRCode(String url) {
        ByteArrayOutputStream out = QRCode.from(url).to(ImageType.PNG).stream();
        byte[] qrCodeBytes = out.toByteArray();
        String base64QRCode = Base64.getEncoder().encodeToString(qrCodeBytes);
        return "data:image/png;base64, " + base64QRCode;
    }

    private InvitationSendStatus saveHistoryAndSendInvitationEmail(SurveyParticipantEntity user,
            SurveyMessageEntity template, SurveyEntity surveyEntity) {

        SurveyInvitationHistoryEntity surveyHistory = new SurveyInvitationHistoryEntity();
        surveyHistory.setSubject(template.getInvitationEmailSubject());
        surveyHistory.setBody(template.getInvitationEmail());
        surveyHistory.setStatus(InvitationSendStatus.PENDING);
        surveyHistory.setFailReason("");
        surveyHistory.setEmailTo(user.getEmail());
        surveyHistory.setSurveyId(template.getSurveyId());
        List<SurveyInvitationHistoryEntity> alreadySentMail = this.invitationHistoryRepository
                .findBySurveyId(template.getSurveyId());

        boolean isUserEmailSent = alreadySentMail.stream()
                .anyMatch(history -> history.getEmailTo().equals(user.getEmail()));
        if (!isUserEmailSent) {
            invitationHistoryRepository.save(surveyHistory);
            sendWhatsAppNotification(user, surveyEntity);
            surveyHistory = emailService.sendEmail(surveyHistory);
            invitationHistoryRepository.save(surveyHistory);
            user.setEmailInvitationSent(true);
            participantRepository.save(user);
        }
        return surveyHistory.getStatus();
    }

    private void sendWhatsAppNotification(SurveyParticipantEntity user, SurveyEntity surveyEntity) {
        RestApi restApi = new RestApi();
        String baseUrl = URLRepository.whatsAppUrl;

        JsonObject parameter = new JsonObject();
        parameter.addProperty("type", "text");
        parameter.addProperty("text",
                uiDomain + "/app/company-app/survey/" + surveyEntity.getUrlKey() + "/" + user.getUrlKey());

        JsonArray components = new JsonArray();
        JsonObject bodyComponent = new JsonObject();
        bodyComponent.addProperty("type", "body");
        JsonArray parametersArray = new JsonArray();
        parametersArray.add(parameter);
        bodyComponent.add("parameters", parametersArray);
        components.add(bodyComponent);

        JsonObject languageObject = new JsonObject();
        languageObject.addProperty("code", "en_GB");

        JsonObject templateObject = new JsonObject();
        templateObject.addProperty("name", "survey_invitation");
        templateObject.add("language", languageObject);
        templateObject.add("components", components);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messaging_product", "whatsapp");
        jsonObject.addProperty("recipient_type", "individual");
        jsonObject.addProperty("to", user.getMobile());
        jsonObject.addProperty("type", "template");
        jsonObject.add("template", templateObject);
        
            restApi.whatsAppSendNotification(baseUrl, jsonObject, HttpMethod.POST, authToken);
    }

}
