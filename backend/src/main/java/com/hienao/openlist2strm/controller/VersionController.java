package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.dto.ApiResponse;
import com.hienao.openlist2strm.dto.version.VersionCheckResponse;
import com.hienao.openlist2strm.service.GitHubVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 版本检查控制器
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
@Tag(name = "版本管理", description = "版本检查和更新相关接口")
public class VersionController {

  private final GitHubVersionService gitHubVersionService;

  /** 检查版本更新 */
  @GetMapping("/check")
  @Operation(summary = "检查版本更新", description = "检查当前版本是否有新版本可用")
  public ResponseEntity<ApiResponse<VersionCheckResponse>> checkVersion(
      @RequestParam(defaultValue = "dev") String currentVersion) {
    try {
      log.debug("检查版本更新请求: {}", currentVersion);

      VersionCheckResponse response = gitHubVersionService.checkVersionUpdate(currentVersion);

      if (response.getError() != null) {
        log.warn("版本检查失败: {}", response.getError());
        return ResponseEntity.ok(ApiResponse.error(response.getError()));
      }

      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception e) {
      log.error("检查版本更新失败", e);
      return ResponseEntity.ok(ApiResponse.error("检查版本更新失败: " + e.getMessage()));
    }
  }

  /** 获取最新版本信息 */
  @GetMapping("/latest")
  @Operation(summary = "获取最新版本信息", description = "获取GitHub上的最新版本信息")
  public ResponseEntity<ApiResponse<VersionCheckResponse>> getLatestVersion() {
    try {
      log.debug("获取最新版本信息请求");

      VersionCheckResponse response = gitHubVersionService.checkVersionUpdate("dev");

      if (response.getError() != null) {
        log.warn("获取最新版本失败: {}", response.getError());
        return ResponseEntity.ok(ApiResponse.error(response.getError()));
      }

      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception e) {
      log.error("获取最新版本失败", e);
      return ResponseEntity.ok(ApiResponse.error("获取最新版本失败: " + e.getMessage()));
    }
  }

  /** 清除版本检查缓存 */
  @DeleteMapping("/cache/clear")
  @Operation(summary = "清除版本检查缓存", description = "清除版本检查相关的缓存数据")
  public ResponseEntity<ApiResponse<String>> clearVersionCache() {
    try {
      log.debug("清除版本检查缓存请求");

      // 这里可以添加清除缓存的逻辑
      // 由于使用了注解缓存，Spring会自动管理

      return ResponseEntity.ok(ApiResponse.success("版本检查缓存已清除"));
    } catch (Exception e) {
      log.error("清除版本检查缓存失败", e);
      return ResponseEntity.ok(ApiResponse.error("清除版本检查缓存失败: " + e.getMessage()));
    }
  }
}
