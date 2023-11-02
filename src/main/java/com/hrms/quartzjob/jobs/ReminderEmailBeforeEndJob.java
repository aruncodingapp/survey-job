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
import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantAttendeesEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveySettingsEntity;
import com.hrms.quartzjob.hrmsdb.models.service.EmailService;
import com.hrms.quartzjob.hrmsdb.models.service.RestApi;
import com.hrms.quartzjob.hrmsdb.repository.JobLogRepository;
import com.hrms.quartzjob.hrmsdb.repository.SmtpRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyInvitationHistoryRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyMessageRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyParticipantAttendeesRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyParticipantRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveyRepository;
import com.hrms.quartzjob.hrmsdb.repository.SurveySettingsRepository;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@DisallowConcurrentExecution
public class ReminderEmailBeforeEndJob extends QuartzJobBean {
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

    @Autowired
    private SurveyParticipantAttendeesRepository participantAttendeesRepository;

    @Autowired 
    private SmtpRepository smtpRepository;

    @Value("${survey.ui.domain}")
    String uiDomain;

    @Autowired private JobLogRepository jobLogRepository;
    private final String name = "Reminder Mail Before End Job";

    String qrCodeAttachment;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("---------------------REMINDER EMAIL BEFORE END JOB START----------------------------");
        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END JOB START");
        List<ISurveyDto> existingAllActiveSurvey = surveyRepository.getAllByActive();
        if (!existingAllActiveSurvey.isEmpty()) {
            for (ISurveyDto survey : existingAllActiveSurvey) {
                Optional<SurveyEntity> existingSurvey = surveyRepository.findById(survey.getId());
                if (existingSurvey.isPresent() && existingSurvey.get().getIsPublished()) {
                    jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey is Present & its published");
                    SurveyEntity surveyEntity = existingSurvey.get();
                    Optional<SurveyMessageEntity> surveyMessage = surveyMessageRepository
                            .findBySurveyId(surveyEntity.getId());
                    Optional<SurveySettingsEntity> existingSurveySettings = settingsRepository
                            .findBySurveyId(surveyEntity.getId());
                    if (existingSurveySettings.isPresent()) {
                        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Existing Survey is Present");
                        SurveySettingsEntity surveySettingsEntity = existingSurveySettings.get();
                        List<SurveyParticipantEntity> existingUsers = participantRepository
                                .findBySurveyId(surveyEntity.getId());
                        int surveyEndReminder = surveySettingsEntity.getSurveyEndReminder();
                        LocalDate endDate = surveySettingsEntity.getEndDate();
                        LocalDate currentDate = LocalDate.now();
                        if (currentDate.plusDays(surveyEndReminder).isEqual(endDate)) {
                            jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END:Survey end date less then current Date");
                            for (SurveyParticipantEntity user : existingUsers) {
                                jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey ExistingUser");
                                if (surveyMessage.isPresent()) {
                                    jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Message Present");
                                    SurveyMessageEntity msgEntity = surveyMessage.get();
                                    SurveyMessageEntity surveyMessageEntity = new SurveyMessageEntity();
                                    surveyMessageEntity.setSurveyId(surveyEntity.getId());
                                    surveyMessageEntity
                                            .setReminderEndEmailSubject(msgEntity.getReminderEndEmailSubject());
                                    surveyMessageEntity.setReminderEndEmail(msgEntity.getReminderEndEmail());
                                    surveyMessageEntity.setReminderEndEmail(
                                            surveyMessageEntity.getReminderEndEmail().replace("{{name}}",
                                                    user.getName()));
                                    surveyMessageEntity
                                            .setReminderEndEmail(
                                                    surveyMessageEntity.getReminderEndEmail().replace("{{URL}}",
                                                            uiDomain + "/app/company-app/survey/"
                                                                    + surveyEntity.getUrlKey() + "/"
                                                                    + user.getUrlKey()));
                                    surveyMessageEntity.setReminderEndEmail(
                                            surveyMessageEntity.getReminderEndEmail().replace("{{start_Date}}",
                                                    surveySettingsEntity.getStartDate() + " At: "
                                                            + surveySettingsEntity.getStartTime()));
                                    surveyMessageEntity.setReminderEndEmail(
                                            surveyMessageEntity.getReminderEndEmail().replace("{{end_Date}}",
                                                    surveySettingsEntity.getEndDate() + " At: "
                                                            + surveySettingsEntity.getEndTime()));
                                    String qrCodeUrl = uiDomain + "/app/company-app/survey/"
                                            + surveyEntity.getUrlKey()
                                            + "/"
                                            + user.getUrlKey();
                                     String qrCode = generateQRCode(qrCodeUrl);
                                    surveyMessageEntity.setReminderEndEmail(
                                            surveyMessageEntity.getReminderEndEmail().replace("{{QR_Code}}", qrCode));
                                    try {
                                        saveHistoryAndSendReminderEmail(user, surveyMessageEntity);
                                        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Reminder Send Successfully");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Reminder Failed"+e.getMessage());
                                    }
                                }
                            }

                        }
                    }
                }
            }
            jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END JOB END");
            System.out.println("---------------------REMINDER EMAIL BEFORE END JOB END----------------------------");
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
        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Email Reminder Start");
        SurveyInvitationHistoryEntity surveyHistory = new SurveyInvitationHistoryEntity();
        surveyHistory.setSubject(template.getReminderEndEmailSubject());
        surveyHistory.setBody(template.getReminderEndEmail());
        surveyHistory.setStatus(InvitationSendStatus.PENDING);
        surveyHistory.setFailReason("");
        surveyHistory.setSurveyId(user.getSurveyId());
        surveyHistory.setEmailTo(user.getEmail());
        surveyHistory.setSurveyId(template.getSurveyId());
        SurveyParticipantAttendeesEntity participantAttendee = this.participantAttendeesRepository
                .findByParticipantId(user.getId());
        boolean shouldSendEmail = (participantAttendee == null) || !participantAttendee.getIsSubmitted();

