package com.hrms.quartzjob.hrmsdb.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "survey")
public class SurveyEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(columnDefinition = "NVARCHAR(250)", nullable = false)
    private String name;
    @Column(columnDefinition = "boolean default false") 
    private Boolean isPublished;
    @Column(columnDefinition = "boolean default false") 
    private Boolean isSuspended;
    private String urlKey;
    @Column(columnDefinition = "boolean default false") 
    private Boolean isRandomized;
}
