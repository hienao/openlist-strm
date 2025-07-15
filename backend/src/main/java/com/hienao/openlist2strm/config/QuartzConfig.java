package com.hienao.openlist2strm.config;

import com.hienao.openlist2strm.job.DataBackupJob;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

  @Value("${spring.flyway.default-schema}")
  private String defaultSchema;

  @Bean("emailJobSchedulerFactory")
  public SchedulerFactoryBean emailJobSchedulerFactory(DataSource dataSource) {
    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setSchedulerName("email-scheduler");
    Properties props = getCommonProps();
    props.setProperty("org.quartz.threadPool.threadCount", "10");
    schedulerFactory.setDataSource(dataSource);
    schedulerFactory.setQuartzProperties(props);
    return schedulerFactory;
  }

  public Properties getCommonProps() {
    Properties props = new Properties();
    // Use in-memory job store instead of database for SQLite compatibility
    props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
    props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    return props;
  }

  @Bean("dataBackupJobDetail")
  public JobDetailFactoryBean dataBackupJobDetail() {
    JobDetailFactoryBean factory = new JobDetailFactoryBean();
    factory.setJobClass(DataBackupJob.class);
    factory.setJobDataMap(new JobDataMap(Map.of("userId", "Gh2mxa")));
    factory.setName("data-backup-job");
    factory.setGroup("batch-service");
    factory.setDurability(true);
    return factory;
  }

  @Bean("dataBackupSchedulerFactory")
  public SchedulerFactoryBean dataBackupSchedulerFactory(
      Trigger dataBackupTrigger, JobDetail dataBackupJobDetail, DataSource dataSource) {
    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setSchedulerName("data-backup-scheduler");
    Properties props = getCommonProps();
    props.setProperty("org.quartz.threadPool.threadCount", "5");
    schedulerFactory.setQuartzProperties(props);
    schedulerFactory.setJobDetails(dataBackupJobDetail);
    schedulerFactory.setTriggers(dataBackupTrigger);
    schedulerFactory.setDataSource(dataSource);
    return schedulerFactory;
  }

  @Bean("dataBackupTrigger")
  public CronTriggerFactoryBean dataBackupTrigger(JobDetail dataBackupJobDetail) {
    CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
    factory.setJobDetail(dataBackupJobDetail);
    factory.setCronExpression("0 0/5 * * * ?");
    return factory;
  }
}