        if (shouldSendEmail) {
            sendWhatsAppNotification(user);
            jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Email Reminder Sending to..."+user.getEmail());
            invitationHistoryRepository.save(surveyHistory);
            if (user.getEmail().endsWith("@gmail.com")) {
                surveyHistory = emailService.sendEmailWithAttachment(surveyHistory, qrCodeAttachment);
            } else {
                surveyHistory = emailService.sendEmail(surveyHistory);
            }
            invitationHistoryRepository.save(surveyHistory);
        }
        System.out.println("...Reminder Email Sent to ..."+user.getEmail()+"successfully");
        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Email Reminder Send to..."+user.getEmail()+" successfully");
        user.setEmailInvitationSent(true);
        participantRepository.save(user);
        return surveyHistory.getStatus();
    }

    private void  sendWhatsAppNotification(SurveyParticipantEntity user) {
        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Whats App Reminder Triggered");
        RestApi restApi = new RestApi();
        String baseUrl = URLRepository.whatsAppUrl;
        String authToken = smtpRepository.findWhatsAppKey();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messaging_product", "whatsapp");
        jsonObject.addProperty("recipient_type", "individual");
        jsonObject.addProperty("to",user.getMobile());
        jsonObject.addProperty("type", "template");

        JsonObject templateObject = new JsonObject();
        templateObject.addProperty("name", "survey_reminder");

        JsonObject languageObject = new JsonObject();
        languageObject.addProperty("code", "en_GB");

        templateObject.add("language", languageObject);
        jsonObject.add("template", templateObject);

        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Whats App Reminder Sending...");
        restApi.whatsAppSendNotification(baseUrl, jsonObject, HttpMethod.POST, authToken);
        jobLogRepository.insertLog(name,"REMINDER EMAIL BEFORE END: Survey Whats App Reminder Successfully to"+user.getMobile());
    }
}
