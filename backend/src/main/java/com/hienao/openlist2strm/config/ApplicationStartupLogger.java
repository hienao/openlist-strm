package com.hienao.openlist2strm.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 应用启动日志输出 在应用完全启动后输出关键信息，帮助诊断问题 */
@Component
@Slf4j
public class ApplicationStartupLogger {

  @Value("${logging.file.path:./logs}")
  private String logPath;

  private final Environment environment;

  public ApplicationStartupLogger(Environment environment) {
    this.environment = environment;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("=".repeat(60));
    log.info("🚀 应用启动完成！");
    log.info("=".repeat(60));

    // 输出环境信息
    String[] activeProfiles = environment.getActiveProfiles();
    log.info(
        "📋 活动配置文件: {}", activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default");
    log.info("🌐 服务端口: {}", environment.getProperty("server.port", "8080"));
    log.info("📁 工作目录: {}", System.getProperty("user.dir"));
    log.info("☕ Java版本: {}", System.getProperty("java.version"));
    log.info("🖥️  操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));

    // 输出日志配置信息
    log.info("📝 日志配置:");
    log.info("   配置路径: {}", logPath);

    Path logDir = Paths.get(logPath);
    log.info("   绝对路径: {}", logDir.toAbsolutePath());
    log.info("   目录存在: {}", Files.exists(logDir));

    if (Files.exists(logDir)) {
      try {
        log.info("   目录权限: 可读={}, 可写={}", Files.isReadable(logDir), Files.isWritable(logDir));

        // 检查日志文件
        Path backendLog = logDir.resolve("backend.log");
        Path errorLog = logDir.resolve("error.log");

        log.info(
            "   backend.log: 存在={}, 大小={}字节",
            Files.exists(backendLog),
            Files.exists(backendLog) ? Files.size(backendLog) : 0);

        log.info(
            "   error.log: 存在={}, 大小={}字节",
            Files.exists(errorLog),
            Files.exists(errorLog) ? Files.size(errorLog) : 0);

      } catch (Exception e) {
        log.warn("检查日志文件时出错: {}", e.getMessage());
      }
    }

    // 输出数据库配置信息
    String dbUrl = environment.getProperty("spring.datasource.url");
    if (dbUrl != null) {
      log.info("🗄️  数据库: {}", dbUrl);
    }

    // 输出CORS配置
    String allowedOrigins = environment.getProperty("cors.allowedOrigins");
    if (allowedOrigins != null) {
      log.info("🌍 CORS允许源: {}", allowedOrigins);
    }

    // 输出访问地址
    String port = environment.getProperty("server.port", "8080");
    log.info("🔗 访问地址:");
    log.info("   本地: http://localhost:{}", port);
    log.info("   API文档: http://localhost:{}/swagger-ui.html", port);

    // 输出环境变量（仅关键的）
    log.info("🔧 关键环境变量:");
    logEnvVar("LOG_PATH");
    logEnvVar("SPRING_PROFILES_ACTIVE");
    logEnvVar("DATABASE_PATH");

    log.info("=".repeat(60));
    log.info("✅ 应用已就绪，可以开始处理请求");
    log.info("=".repeat(60));
  }

  private void logEnvVar(String name) {
    String value = System.getenv(name);
    if (value != null) {
      log.info("   {}: {}", name, value);
    } else {
      log.info("   {}: (未设置)", name);
    }
  }
}
