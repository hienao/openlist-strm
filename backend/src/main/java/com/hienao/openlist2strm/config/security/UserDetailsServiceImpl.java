package com.hienao.openlist2strm.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final String USER_INFO_FILE = "./data/config/userInfo.json";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    File userFile = new File(USER_INFO_FILE);

    if (!userFile.exists()) {
      throw new UsernameNotFoundException(String.format("用户 %s 不存在", username));
    }

    try {
      Map<String, String> userInfo = objectMapper.readValue(userFile, Map.class);
      String storedUsername = userInfo.get("username");
      String storedPassword = userInfo.get("pwd");

      if (!username.equals(storedUsername)) {
        throw new UsernameNotFoundException(String.format("用户 %s 不存在", username));
      }

      return new User(
          storedUsername,
          storedPassword,
          true, // enabled
          true, // accountNonExpired
          true, // credentialsNonExpired
          true, // accountNonLocked
          Collections.emptyList() // authorities - 单用户系统无需权限
          );
    } catch (IOException e) {
      log.error("读取用户信息失败", e);
      throw new UsernameNotFoundException(String.format("用户 %s 验证失败", username), e);
    }
  }
}
