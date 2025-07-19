package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.entity.TaskConfig;
import com.hienao.openlist2strm.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

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
     * TODO: 根据实际业务需求实现具体的任务执行逻辑
     *
     * @param taskConfig 任务配置
     * @param isIncrement 是否增量执行
     */
    private void executeTaskLogic(TaskConfig taskConfig, boolean isIncrement) {
        log.info("执行任务逻辑 - 任务名称: {}, 路径: {}, 增量模式: {}, STRM路径: {}", 
                taskConfig.getTaskName(), taskConfig.getPath(), isIncrement, taskConfig.getStrmPath());
        
        try {
            // 模拟任务执行时间
            Thread.sleep(1000);
            
            // TODO: 在这里实现具体的任务逻辑
            // 1. 根据taskConfig.getPath()扫描文件
            // 2. 根据isIncrement决定是全量还是增量处理
            // 3. 根据taskConfig.getNeedScrap()决定是否需要刮削
            // 4. 根据taskConfig.getRenameRegex()进行文件重命名
            // 5. 生成STRM文件到taskConfig.getStrmPath()
            
            log.info("任务逻辑执行完成 - 任务名称: {}", taskConfig.getTaskName());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("任务执行被中断", e);
        } catch (Exception e) {
            throw new BusinessException("任务逻辑执行失败: " + e.getMessage(), e);
        }
    }
}