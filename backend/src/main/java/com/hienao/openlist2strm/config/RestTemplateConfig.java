package com.hienao.openlist2strm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置类
 *
 * @author hienao
 * @since 2024-01-01
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout((int) Duration.ofSeconds(30).toMillis());
        
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
        
        return new RestTemplate(factory);
    }
}