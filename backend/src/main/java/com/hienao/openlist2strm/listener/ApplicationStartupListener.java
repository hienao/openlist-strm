package com.hienao.openlist2strm.listener;

import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.service.QuartzSchedulerService;
import com.hienao.openlist2strm.service.TaskConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    log.info("应用启动完成，开始加载定时任务...");

    try {
      // 查询所有有定时任务表达式的任务配置
      List<TaskConfig> scheduledConfigs = taskConfigService.getScheduledConfigs();

      if (scheduledConfigs.isEmpty()) {
        log.info("没有找到需要调度的任务配置");
        return;
      }

      log.info("找到 {} 个需要调度的任务配置", scheduledConfigs.size());

      // 初始化所有定时任务
      quartzSchedulerService.initializeScheduledTasks(scheduledConfigs);

      log.info("定时任务加载完成");

    } catch (Exception e) {
      log.error("加载定时任务失败: {}", e.getMessage(), e);
    }
  }
}
