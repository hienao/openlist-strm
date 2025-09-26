package com.hienao.openlist2strm.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  // CORS配置 - 开发环境和生产环境都使用通配符
  private static final String[] ALLOWED_ORIGINS = {"*"};

  private static final String[] ALLOWED_METHODS = {"*"};
  private static final String[] ALLOWED_HEADERS = {"*"};
  private static final String[] ALLOWED_EXPOSE_HEADERS = {"*"};

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**");
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 设置允许的源（包含通配符）
    configuration.setAllowedOriginPatterns(Arrays.asList(ALLOWED_ORIGINS));

    // 设置允许的方法
    configuration.setAllowedMethods(Arrays.asList(ALLOWED_METHODS));

    // 设置允许的头部
    configuration.setAllowedHeaders(Arrays.asList(ALLOWED_HEADERS));

    // 设置暴露的头部
    configuration.setExposedHeaders(Arrays.asList(ALLOWED_EXPOSE_HEADERS));

    // 允许携带认证信息
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
