package com.hienao.openlist2strm.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${cors.allowedOrigins}")
  private String allowedOrigins;

  @Value("${cors.allowedMethods}")
  private String allowedMethods;

  @Value("${cors.allowedHeaders}")
  private String allowedHeaders;

  @Value("${cors.allowedExposeHeaders}")
  private String allowedExposeHeaders;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**");
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 检查是否包含通配符 "*"
    if (allowedOrigins.contains("*")) {
      // 如果包含通配符，使用 allowedOriginPatterns 而不是 allowedOrigins
      configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
    } else {
      // 如果不包含通配符，使用 allowedOrigins
      configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
    }
    
    configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
    configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
    configuration.setExposedHeaders(Arrays.asList(allowedExposeHeaders.split(",")));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
