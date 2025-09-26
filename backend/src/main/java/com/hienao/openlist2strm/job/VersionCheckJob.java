package com.hienao.openlist2strm.job;

import com.hienao.openlist2strm.dto.version.VersionCheckResponse;
import com.hienao.openlist2strm.service.GitHubVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 版本检查定时任务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VersionCheckJob implements Job {

  private final GitHubVersionService gitHubVersionService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      log.info("开始执行版本检查定时任务");

      // 获取当前版本
      String currentVersion = getCurrentVersion();

      // 检查版本更新
      VersionCheckResponse response = gitHubVersionService.checkVersionUpdate(currentVersion);

      if (response.getError() != null) {
        log.warn("版本检查失败: {}", response.getError());
        return;
      }

      if (response.isHasUpdate()) {
        log.info("发现新版本: 当前版本 {}, 最新版本 {}", currentVersion, response.getLatestVersion());

        // 这里可以添加通知逻辑，比如发送邮件、WebSocket推送等
        // notifyNewVersion(response);
      } else {
        log.debug("当前版本已是最新: {}", currentVersion);
      }

      log.info("版本检查定时任务执行完成");
    } catch (Exception e) {
      log.error("版本检查定时任务执行失败", e);
      throw new JobExecutionException("版本检查定时任务执行失败", e);
    }
  }

  /** 获取当前版本 */
  private String getCurrentVersion() {
    // 从环境变量获取版本号（通过GitHub Actions自动设置）
    String version = System.getenv("APP_VERSION");
    if (version == null || version.trim().isEmpty()) {
      version = "dev"; // 默认版本号
    }
    return version;
  }
}
