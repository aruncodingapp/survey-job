package com.hrms.quartzjob.hrmsdb.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseDto {
    private String input;
    @JsonProperty("wa_id")
    private String waId;
}