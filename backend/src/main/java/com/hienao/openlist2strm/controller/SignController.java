package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.config.security.Jwt;
import com.hienao.openlist2strm.dto.sign.ChangePasswordDto;
import com.hienao.openlist2strm.dto.sign.SignInDto;
import com.hienao.openlist2strm.dto.sign.SignUpDto;
import com.hienao.openlist2strm.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class SignController {

  private final SignService signService;

  private final Jwt jwt;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-in")
  @Operation(summary = "用户登录", description = "用户登录接口，成功后返回JWT token")
  Map<String, Object> signIn(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody @Valid SignInDto signInDto) {
    String username = signService.signIn(signInDto);
    String token = jwt.makeToken(request, response, username);
    Map<String, Object> result = new HashMap<>();
    result.put("message", "登录成功");
    result.put("username", username);
    result.put("token", token);
    result.put("expiresAt", jwt.getExpiresAt(token));
    return result;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/sign-up")
  @Operation(summary = "用户注册", description = "用户注册接口")
  Map<String, String> signUp(@RequestBody @Valid SignUpDto signUpDto) {
    signService.signUp(signUpDto);
    Map<String, String> result = new HashMap<>();
    result.put("message", "注册成功");
    return result;
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-out")
  @Operation(summary = "用户登出", description = "用户登出接口，需要JWT认证")
  @SecurityRequirement(name = "Bearer Authentication")
  Map<String, String> signOut(HttpServletRequest request, HttpServletResponse response) {
    jwt.removeToken(request, response);
    Map<String, String> result = new HashMap<>();
    result.put("message", "登出成功");
    return result;
  }
  
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/change-password")
  @Operation(summary = "修改密码", description = "修改用户密码接口，需要JWT认证")
  @SecurityRequirement(name = "Bearer Authentication")
  Map<String, String> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
    signService.changePassword(changePasswordDto);
    Map<String, String> result = new HashMap<>();
    result.put("message", "密码修改成功");
    return result;
  }
}
