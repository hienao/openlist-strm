package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.dto.sign.ChangePasswordDto;
import com.hienao.openlist2strm.dto.sign.SignInDto;
import com.hienao.openlist2strm.dto.sign.SignUpDto;
import com.hienao.openlist2strm.exception.BusinessException;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SignService {

  private static final String USER_INFO_FILE = "/Users/hienao/Code/Github/openlisttostrm/backend/data/userInfo.json";
  private final ObjectMapper objectMapper = new ObjectMapper();

  public void signUp(SignUpDto signUpDto) {
    File userFile = new File(USER_INFO_FILE);
    
    // 如果用户文件已存在，直接报错
    if (userFile.exists()) {
      throw new BusinessException("用户已存在，不允许重复注册");
    }
    
    try {
      // 创建目录（如果不存在）
      userFile.getParentFile().mkdirs();
      
      // 创建用户信息
      Map<String, String> userInfo = new HashMap<>();
      userInfo.put("username", signUpDto.getUsername());
      userInfo.put("pwd", md5Hash(signUpDto.getPassword()));
      
      // 保存到文件
      objectMapper.writeValue(userFile, userInfo);
      log.info("用户注册成功: {}", signUpDto.getUsername());
    } catch (IOException e) {
      log.error("保存用户信息失败", e);
      throw new BusinessException("注册失败");
    }
  }

  public String signIn(SignInDto signInDto) {
    File userFile = new File(USER_INFO_FILE);
    
    if (!userFile.exists()) {
      throw new BusinessException("用户不存在");
    }
    
    try {
      Map<String, String> userInfo = objectMapper.readValue(userFile, Map.class);
      String storedUsername = userInfo.get("username");
      String storedPassword = userInfo.get("pwd");
      
      if (!signInDto.getUsername().equals(storedUsername)) {
        throw new BusinessException("用户名错误");
      }
      
      if (!md5Hash(signInDto.getPassword()).equals(storedPassword)) {
        throw new BusinessException("密码错误");
      }
      
      log.info("用户登录成功: {}", signInDto.getUsername());
       return storedUsername;
     } catch (IOException e) {
       log.error("读取用户信息失败", e);
       throw new BusinessException("登录失败");
     }
   }
   
   public void changePassword(ChangePasswordDto changePasswordDto) {
     File userFile = new File(USER_INFO_FILE);
     
     if (!userFile.exists()) {
       throw new BusinessException("用户不存在");
     }
     
     try {
       Map<String, String> userInfo = objectMapper.readValue(userFile, Map.class);
       String storedPassword = userInfo.get("pwd");
       
       // 验证旧密码
       if (!md5Hash(changePasswordDto.getOldPassword()).equals(storedPassword)) {
         throw new BusinessException("旧密码错误");
       }
       
       // 更新密码
       userInfo.put("pwd", md5Hash(changePasswordDto.getNewPassword()));
       objectMapper.writeValue(userFile, userInfo);
       
       log.info("密码修改成功");
     } catch (IOException e) {
       log.error("修改密码失败", e);
       throw new BusinessException("修改密码失败");
     }
   }
   
   private String md5Hash(String input) {
     try {
       MessageDigest md = MessageDigest.getInstance("MD5");
       byte[] hashBytes = md.digest(input.getBytes());
       StringBuilder sb = new StringBuilder();
       for (byte b : hashBytes) {
         sb.append(String.format("%02x", b));
       }
       return sb.toString();
     } catch (NoSuchAlgorithmException e) {
       throw new RuntimeException("MD5算法不可用", e);
     }
   }
 }
