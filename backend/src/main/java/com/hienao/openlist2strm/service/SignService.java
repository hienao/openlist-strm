package com.hienao.openlist2strm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hienao.openlist2strm.config.PathConfiguration;
import com.hienao.openlist2strm.dto.sign.ChangePasswordDto;
import com.hienao.openlist2strm.dto.sign.SignInDto;
import com.hienao.openlist2strm.dto.sign.SignUpDto;
import com.hienao.openlist2strm.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

  private static final String PWD_KEY = "pwd";
  private static final String CONTAINER_INSTANCE_ID_KEY = "containerInstanceId";
  private final ObjectMapper objectMapper;
  private final PathConfiguration pathConfiguration;

  /** 获取用户信息文件路径 */
  private String getUserInfoFilePath() {
    return pathConfiguration.getUserInfo();
  }

  /**
   * 容器启动时检查并生成容器实例ID
   *
   * @author hienao
   * @since 2024-01-01
   */
  @PostConstruct
  public void initializeContainerInstanceId() {
    try {
      ensureContainerInstanceId();
    } catch (Exception e) {
      log.error("初始化容器实例ID失败", e);
    }
  }

  public void signUp(SignUpDto signUpDto) {
    // 检查用户是否已存在（检查文件中的用户名和密码字段）
    if (checkUserExists()) {
      throw new BusinessException("用户已存在，不允许重复注册");
    }

    File userFile = new File(getUserInfoFilePath());

    try {
      // 创建目录（如果不存在）
      userFile.getParentFile().mkdirs();

      // 创建用户信息
      Map<String, String> userInfo = new HashMap<>();
      userInfo.put("username", signUpDto.getUsername());
      userInfo.put(PWD_KEY, md5Hash(signUpDto.getPassword()));

      // 保存到文件
      objectMapper.writeValue(userFile, userInfo);
      log.info("用户注册成功: {}", signUpDto.getUsername());
    } catch (IOException e) {
      log.error("保存用户信息失败", e);
      throw new BusinessException("注册失败", e);
    }
  }

  public String signIn(SignInDto signInDto) {
    Map<String, String> userInfo = readUserInfoOrThrow();
    String storedUsername = userInfo.get("username");
    String storedPassword = userInfo.get(PWD_KEY);

    if (!signInDto.getUsername().equals(storedUsername)) {
      throw new BusinessException("用户名错误");
    }

    if (!md5Hash(signInDto.getPassword()).equals(storedPassword)) {
      throw new BusinessException("密码错误");
    }

    log.info("用户登录成功: {}", signInDto.getUsername());
    return storedUsername;
  }

  public void changePassword(ChangePasswordDto changePasswordDto) {
    Map<String, String> userInfo = readUserInfoOrThrow();
    String storedPassword = userInfo.get(PWD_KEY);

    // 验证旧密码
    if (!md5Hash(changePasswordDto.getOldPassword()).equals(storedPassword)) {
      throw new BusinessException("旧密码错误");
    }

    // 更新密码
    userInfo.put(PWD_KEY, md5Hash(changePasswordDto.getNewPassword()));

    try {
      File userFile = new File(getUserInfoFilePath());
      objectMapper.writeValue(userFile, userInfo);
      log.info("密码修改成功");
    } catch (IOException e) {
      log.error("修改密码失败", e);
      throw new BusinessException("修改密码失败", e);
    }
  }

  public boolean checkUserExists() {
    File userFile = new File(getUserInfoFilePath());
    if (!userFile.exists()) {
      return false;
    }

    try {
      Map<String, String> userInfo = objectMapper.readValue(userFile, Map.class);
      String username = userInfo.get("username");
      String password = userInfo.get("pwd");

      // 检查用户名和密码字段是否都存在且不为空
      return username != null
          && !username.trim().isEmpty()
          && password != null
          && !password.trim().isEmpty();
    } catch (IOException e) {
      log.error("读取用户信息失败", e);
      return false;
    }
  }

  /**
   * 确保容器实例ID存在，如果不存在则生成一个
   *
   * @author hienao
   * @since 2024-01-01
   */
  public void ensureContainerInstanceId() {
    File userFile = new File(getUserInfoFilePath());

    try {
      Map<String, String> userInfo;

      if (userFile.exists()) {
        // 读取现有配置
        userInfo = objectMapper.readValue(userFile, Map.class);
      } else {
        // 创建新配置
        userFile.getParentFile().mkdirs();
        userInfo = new HashMap<>();
      }

      // 检查是否存在容器实例ID
      if (!userInfo.containsKey(CONTAINER_INSTANCE_ID_KEY)
          || userInfo.get(CONTAINER_INSTANCE_ID_KEY) == null
          || userInfo.get(CONTAINER_INSTANCE_ID_KEY).trim().isEmpty()) {

        // 生成新的容器实例ID
        String containerInstanceId = UUID.randomUUID().toString();
        userInfo.put(CONTAINER_INSTANCE_ID_KEY, containerInstanceId);

        // 保存到文件
        objectMapper.writeValue(userFile, userInfo);
        log.info("生成新的容器实例ID: {}", containerInstanceId);
      } else {
        log.info("容器实例ID已存在: {}", userInfo.get(CONTAINER_INSTANCE_ID_KEY));
      }

    } catch (IOException e) {
      log.error("处理容器实例ID失败", e);
      throw new BusinessException("初始化容器实例ID失败", e);
    }
  }

  /**
   * 获取容器实例ID
   *
   * @return 容器实例ID
   * @author hienao
   * @since 2024-01-01
   */
  public String getContainerInstanceId() {
    File userFile = new File(getUserInfoFilePath());

    if (!userFile.exists()) {
      return null;
    }

    try {
      Map<String, String> userInfo = objectMapper.readValue(userFile, Map.class);
      return userInfo.get(CONTAINER_INSTANCE_ID_KEY);
    } catch (IOException e) {
      log.error("读取容器实例ID失败", e);
      return null;
    }
  }

  /**
   * 读取用户信息，如果用户不存在则抛出异常
   *
   * @return 用户信息Map
   * @throws BusinessException 用户不存在时抛出
   * @author hienao
   * @since 2024-01-01
   */
  private Map<String, String> readUserInfoOrThrow() {
    File userFile = new File(getUserInfoFilePath());

    if (!userFile.exists()) {
      throw new BusinessException("用户不存在");
    }

    try {
      return objectMapper.readValue(userFile, Map.class);
    } catch (IOException e) {
      log.error("读取用户信息失败", e);
      throw new BusinessException("读取用户信息失败", e);
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
