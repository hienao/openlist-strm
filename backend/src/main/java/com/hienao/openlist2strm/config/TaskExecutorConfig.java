package com.hienao.openlist2strm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 任务执行线程池配置
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableAsync
public class TaskExecutorConfig {

    /**
     * 任务提交线程池
     * 线程数为1，容量为100000
     */
    @Bean("taskSubmitExecutor")
    public Executor taskSubmitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(1);
        // 最大线程数
        executor.setMaxPoolSize(1);
        // 队列容量
        executor.setQueueCapacity(100000);
        // 线程名前缀
        executor.setThreadNamePrefix("task-submit-");
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("任务提交线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}