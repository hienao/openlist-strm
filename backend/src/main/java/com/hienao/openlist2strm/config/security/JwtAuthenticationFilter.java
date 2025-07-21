package com.hienao.openlist2strm.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Setter
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final Jwt jwt;

  private final UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = jwt.extract(request);
    if (StringUtils.isNotEmpty(token) && jwt.verify(token)) {
      try {
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwt.getSubject(token));

        // 检查是否需要刷新token
        if (jwt.shouldRefresh(token)) {
          String newToken = jwt.refreshToken(token);
          if (StringUtils.isNotEmpty(newToken)) {
            response.addHeader("Authorization", String.format("Bearer %s", newToken));
            log.info("Token已自动刷新");
          }
        }

        JwtAuthenticationToken authenticated =
            JwtAuthenticationToken.authenticated(userDetails, token, userDetails.getAuthorities());
        authenticated.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticated);
      } catch (Exception e) {
        log.error("jwt with invalid user id {}", jwt.getSubject(token), e);
      }
    }
    filterChain.doFilter(request, response);
  }
}
