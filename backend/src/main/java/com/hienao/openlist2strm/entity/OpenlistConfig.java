package com.hienao.openlist2strm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * openlist配置信息实体类
 *
 * @author hienao
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenlistConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * openlist网址
     */
    private String baseUrl;

    /**
     * 用户令牌
     */
    private String token;

    /**
     * 初始路径
     */
    private String basePath;

    /**
     * 用户名
     */
    private String username;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Boolean isActive;
}