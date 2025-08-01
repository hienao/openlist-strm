package com.hienao.openlist2strm.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class Jwt {

  private final String secret;

  private final int expirationMin;

  private final JWTVerifier verifier;

  public Jwt(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration-min}") int expirationMin) {
    this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
    this.secret = secret;
    this.expirationMin = expirationMin;
  }

  public String getSubject(String token) {
    return JWT.decode(token).getSubject();
  }

  public Date getExpiresAt(String token) {
    return JWT.decode(token).getExpiresAt();
  }

  public Date getIssuedAt(String token) {
    return JWT.decode(token).getIssuedAt();
  }

  public boolean shouldRefresh(String token) {
    try {
      DecodedJWT decodedJWT = JWT.decode(token);
      Date issuedAt = decodedJWT.getIssuedAt();
      Date expiresAt = decodedJWT.getExpiresAt();
      Date now = new Date();

      // 计算token已使用时间（分钟）
      long usedMinutes = (now.getTime() - issuedAt.getTime()) / (1000 * 60);
      // 计算token剩余时间（分钟）
      long remainingMinutes = (expiresAt.getTime() - now.getTime()) / (1000 * 60);

      // 如果已使用超过7天（10080分钟）且剩余时间少于7天，则需要刷新
      return usedMinutes > 10080 && remainingMinutes < 10080;
    } catch (Exception e) {
      log.warn("检查token刷新状态失败", e);
      return false;
    }
  }

  public Boolean verify(String token) {
    try {
      verifier.verify(token);
      return Boolean.TRUE;
    } catch (JWTVerificationException e) {
      return Boolean.FALSE;
    }
  }

  public String extract(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (StringUtils.isNotEmpty(authorization) && authorization.startsWith("Bearer")) {
      return authorization.substring(7);
    } else {
      return null;
    }
  }

  public String create(String userIdentify) {
    return JWT.create()
        .withSubject(String.valueOf(userIdentify))
        .withIssuedAt(new Date())
        .withExpiresAt(
            Date.from(
                LocalDateTime.now()
                    .plusMinutes(expirationMin)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()))
        .sign(Algorithm.HMAC256(secret));
  }

  public String makeToken(
      HttpServletRequest request, HttpServletResponse response, String userIdentify) {
    String token = create(userIdentify);
    response.addHeader("Authorization", String.format("Bearer %s", token));
    return token;
  }

  public String refreshToken(String oldToken) {
    try {
      String subject = getSubject(oldToken);
      return create(subject);
    } catch (Exception e) {
      log.error("刷新token失败", e);
      return null;
    }
  }

  public void removeToken(HttpServletRequest request, HttpServletResponse response) {
    response.addHeader("Authorization", null);
  }
}
