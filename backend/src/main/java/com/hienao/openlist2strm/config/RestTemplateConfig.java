package com.hienao.openlist2strm.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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

    // 启用重定向跟随
    factory.setOutputStreaming(false);

    RestTemplate restTemplate = new RestTemplate(factory);

    // 添加拦截器
    restTemplate.setInterceptors(getInterceptors());

    return restTemplate;
  }

  /**
   * 获取拦截器列表
   *
   * @return 拦截器列表
   */
  private List<ClientHttpRequestInterceptor> getInterceptors() {
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

    // 添加用户代理拦截器
    interceptors.add(
        (request, body, execution) -> {
          request.getHeaders().set("User-Agent", "OpenList-STRM/1.0");
          return execution.execute(request, body);
        });

    return interceptors;
  }
}
