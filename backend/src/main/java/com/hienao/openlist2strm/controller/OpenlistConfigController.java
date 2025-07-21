package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.dto.ApiResponse;
import com.hienao.openlist2strm.dto.openlist.OpenlistConfigDto;
import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.service.OpenlistConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * openlist配置管理控制器
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/openlist-config")
@RequiredArgsConstructor
@Tag(name = "Openlist配置管理", description = "openlist配置的增删改查接口")
public class OpenlistConfigController {

  private final OpenlistConfigService openlistConfigService;

  /** 查询所有配置 */
  @GetMapping
  @Operation(summary = "查询所有配置", description = "获取所有openlist配置列表")
  public ResponseEntity<ApiResponse<List<OpenlistConfigDto>>> getAllConfigs() {
    List<OpenlistConfig> configs = openlistConfigService.getAllConfigs();
    List<OpenlistConfigDto> configDtos =
        configs.stream().map(this::convertToDto).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(configDtos));
  }

  /** 查询启用的配置 */
  @GetMapping("/active")
  @Operation(summary = "查询启用的配置", description = "获取所有启用状态的openlist配置")
  public ResponseEntity<ApiResponse<List<OpenlistConfigDto>>> getActiveConfigs() {
    List<OpenlistConfig> configs = openlistConfigService.getActiveConfigs();
    List<OpenlistConfigDto> configDtos =
        configs.stream().map(this::convertToDto).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(configDtos));
  }

  /** 根据ID查询配置 */
  @GetMapping("/{id}")
  @Operation(summary = "根据ID查询配置", description = "根据配置ID获取openlist配置详情")
  public ResponseEntity<ApiResponse<OpenlistConfigDto>> getConfigById(
      @Parameter(description = "配置ID", required = true) @PathVariable Long id) {
    OpenlistConfig config = openlistConfigService.getById(id);
    if (config == null) {
      return ResponseEntity.ok(ApiResponse.error(404, "配置不存在"));
    }
    return ResponseEntity.ok(ApiResponse.success(convertToDto(config)));
  }

  /** 根据用户名查询配置 */
  @GetMapping("/username/{username}")
  @Operation(summary = "根据用户名查询配置", description = "根据用户名获取openlist配置")
  public ResponseEntity<ApiResponse<OpenlistConfigDto>> getConfigByUsername(
      @Parameter(description = "用户名", required = true) @PathVariable String username) {
    OpenlistConfig config = openlistConfigService.getByUsername(username);
    if (config == null) {
      return ResponseEntity.ok(ApiResponse.error(404, "配置不存在"));
    }
    return ResponseEntity.ok(ApiResponse.success(convertToDto(config)));
  }

  /** 创建配置 */
  @PostMapping
  @Operation(summary = "创建配置", description = "创建新的openlist配置")
  public ResponseEntity<ApiResponse<OpenlistConfigDto>> createConfig(
      @Parameter(description = "配置信息", required = true) @Valid @RequestBody
          OpenlistConfigDto configDto) {
    OpenlistConfig config = convertToEntity(configDto);
    OpenlistConfig createdConfig = openlistConfigService.createConfig(config);
    return ResponseEntity.ok(ApiResponse.success(convertToDto(createdConfig)));
  }

  /** 更新配置 */
  @PutMapping("/{id}")
  @Operation(summary = "更新配置", description = "更新指定ID的openlist配置")
  public ResponseEntity<ApiResponse<OpenlistConfigDto>> updateConfig(
      @Parameter(description = "配置ID", required = true) @PathVariable Long id,
      @Parameter(description = "配置信息", required = true) @Valid @RequestBody
          OpenlistConfigDto configDto) {
    configDto.setId(id);
    OpenlistConfig config = convertToEntity(configDto);
    OpenlistConfig updatedConfig = openlistConfigService.updateConfig(config);
    return ResponseEntity.ok(ApiResponse.success(convertToDto(updatedConfig)));
  }

  /** 删除配置 */
  @DeleteMapping("/{id}")
  @Operation(summary = "删除配置", description = "删除指定ID的openlist配置")
  public ResponseEntity<ApiResponse<Void>> deleteConfig(
      @Parameter(description = "配置ID", required = true) @PathVariable Long id) {
    openlistConfigService.deleteConfig(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /** 启用/禁用配置 */
  @PatchMapping("/{id}/status")
  @Operation(summary = "启用/禁用配置", description = "更新指定配置的启用状态")
  public ResponseEntity<ApiResponse<Void>> updateConfigStatus(
      @Parameter(description = "配置ID", required = true) @PathVariable Long id,
      @Parameter(description = "状态更新请求", required = true) @RequestBody
          UpdateStatusRequest request) {
    openlistConfigService.updateActiveStatus(id, request.getIsActive());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /** 状态更新请求DTO */
  public static class UpdateStatusRequest {
    private Boolean isActive;

    public Boolean getIsActive() {
      return isActive;
    }

    public void setIsActive(Boolean isActive) {
      this.isActive = isActive;
    }
  }

  /** 实体转DTO */
  private OpenlistConfigDto convertToDto(OpenlistConfig config) {
    OpenlistConfigDto dto = new OpenlistConfigDto();
    BeanUtils.copyProperties(config, dto);
    return dto;
  }

  /** DTO转实体 */
  private OpenlistConfig convertToEntity(OpenlistConfigDto dto) {
    OpenlistConfig config = new OpenlistConfig();
    BeanUtils.copyProperties(dto, config);
    return config;
  }
}
