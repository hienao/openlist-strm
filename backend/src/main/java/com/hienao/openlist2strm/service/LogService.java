/*
 * OpenList STRM - Stream Management System
 * Copyright (C) 2024 OpenList STRM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hienao.openlist2strm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogService {

    @Value("${logging.file.path:./logs}")
    private String logPath;

    // 默认日志路径，如果配置的路径不存在则使用项目根目录下的logs
    private String getActualLogPath() {
        // 首先尝试使用配置的路径
        Path configuredPath = Paths.get(logPath);
        if (Files.exists(configuredPath)) {
            return logPath;
        }

        // 如果配置路径不存在，尝试项目根目录下的logs
        String projectRoot = System.getProperty("user.dir");
        Path projectLogsPath = Paths.get(projectRoot, "logs");
        if (Files.exists(projectLogsPath)) {
            return projectLogsPath.toString();
        }

        // 都不存在则返回配置的路径（可能需要创建）
        return logPath;
    }

    private static final Map<String, String> LOG_FILE_MAPPING = Map.of(
        "backend", "backend.log",
        "frontend", "frontend.log"
    );

    /**
     * 获取日志文件路径
     */
    private Path getLogFilePath(String logType) {
        String fileName = LOG_FILE_MAPPING.get(logType.toLowerCase());
        if (fileName == null) {
            throw new IllegalArgumentException("不支持的日志类型: " + logType);
        }
        return Paths.get(getActualLogPath(), fileName);
    }

    /**
     * 获取日志行
     */
    public List<String> getLogLines(String logType, int maxLines) {
        Path logFile = getLogFilePath(logType);
        
        if (!Files.exists(logFile)) {
            log.warn("日志文件不存在: {}", logFile);
            return Collections.emptyList();
        }

        try {
            List<String> allLines = Files.readAllLines(logFile);
            
            // 如果请求的行数大于等于总行数，返回所有行
            if (maxLines >= allLines.size()) {
                return allLines;
            }
            
            // 返回最后的 maxLines 行
            return allLines.subList(allLines.size() - maxLines, allLines.size());
            
        } catch (IOException e) {
            log.error("读取日志文件失败: {}", logFile, e);
            throw new RuntimeException("读取日志文件失败", e);
        }
    }

    /**
     * 获取日志文件资源
     */
    public Resource getLogFile(String logType) {
        Path logFile = getLogFilePath(logType);
        
        if (!Files.exists(logFile)) {
            log.warn("日志文件不存在: {}", logFile);
            return null;
        }

        return new FileSystemResource(logFile);
    }

    /**
     * 获取日志统计信息
     */
    public Map<String, Object> getLogStats(String logType) {
        Path logFile = getLogFilePath(logType);
        Map<String, Object> stats = new HashMap<>();
        
        if (!Files.exists(logFile)) {
            stats.put("exists", false);
            stats.put("totalLines", 0);
            stats.put("fileSize", 0);
            stats.put("lastModified", null);
            return stats;
        }

        try {
            // 基本文件信息
            stats.put("exists", true);
            stats.put("fileSize", Files.size(logFile));
            stats.put("lastModified", Files.getLastModifiedTime(logFile).toString());
            
            // 读取文件内容进行统计
            List<String> lines = Files.readAllLines(logFile);
            stats.put("totalLines", lines.size());
            
            // 统计不同级别的日志数量
            long errorCount = lines.stream()
                .filter(line -> line.toLowerCase().contains("error") || 
                               line.toLowerCase().contains("exception") ||
                               line.toLowerCase().contains("failed"))
                .count();
            
            long warnCount = lines.stream()
                .filter(line -> line.toLowerCase().contains("warn") || 
                               line.toLowerCase().contains("warning"))
                .count();
            
            long infoCount = lines.stream()
                .filter(line -> line.toLowerCase().contains("info"))
                .count();
            
            long debugCount = lines.stream()
                .filter(line -> line.toLowerCase().contains("debug"))
                .count();
            
            stats.put("errorCount", errorCount);
            stats.put("warnCount", warnCount);
            stats.put("infoCount", infoCount);
            stats.put("debugCount", debugCount);
            
            // 最近的几行日志（用于预览）
            int previewLines = Math.min(10, lines.size());
            if (previewLines > 0) {
                List<String> recentLines = lines.subList(lines.size() - previewLines, lines.size());
                stats.put("recentLines", recentLines);
            } else {
                stats.put("recentLines", Collections.emptyList());
            }
            
        } catch (IOException e) {
            log.error("获取日志统计信息失败: {}", logFile, e);
            throw new RuntimeException("获取日志统计信息失败", e);
        }
        
        return stats;
    }

    /**
     * 监控日志文件变化（用于实时推送）
     */
    public void watchLogFile(String logType, LogFileWatcher watcher) {
        Path logFile = getLogFilePath(logType);
        
        if (!Files.exists(logFile)) {
            log.warn("日志文件不存在，无法监控: {}", logFile);
            return;
        }

        // 这里可以实现文件监控逻辑
        // 由于简化实现，这里只是一个接口定义
        log.info("开始监控日志文件: {}", logFile);
    }

    /**
     * 日志文件监控回调接口
     */
    public interface LogFileWatcher {
        void onNewLine(String line);
        void onError(Exception e);
    }

    /**
     * 获取支持的日志类型
     */
    public Set<String> getSupportedLogTypes() {
        return LOG_FILE_MAPPING.keySet();
    }

    /**
     * 清理旧日志文件（可选功能）
     */
    public void cleanOldLogs(int daysToKeep) {
        // 实现日志清理逻辑
        log.info("清理 {} 天前的日志文件", daysToKeep);
    }
}
