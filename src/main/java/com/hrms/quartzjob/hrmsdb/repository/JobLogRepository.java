package com.hrms.quartzjob.hrmsdb.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.hrms.quartzjob.hrmsdb.models.JobLogEntity;

public interface JobLogRepository extends JpaRepository<JobLogEntity,Long> {

    @Query(value ="insert into job_logs (created_on,name,info) values (current_timestamp,:name,:info)",nativeQuery=true)
    @Transactional 
    @Modifying
    void insertLog(String name,String info);
    
}
