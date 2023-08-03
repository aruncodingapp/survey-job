package com.hrms.quartzjob.hrmsdb.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppBodyResponseDto {
    
     @JsonProperty("messaging_product")
    private String messagingProduct;
    private List<ContactResponseDto> contacts;
    private List<MessageResponseDto> messages;
}