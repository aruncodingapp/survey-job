package com.hrms.quartzjob.hrmsdb.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hrms.quartzjob.config.URLRepository;
import com.hrms.quartzjob.hrmsdb.models.SurveyEntity;
import com.hrms.quartzjob.hrmsdb.models.SurveyParticipantEntity;
import com.hrms.quartzjob.hrmsdb.repository.SmtpRepository;
@Service
public class WhatsAppService {
    
        @Autowired SmtpRepository smtpRepository;
        
        @Value("${survey.ui.domain}")
        String uiDomain;

    public void sendWhatsAppNotification(SurveyParticipantEntity user, SurveyEntity surveyEntity) {
        RestApi restApi = new RestApi();
        String baseUrl = URLRepository.whatsAppUrl;
        String authToken = smtpRepository.findWhatsAppKey();

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
