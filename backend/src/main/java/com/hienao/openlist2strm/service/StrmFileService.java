package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/**
 * STRM文件生成服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
public class StrmFileService {

    /**
     * 生成STRM文件
     *
     * @param strmBasePath STRM文件基础路径
     * @param relativePath 相对路径（相对于任务配置的path）
     * @param fileName 文件名
     * @param fileUrl 文件URL
     * @param renameRegex 重命名正则表达式（可选）
     */
    public void generateStrmFile(String strmBasePath, String relativePath, String fileName, String fileUrl, String renameRegex) {
        try {
            // 处理文件名重命名
            String finalFileName = processFileName(fileName, renameRegex);
            
            // 构建STRM文件路径
            Path strmFilePath = buildStrmFilePath(strmBasePath, relativePath, finalFileName);
            
            // 确保目录存在
            createDirectoriesIfNotExists(strmFilePath.getParent());
            
            // 写入STRM文件内容
            writeStrmFile(strmFilePath, fileUrl);
            
            log.info("生成STRM文件成功: {}", strmFilePath);
            
        } catch (Exception e) {
            log.error("生成STRM文件失败: {}, 错误: {}", fileName, e.getMessage(), e);
            throw new BusinessException("生成STRM文件失败: " + fileName + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 处理文件名（重命名和添加.strm扩展名）
     *
     * @param originalFileName 原始文件名
     * @param renameRegex 重命名正则表达式
     * @return 处理后的文件名
     */
    private String processFileName(String originalFileName, String renameRegex) {
        String processedName = originalFileName;
        
        // 应用重命名规则
        if (StringUtils.hasText(renameRegex)) {
            try {
                // 简单的正则替换，可以根据需要扩展
                // 格式: "原始模式|替换内容"
                if (renameRegex.contains("|")) {
                    String[] parts = renameRegex.split("\\|", 2);
                    String pattern = parts[0];
                    String replacement = parts[1];
                    processedName = processedName.replaceAll(pattern, replacement);
                    log.debug("文件重命名: {} -> {}", originalFileName, processedName);
                }
            } catch (Exception e) {
                log.warn("重命名规则应用失败: {}, 使用原始文件名", renameRegex, e);
            }
        }
        
        // 移除原始扩展名并添加.strm扩展名
        int lastDotIndex = processedName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            processedName = processedName.substring(0, lastDotIndex);
        }
        processedName += ".strm";
        
        return processedName;
    }

    /**
     * 构建STRM文件路径
     *
     * @param strmBasePath STRM基础路径
     * @param relativePath 相对路径
     * @param fileName 文件名
     * @return STRM文件路径
     */
    private Path buildStrmFilePath(String strmBasePath, String relativePath, String fileName) {
        Path basePath = Paths.get(strmBasePath);
        
        if (StringUtils.hasText(relativePath)) {
            // 清理相对路径
            String cleanRelativePath = relativePath.replaceAll("^/+", "").replaceAll("/+$", "");
            if (StringUtils.hasText(cleanRelativePath)) {
                basePath = basePath.resolve(cleanRelativePath);
            }
        }
        
        return basePath.resolve(fileName);
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param directoryPath 目录路径
     */
    private void createDirectoriesIfNotExists(Path directoryPath) {
        try {
            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.debug("创建目录: {}", directoryPath);
            }
        } catch (IOException e) {
            throw new BusinessException("创建目录失败: " + directoryPath + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 写入STRM文件内容
     *
     * @param strmFilePath STRM文件路径
     * @param fileUrl 文件URL
     */
    private void writeStrmFile(Path strmFilePath, String fileUrl) {
        try {
            // STRM文件内容就是文件的URL
            Files.writeString(strmFilePath, fileUrl, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("写入STRM文件: {} -> {}", strmFilePath, fileUrl);
        } catch (IOException e) {
            throw new BusinessException("写入STRM文件失败: " + strmFilePath + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 计算相对路径
     *
     * @param taskPath 任务配置的路径
     * @param filePath 文件的完整路径
     * @return 相对路径
     */
    public String calculateRelativePath(String taskPath, String filePath) {
        if (!StringUtils.hasText(taskPath) || !StringUtils.hasText(filePath)) {
            return "";
        }
        
        // 标准化路径
        String normalizedTaskPath = taskPath.replaceAll("/+$", ""); // 移除末尾斜杠
        String normalizedFilePath = filePath;
        
        // 如果文件路径以任务路径开头，计算相对路径
        if (normalizedFilePath.startsWith(normalizedTaskPath)) {
            String relativePath = normalizedFilePath.substring(normalizedTaskPath.length());
            relativePath = relativePath.replaceAll("^/+", ""); // 移除开头斜杠
            
            // 移除文件名，只保留目录路径
            int lastSlashIndex = relativePath.lastIndexOf('/');
            if (lastSlashIndex > 0) {
                return relativePath.substring(0, lastSlashIndex);
            }
        }
        
        return "";
    }

    /**
     * 检查文件是否为视频文件
     *
     * @param fileName 文件名
     * @return 是否为视频文件
     */
    public boolean isVideoFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String lowerCaseFileName = fileName.toLowerCase();
        String[] videoExtensions = {
            ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".m4v",
            ".3gp", ".3g2", ".asf", ".divx", ".f4v", ".m2ts", ".m2v", ".mts",
            ".ogv", ".rm", ".rmvb", ".ts", ".vob", ".xvid"
        };
        
        for (String extension : videoExtensions) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 清空STRM目录下的所有文件和文件夹
     * 用于全量执行时清理旧的STRM文件
     *
     * @param strmBasePath STRM基础路径
     */
    public void clearStrmDirectory(String strmBasePath) {
        if (!StringUtils.hasText(strmBasePath)) {
            log.warn("STRM基础路径为空，跳过清理操作");
            return;
        }
        
        try {
            Path strmPath = Paths.get(strmBasePath);
            
            // 检查目录是否存在
            if (!Files.exists(strmPath)) {
                log.info("STRM目录不存在，无需清理: {}", strmPath);
                return;
            }
            
            // 检查是否为目录
            if (!Files.isDirectory(strmPath)) {
                log.warn("STRM路径不是目录，跳过清理: {}", strmPath);
                return;
            }
            
            log.info("开始清理STRM目录: {}", strmPath);
            
            // 递归删除目录下的所有文件和子目录
            Files.walk(strmPath)
                .sorted((path1, path2) -> path2.compareTo(path1)) // 先删除子文件/目录，再删除父目录
                .filter(path -> !path.equals(strmPath)) // 保留根目录本身
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("删除: {}", path);
                    } catch (IOException e) {
                        log.warn("删除文件/目录失败: {}, 错误: {}", path, e.getMessage());
                    }
                });
            
            log.info("STRM目录清理完成: {}", strmPath);
            
        } catch (Exception e) {
            log.error("清理STRM目录失败: {}, 错误: {}", strmBasePath, e.getMessage(), e);
            throw new BusinessException("清理STRM目录失败: " + strmBasePath + ", 错误: " + e.getMessage());
        }
    }
}