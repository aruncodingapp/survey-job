package com.hrms.quartzjob.config;

import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;


import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.hrms.quartzjob.jobs.ReminderEmailBeforeEndJob;
import com.hrms.quartzjob.jobs.ReminderEmailBeforeStartJob;

@Configuration
public class QuartzConfig {
    private ApplicationContext applicationContext;
    private DataSource dataSource;
    private QuartzProperties quartzProperties;

    //  private static final String EVERY_MINUTE = "59 * * ? * * *";

    private static final String EVERY_DAY_MIDNIGHT = "0 0 0 * * ?";
    private static final String EVERY_MONTH_FIRST_DAY_MIDNIGHT = "0 0 0 1 * ? *";

    public QuartzConfig(ApplicationContext applicationContext, DataSource dataSource,
            QuartzProperties quartzProperties) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.quartzProperties = quartzProperties;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SechedulerJobFactory jobFactory = new SechedulerJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setAutoStartup(false);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setTriggers(reminderBeforeStartTrigger().getObject(), reminderBeforeEndTrigger().getObject());
        try {
            schedulerFactoryBean.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedulerFactoryBean;
    }

    @Bean
    public CronTriggerFactoryBean reminderBeforeStartTrigger() {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(Objects.requireNonNull(reminderBeforeStartJob().getObject()));
        factoryBean.setStartDelay(1000);
        factoryBean.setCronExpression(EVERY_DAY_MIDNIGHT);
        return factoryBean;

    }

    @Bean
    public JobDetailFactoryBean reminderBeforeStartJob() {
        JobDetailFactoryBean detailFactoryBean = new JobDetailFactoryBean();
        detailFactoryBean.setJobClass(ReminderEmailBeforeStartJob.class);
        detailFactoryBean.setDescription("Survey reminder before start job");
        detailFactoryBean.setDurability(true);
        detailFactoryBean.setName("Survey reminder before start job");
        detailFactoryBean.afterPropertiesSet();
        return detailFactoryBean;

    }
    @Bean
    public CronTriggerFactoryBean reminderBeforeEndTrigger() {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(Objects.requireNonNull(reminderBeforeEndJob().getObject()));
        factoryBean.setStartDelay(1000);
        factoryBean.setCronExpression(EVERY_DAY_MIDNIGHT);
        return factoryBean;

    }

    @Bean
    public JobDetailFactoryBean reminderBeforeEndJob() {
        JobDetailFactoryBean detailFactoryBean = new JobDetailFactoryBean();
        detailFactoryBean.setJobClass(ReminderEmailBeforeEndJob.class);
        detailFactoryBean.setDescription("Survey reminder before end job");
        detailFactoryBean.setDurability(true);
        detailFactoryBean.setName("Survey reminder before end job");
        detailFactoryBean.afterPropertiesSet();
        return detailFactoryBean;

    }
}
