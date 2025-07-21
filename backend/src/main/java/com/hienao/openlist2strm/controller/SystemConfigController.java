package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.dto.ApiResponse;
import com.hienao.openlist2strm.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "系统配置的读取和保存接口")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取系统配置", description = "获取当前系统配置信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfig() {
        try {
            Map<String, Object> config = systemConfigService.getSystemConfig();
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取系统配置失败: " + e.getMessage()));
        }
    }

    /**
     * 保存系统配置
     */
    @PostMapping("/config")
    @Operation(summary = "保存系统配置", description = "保存系统配置信息")
    public ResponseEntity<ApiResponse<String>> saveSystemConfig(
            @RequestBody Map<String, Object> config) {
        try {
            // 验证媒体文件后缀配置
            if (config.containsKey("mediaExtensions")) {
                Object mediaExtensions = config.get("mediaExtensions");
                if (!(mediaExtensions instanceof List)) {
                    return ResponseEntity.ok(ApiResponse.error("mediaExtensions必须是数组类型"));
                }
                
                @SuppressWarnings("unchecked")
                List<String> extensions = (List<String>) mediaExtensions;
                
                // 验证后缀格式
                for (String ext : extensions) {
                    if (!ext.startsWith(".")) {
                        return ResponseEntity.ok(ApiResponse.error("文件后缀必须以.开头: " + ext));
                    }
                }
            }
            
            systemConfigService.saveSystemConfig(config);
            return ResponseEntity.ok(ApiResponse.success("配置保存成功"));
        } catch (Exception e) {
            log.error("保存系统配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("保存系统配置失败: " + e.getMessage()));
        }
    }
}