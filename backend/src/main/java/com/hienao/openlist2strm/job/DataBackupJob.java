package com.hienao.openlist2strm.job;

import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class DataBackupJob implements Job {

  @Override
  public void execute(JobExecutionContext context) {
    String userId = context.getJobDetail().getJobDataMap().getString("userId");
    log.info(
        MessageFormat.format(
            "Job execute: JobName {0} Param {1} Thread: {2}",
            getClass(), userId, Thread.currentThread().getName()));
  }
}
