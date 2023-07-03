package com.hrms.quartzjob.hrmsdb.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hrms.quartzjob.hrmsdb.models.SmtpEntity;



@Repository
public interface SmtpRepository extends CrudRepository<SmtpEntity, Long> { 

    @Query("SELECT s FROM SmtpEntity s")
    public SmtpEntity findDefault();
}
