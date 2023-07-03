package com.hrms.quartzjob.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
public class Startup {

    @Autowired
    SchedulerFactoryBean schedulerFactoryBean;

    @PostConstruct
    void init() {
        System.out.println("----------------STARTTING JOB------------------");
        try {
            if (schedulerFactoryBean != null && !schedulerFactoryBean.isRunning()) {
                schedulerFactoryBean.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    void distory() {
        try {
            System.out.println("-----------------STOPPING JOB---------------");
            if (schedulerFactoryBean != null && schedulerFactoryBean.isRunning()) {
                schedulerFactoryBean.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
