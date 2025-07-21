package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.job.TaskConfigJob;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Quartz调度器服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
public class QuartzSchedulerService {

  private final Scheduler scheduler;

  public QuartzSchedulerService(
      @Qualifier("emailJobSchedulerFactory") SchedulerFactoryBean schedulerFactoryBean)
      throws SchedulerException {
    this.scheduler = schedulerFactoryBean.getScheduler();
  }

  private static final String JOB_GROUP = "task-config-group";
  private static final String TRIGGER_GROUP = "task-config-trigger-group";

  /**
   * 添加定时任务
   *
   * @param taskConfig 任务配置
   */
  public void addScheduledTask(TaskConfig taskConfig) {
    if (taskConfig == null || !StringUtils.hasText(taskConfig.getCron())) {
      log.warn("任务配置为空或cron表达式为空，跳过添加定时任务");
      return;
    }

    try {
      String jobName = getJobName(taskConfig.getId());
      String triggerName = getTriggerName(taskConfig.getId());

      // 创建JobDetail
      JobDetail jobDetail =
          JobBuilder.newJob(TaskConfigJob.class)
              .withIdentity(jobName, JOB_GROUP)
              .withDescription("任务配置定时任务: " + taskConfig.getTaskName())
              .usingJobData("taskConfigId", taskConfig.getId())
              .storeDurably(true)
              .build();

      // 创建Trigger
      CronTrigger trigger =
          TriggerBuilder.newTrigger()
              .withIdentity(triggerName, TRIGGER_GROUP)
              .withDescription("任务配置触发器: " + taskConfig.getTaskName())
              .withSchedule(CronScheduleBuilder.cronSchedule(taskConfig.getCron()))
              .forJob(jobDetail)
              .build();

      // 调度任务
      scheduler.scheduleJob(jobDetail, trigger);

      log.info("添加定时任务成功，任务名称: {}, cron: {}", taskConfig.getTaskName(), taskConfig.getCron());

    } catch (Exception e) {
      log.error("添加定时任务失败，任务名称: {}, 错误信息: {}", taskConfig.getTaskName(), e.getMessage(), e);
      throw new RuntimeException("添加定时任务失败", e);
    }
  }

  /**
   * 删除定时任务
   *
   * @param taskConfigId 任务配置ID
   */
  public void removeScheduledTask(Long taskConfigId) {
    if (taskConfigId == null) {
      log.warn("任务配置ID为空，跳过删除定时任务");
      return;
    }

    try {
      String jobName = getJobName(taskConfigId);
      String triggerName = getTriggerName(taskConfigId);

      JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);
      TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP);

      // 暂停触发器
      scheduler.pauseTrigger(triggerKey);

      // 删除触发器
      scheduler.unscheduleJob(triggerKey);

      // 删除任务
      scheduler.deleteJob(jobKey);

      log.info("删除定时任务成功，任务配置ID: {}", taskConfigId);

    } catch (Exception e) {
      log.error("删除定时任务失败，任务配置ID: {}, 错误信息: {}", taskConfigId, e.getMessage(), e);
      throw new RuntimeException("删除定时任务失败", e);
    }
  }

  /**
   * 更新定时任务
   *
   * @param taskConfig 任务配置
   */
  public void updateScheduledTask(TaskConfig taskConfig) {
    if (taskConfig == null || taskConfig.getId() == null) {
      log.warn("任务配置为空或ID为空，跳过更新定时任务");
      return;
    }

    // 先删除旧的定时任务
    removeScheduledTask(taskConfig.getId());

    // 如果有cron表达式，则添加新的定时任务
    if (StringUtils.hasText(taskConfig.getCron())) {
      addScheduledTask(taskConfig);
    }
  }

  /**
   * 暂停定时任务
   *
   * @param taskConfigId 任务配置ID
   */
  public void pauseScheduledTask(Long taskConfigId) {
    if (taskConfigId == null) {
      log.warn("任务配置ID为空，跳过暂停定时任务");
      return;
    }

    try {
      String triggerName = getTriggerName(taskConfigId);
      TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP);

      scheduler.pauseTrigger(triggerKey);

      log.info("暂停定时任务成功，任务配置ID: {}", taskConfigId);

    } catch (Exception e) {
      log.error("暂停定时任务失败，任务配置ID: {}, 错误信息: {}", taskConfigId, e.getMessage(), e);
      throw new RuntimeException("暂停定时任务失败", e);
    }
  }

  /**
   * 恢复定时任务
   *
   * @param taskConfigId 任务配置ID
   */
  public void resumeScheduledTask(Long taskConfigId) {
    if (taskConfigId == null) {
      log.warn("任务配置ID为空，跳过恢复定时任务");
      return;
    }

    try {
      String triggerName = getTriggerName(taskConfigId);
      TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP);

      scheduler.resumeTrigger(triggerKey);

      log.info("恢复定时任务成功，任务配置ID: {}", taskConfigId);

    } catch (Exception e) {
      log.error("恢复定时任务失败，任务配置ID: {}, 错误信息: {}", taskConfigId, e.getMessage(), e);
      throw new RuntimeException("恢复定时任务失败", e);
    }
  }

  /**
   * 初始化所有定时任务
   *
   * @param taskConfigs 任务配置列表
   */
  public void initializeScheduledTasks(List<TaskConfig> taskConfigs) {
    if (taskConfigs == null || taskConfigs.isEmpty()) {
      log.info("没有需要初始化的定时任务");
      return;
    }

    log.info("开始初始化定时任务，任务数量: {}", taskConfigs.size());

    int successCount = 0;
    int failCount = 0;

    for (TaskConfig taskConfig : taskConfigs) {
      try {
        if (StringUtils.hasText(taskConfig.getCron()) && taskConfig.getIsActive()) {
          addScheduledTask(taskConfig);
          successCount++;
        }
      } catch (Exception e) {
        log.error("初始化定时任务失败，任务名称: {}, 错误信息: {}", taskConfig.getTaskName(), e.getMessage(), e);
        failCount++;
      }
    }

    log.info("定时任务初始化完成，成功: {}, 失败: {}", successCount, failCount);
  }

  /**
   * 检查定时任务是否存在
   *
   * @param taskConfigId 任务配置ID
   * @return 是否存在
   */
  public boolean isScheduledTaskExists(Long taskConfigId) {
    if (taskConfigId == null) {
      return false;
    }

    try {
      String jobName = getJobName(taskConfigId);
      JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);

      return scheduler.checkExists(jobKey);

    } catch (Exception e) {
      log.error("检查定时任务是否存在失败，任务配置ID: {}, 错误信息: {}", taskConfigId, e.getMessage(), e);
      return false;
    }
  }

  /**
   * 获取Job名称
   *
   * @param taskConfigId 任务配置ID
   * @return Job名称
   */
  private String getJobName(Long taskConfigId) {
    return "task-config-job-" + taskConfigId;
  }

  /**
   * 获取Trigger名称
   *
   * @param taskConfigId 任务配置ID
   * @return Trigger名称
   */
  private String getTriggerName(Long taskConfigId) {
    return "task-config-trigger-" + taskConfigId;
  }
}
