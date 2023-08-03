package com.hrms.quartzjob.hrmsdb.models.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.hrms.quartzjob.hrmsdb.models.WhatsAppBodyResponseDto;

@Service
public class RestApi {

    private RestTemplate restTemplate;

    WhatsAppBodyResponseDto response = new WhatsAppBodyResponseDto();

    public WhatsAppBodyResponseDto whatsAppSendNotification(String url, Object body, HttpMethod methodType,String authToken) {
        String bodyParams = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                Gson gson = new Gson();
                bodyParams = gson.toJson(body);
            }
            headers.add("Authorization" ,"Bearer "+ authToken);
            headers.add("Accept", "*/*");
            headers.add("user-agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2)Gecko/20100316 Firefox/3.6.2");
            HttpEntity<String> httpEntity = new HttpEntity<String>(bodyParams, headers);

            restTemplate = new RestTemplate();
            if (methodType == HttpMethod.POST) {
            response = restTemplate.exchange(url, methodType, httpEntity,WhatsAppBodyResponseDto.class).getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
