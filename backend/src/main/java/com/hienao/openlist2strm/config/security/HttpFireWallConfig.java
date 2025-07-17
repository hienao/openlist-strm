package com.hienao.openlist2strm.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class HttpFireWallConfig {

  @Bean
  public HttpFirewall getHttpFirewall() {
    return new StrictHttpFirewall();
  }

  @Bean
  public RequestRejectedHandler requestRejectedHandler() {
    return (request, response, requestRejectedException) -> {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      
      ApiResponse<Void> result = ApiResponse.error(400, "请求被拒绝: " + requestRejectedException.getMessage());
      
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(response.getOutputStream(), result);
    };
  }
}
