package com.hrms.quartzjob.hrmsdb.models.service;

import java.util.Base64;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrms.quartzjob.hrmsdb.enums.InvitationSendStatus;
import com.hrms.quartzjob.hrmsdb.models.SmtpEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyInvitationHistoryEntity;
import com.hrms.quartzjob.hrmsdb.repository.SmtpRepository;

@Service
public class EmailService {

    @Autowired
    private SmtpRepository smtpRepository;

    public SurveyInvitationHistoryEntity sendEmailWithAttachment(SurveyInvitationHistoryEntity surveyHistory, String base64Image) {
        if (surveyHistory.getStatus() == InvitationSendStatus.SENT) {
            return surveyHistory;
        }
        try {
            SmtpEntity smtpEntity = smtpRepository.findDefault();
            if (smtpEntity == null) {
                System.out.println("smtpEntity is null");
                surveyHistory.setFailReason("SMTP not configured");
                surveyHistory.setStatus(InvitationSendStatus.FAILED);
                return surveyHistory;
            }
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", (smtpEntity.getPort() == 25? "false":"true "));
            props.put("mail.smtp.starttls.enable", "true ");
            props.put("mail.smtp.host", smtpEntity.getHost());
            props.put("mail.smtp.port", smtpEntity.getPort());

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpEntity.getUserName(), smtpEntity.getPassword());
                }
            });
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(smtpEntity.getFromEmail(), false));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(surveyHistory.getEmailTo()));
            msg.setSubject(surveyHistory.getSubject());

            MimeBodyPart htmlPart = new MimeBodyPart();
            String bodyHtml = surveyHistory.getBody().replace("{{QR_Code}}", "<img src='cid:qrImage' alt='QR Code' />");
            htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");

            byte[] rawImage = Base64.getDecoder().decode(base64Image);
            BodyPart imagePart = new MimeBodyPart();
            ByteArrayDataSource imageDataSource = new ByteArrayDataSource(rawImage, "image/png");

            imagePart.setDataHandler(new DataHandler(imageDataSource));
            imagePart.setHeader("Content-ID", "<qrImage>");
            imagePart.setFileName("QR_Code.png");

            Multipart multiPart = new MimeMultipart("related");
            multiPart.addBodyPart(htmlPart);
            multiPart.addBodyPart(imagePart);

            msg.setContent(multiPart);
            Transport.send(msg);
            surveyHistory.setFailReason("");
            surveyHistory.setStatus(InvitationSendStatus.SENT);
            return surveyHistory;

        } catch (Exception e) {
            e.printStackTrace();
            surveyHistory.setFailReason(e.getMessage());
            surveyHistory.setStatus(InvitationSendStatus.FAILED);
        }
        return surveyHistory;
    }
    
    public SurveyInvitationHistoryEntity sendEmail(SurveyInvitationHistoryEntity surveyHistory) {
        if (surveyHistory.getStatus() == InvitationSendStatus.SENT) {
            return surveyHistory;
        }
        try {
                SmtpEntity smtpEntity = smtpRepository.findDefault();
                if (smtpEntity == null) {
                     System.out.println("smtpEntity is null");
                     surveyHistory.setFailReason("SMTP not configured");
                     surveyHistory.setStatus(InvitationSendStatus.FAILED);
                    return surveyHistory; 
                }
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", (smtpEntity.getPort() == 25? "false":"true "));
                props.put("mail.smtp.starttls.enable", "true ");
                props.put("mail.smtp.host", smtpEntity.getHost());
                props.put("mail.smtp.port", smtpEntity.getPort());

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(smtpEntity.getUserName(), smtpEntity.getPassword());
                    }
                 });
                 Message msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(smtpEntity.getFromEmail(), false));
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(surveyHistory.getEmailTo()));
                    msg.setSubject(surveyHistory.getSubject());
                    msg.setContent(surveyHistory.getBody(), "text/html");
                    msg.setSentDate(new Date());
                    Transport.send(msg);
                    surveyHistory.setFailReason("");
                    surveyHistory.setStatus(InvitationSendStatus.SENT);
                    return surveyHistory;
            
        } catch (Exception e) {
            e.printStackTrace();
            surveyHistory.setFailReason(e.getMessage());
            surveyHistory.setStatus(InvitationSendStatus.FAILED);
        }
        return surveyHistory;
    }
}
