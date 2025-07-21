package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 *
 * @author hienao
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final ObjectMapper objectMapper;
    
    private static final String CONFIG_DIR = "./data/config";
    private static final String CONFIG_FILE = "systemconf.json";
    private static final String CONFIG_PATH = CONFIG_DIR + "/" + CONFIG_FILE;

    /**
     * 获取系统配置
     *
     * @return 系统配置Map
     */
    public Map<String, Object> getSystemConfig() {
        try {
            // 确保配置目录存在
            createConfigDirectoryIfNotExists();
            
            File configFile = new File(CONFIG_PATH);
            if (!configFile.exists()) {
                // 如果配置文件不存在，返回默认配置
                return getDefaultConfig();
            }
            
            // 读取配置文件
            String content = Files.readString(Paths.get(CONFIG_PATH));
            if (content.trim().isEmpty()) {
                return getDefaultConfig();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(content, Map.class);
            
            // 确保包含默认配置项
            Map<String, Object> result = getDefaultConfig();
            result.putAll(config);
            
            return result;
        } catch (Exception e) {
            log.error("读取系统配置失败", e);
            return getDefaultConfig();
        }
    }

    /**
     * 保存系统配置
     *
     * @param config 配置Map
     */
    public void saveSystemConfig(Map<String, Object> config) throws IOException {
        // 确保配置目录存在
        createConfigDirectoryIfNotExists();
        
        // 读取现有配置
        Map<String, Object> existingConfig = getSystemConfig();
        
        // 合并配置
        existingConfig.putAll(config);
        
        // 写入配置文件
        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(existingConfig);
        
        Files.writeString(Paths.get(CONFIG_PATH), jsonContent);
        
        log.info("系统配置已保存到: {}", CONFIG_PATH);
    }

    /**
     * 获取默认配置
     *
     * @return 默认配置Map
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> defaultConfig = new HashMap<>();
        
        // 默认媒体文件后缀
        defaultConfig.put("mediaExtensions", List.of(".mp4", ".avi", ".rmvb", ".mkv"));
        
        return defaultConfig;
    }

    /**
     * 创建配置目录（如果不存在）
     */
    private void createConfigDirectoryIfNotExists() {
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                log.info("创建配置目录: {}", CONFIG_DIR);
            }
        } catch (IOException e) {
            log.error("创建配置目录失败: {}", CONFIG_DIR, e);
            throw new RuntimeException("创建配置目录失败", e);
        }
    }
}