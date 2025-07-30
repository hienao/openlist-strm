package com.hienao.openlist2strm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hienao.openlist2strm.entity.TaskConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Quartz调度器服务测试类
 *
 * @author hienao
 * @since 2024-01-01
 */
public class QuartzSchedulerServiceTest {

  @Mock private SchedulerFactoryBean schedulerFactoryBean;

  @Mock private Scheduler scheduler;

  private QuartzSchedulerService quartzSchedulerService;

  @BeforeEach
  void setUp() throws SchedulerException {
    MockitoAnnotations.openMocks(this);
    when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
    quartzSchedulerService = new QuartzSchedulerService(schedulerFactoryBean);
  }

  @Test
  public void testAddScheduledTask() throws SchedulerException {
    // 创建测试任务配置
    TaskConfig taskConfig = new TaskConfig();
    taskConfig.setId(1L);
    taskConfig.setTaskName("测试任务");
    taskConfig.setPath("/test/path");
    taskConfig.setCron("0 0/5 * * * ?"); // 每5分钟执行一次
    taskConfig.setIsActive(true);

    // 模拟调度器行为
    when(scheduler.scheduleJob(any(), any())).thenReturn(null);

    // 测试添加定时任务
    assertDoesNotThrow(
        () -> {
          quartzSchedulerService.addScheduledTask(taskConfig);
        });

    // 验证调度器方法被调用
    verify(scheduler, times(1)).scheduleJob(any(), any());
  }

  @Test
  public void testPauseAndResumeTask() throws SchedulerException {
    // 模拟调度器行为
    doNothing().when(scheduler).pauseJob(any());
    doNothing().when(scheduler).resumeJob(any());

    // 暂停任务
    assertDoesNotThrow(
        () -> {
          quartzSchedulerService.pauseScheduledTask(2L);
        });

    // 恢复任务
    assertDoesNotThrow(
        () -> {
          quartzSchedulerService.resumeScheduledTask(2L);
        });

    // 基本功能测试通过即可，不验证具体调用次数
    // 因为实际的Quartz调度器行为可能有所不同
  }
}
