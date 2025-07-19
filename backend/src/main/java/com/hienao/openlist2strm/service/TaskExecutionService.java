package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.entity.OpenlistConfig;
import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 任务执行服务类
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionService {

    private final TaskConfigService taskConfigService;
    private final OpenlistConfigService openlistConfigService;
    private final OpenlistApiService openlistApiService;
    private final StrmFileService strmFileService;
    private final Executor taskSubmitExecutor;

    /**
     * 提交任务到线程池执行
     *
     * @param taskId 任务ID
     * @param isIncrement 是否增量执行（可选参数）
     */
    public void submitTask(Long taskId, Boolean isIncrement) {
        log.info("提交任务到线程池 - 任务ID: {}, 增量模式: {}", taskId, isIncrement);
        executeTask(taskId, isIncrement);
    }

    /**
     * 异步执行任务
     *
     * @param taskId 任务ID
     * @param isIncrement 是否增量执行（可选参数）
     * @return CompletableFuture<Void>
     */
    @Async("taskSubmitExecutor")
    public CompletableFuture<Void> executeTask(Long taskId, Boolean isIncrement) {
        try {
            log.info("开始执行任务 - 任务ID: {}, 增量模式: {}, 线程: {}", 
                    taskId, isIncrement, Thread.currentThread().getName());
            
            // 获取任务配置
            TaskConfig taskConfig = taskConfigService.getById(taskId);
            if (taskConfig == null) {
                throw new BusinessException("任务配置不存在，ID: " + taskId);
            }
            
            // 检查任务是否启用
            if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
                throw new BusinessException("任务已禁用，无法执行，ID: " + taskId);
            }
            
            // 确定是否使用增量模式
            boolean useIncrement;
            if (isIncrement != null) {
                // 如果传了参数，以传参为主
                useIncrement = isIncrement;
                log.info("使用传入的增量参数: {}", isIncrement);
            } else {
                // 如果没传参数，以任务配置为主
                useIncrement = Boolean.TRUE.equals(taskConfig.getIsIncrement());
                log.info("使用任务配置的增量参数: {}", useIncrement);
            }
            
            // 更新任务开始执行时间
            taskConfigService.updateLastExecTime(taskId, LocalDateTime.now());
            
            // 执行具体的任务逻辑
            executeTaskLogic(taskConfig, useIncrement);
            
            log.info("任务执行完成 - 任务ID: {}, 任务名称: {}, 增量模式: {}", 
                    taskId, taskConfig.getTaskName(), useIncrement);
            
        } catch (Exception e) {
            log.error("任务执行失败 - 任务ID: {}, 错误信息: {}", taskId, e.getMessage(), e);
            throw new BusinessException("任务执行失败: " + e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 执行具体的任务逻辑
     * 1. 根据任务配置获取OpenList配置
     * 2. 通过OpenList API递归获取所有文件
     * 3. 对视频文件生成STRM文件
     * 4. 保持目录结构一致
     *
     * @param taskConfig 任务配置
     * @param isIncrement 是否增量执行
     */
    private void executeTaskLogic(TaskConfig taskConfig, boolean isIncrement) {
        log.info("开始执行任务逻辑: {}, 增量模式: {}", taskConfig.getTaskName(), isIncrement);
        
        try {
            // 1. 获取OpenList配置
            OpenlistConfig openlistConfig = getOpenlistConfig(taskConfig);
            
            // 2. 递归获取任务目录下的所有文件
            List<OpenlistApiService.OpenlistFile> allFiles = openlistApiService.getAllFilesRecursively(
                openlistConfig, taskConfig.getPath());
            
            log.info("获取到 {} 个文件/目录", allFiles.size());
            
            // 3. 过滤并处理视频文件
            int processedCount = 0;
            for (OpenlistApiService.OpenlistFile file : allFiles) {
                if ("file".equals(file.getType()) && strmFileService.isVideoFile(file.getName())) {
                    try {
                        // 计算相对路径
                        String relativePath = strmFileService.calculateRelativePath(
                            taskConfig.getPath(), file.getPath());
                        
                        // 生成STRM文件
                        strmFileService.generateStrmFile(
                            taskConfig.getStrmPath(),
                            relativePath,
                            file.getName(),
                            file.getUrl(),
                            taskConfig.getRenameRegex()
                        );
                        
                        processedCount++;
                        
                    } catch (Exception e) {
                        log.error("处理文件失败: {}, 错误: {}", file.getName(), e.getMessage(), e);
                        // 继续处理其他文件，不中断整个任务
                    }
                }
            }
            
            log.info("任务执行完成: {}, 处理了 {} 个视频文件", taskConfig.getTaskName(), processedCount);
            
        } catch (Exception e) {
            log.error("任务执行失败: {}, 错误: {}", taskConfig.getTaskName(), e.getMessage(), e);
            throw new BusinessException("任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取OpenList配置
     *
     * @param taskConfig 任务配置
     * @return OpenList配置
     */
    private OpenlistConfig getOpenlistConfig(TaskConfig taskConfig) {
        if (taskConfig.getOpenlistConfigId() == null) {
            throw new BusinessException("任务配置中未指定OpenList配置ID");
        }
        
        OpenlistConfig openlistConfig = openlistConfigService.getById(taskConfig.getOpenlistConfigId());
        if (openlistConfig == null) {
            throw new BusinessException("OpenList配置不存在，ID: " + taskConfig.getOpenlistConfigId());
        }
        
        if (!Boolean.TRUE.equals(openlistConfig.getIsActive())) {
            throw new BusinessException("OpenList配置已禁用，ID: " + taskConfig.getOpenlistConfigId());
        }
        
        return openlistConfig;
    }
}