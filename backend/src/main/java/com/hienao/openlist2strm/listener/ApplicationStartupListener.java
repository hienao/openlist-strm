package com.hienao.openlist2strm.listener;

import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.job.LogCleanupJob;
import com.hienao.openlist2strm.service.LogConfigService;
import com.hienao.openlist2strm.service.QuartzSchedulerService;
import com.hienao.openlist2strm.service.TaskConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器 在应用启动完成后，自动加载所有定时任务到Quartz调度器
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

  private final TaskConfigService taskConfigService;
  private final QuartzSchedulerService quartzSchedulerService;
  private final LogConfigService logConfigService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    log.info("应用启动完成，开始初始化系统配置...");

    try {
      // 1. 应用日志级别配置
      logConfigService.applyLogLevelConfig();

      // 2. 注册日志清理定时任务
      registerLogCleanupTask();

      // 3. 查询所有有定时任务表达式的任务配置
      List<TaskConfig> scheduledConfigs = taskConfigService.getScheduledConfigs();

      if (scheduledConfigs.isEmpty()) {
        log.info("没有找到需要调度的任务配置");
      } else {
        log.info("找到 {} 个需要调度的任务配置", scheduledConfigs.size());
        // 初始化所有定时任务
        quartzSchedulerService.initializeScheduledTasks(scheduledConfigs);
      }

      log.info("系统初始化完成");

    } catch (Exception e) {
      log.error("系统初始化失败: {}", e.getMessage(), e);
    }
  }

  /** 注册日志清理定时任务 */
  private void registerLogCleanupTask() {
    try {
      // 获取Scheduler实例
      Scheduler scheduler = quartzSchedulerService.getScheduler();

      // 每天凌晨1:30执行日志清理任务
      String cronExpression = "0 30 1 * * ?";
      String jobName = "LogCleanupJob";
      String jobGroup = "SYSTEM";

      // 创建JobDetail
      JobDetail jobDetail =
          JobBuilder.newJob(LogCleanupJob.class)
              .withIdentity(jobName, jobGroup)
              .withDescription("日志清理定时任务")
              .storeDurably(true)
              .build();

      // 创建CronTrigger
      CronTrigger trigger =
          TriggerBuilder.newTrigger()
              .withIdentity(jobName + "Trigger", jobGroup)
              .withDescription("日志清理触发器")
              .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
              .forJob(jobDetail)
              .build();

      // 调度任务
      scheduler.scheduleJob(jobDetail, trigger);

      log.info("日志清理定时任务注册成功，执行时间: 每天凌晨1:30");

    } catch (Exception e) {
      log.error("注册日志清理定时任务失败: {}", e.getMessage(), e);
    }
  }
}
