package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.config.security.Jwt;
import com.hienao.openlist2strm.dto.sign.ChangePasswordDto;
import com.hienao.openlist2strm.dto.sign.SignInDto;
import com.hienao.openlist2strm.dto.sign.SignUpDto;
import com.hienao.openlist2strm.service.SignService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignController {

  private final SignService signService;

  private final Jwt jwt;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-in")
  void signIn(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody @Valid SignInDto signInDto) {
    String username = signService.signIn(signInDto);
    jwt.makeToken(request, response, username);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/sign-up")
  void signUp(@RequestBody @Valid SignUpDto signUpDto) {
    signService.signUp(signUpDto);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-out")
  void signOut(HttpServletRequest request, HttpServletResponse response) {
    jwt.removeToken(request, response);
  }
  
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/change-password")
  void changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
    signService.changePassword(changePasswordDto);
  }
}
