package com.hienao.openlist2strm.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestfulAuthenticationEntryPointHandler
    implements AccessDeniedHandler, AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Void> result = ApiResponse.error(401, "认证失败，请先登录");

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), result);
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Void> result = ApiResponse.error(403, "访问被拒绝，权限不足");

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), result);
  }
}
