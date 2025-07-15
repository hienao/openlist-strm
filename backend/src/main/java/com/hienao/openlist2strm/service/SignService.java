package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.sign.SignInDto;
import com.hienao.openlist2strm.dto.sign.SignUpDto;
import com.hienao.openlist2strm.entity.Role;
import com.hienao.openlist2strm.entity.User;
import com.hienao.openlist2strm.exception.BusinessException;
import com.hienao.openlist2strm.model.urp.ERole;
import com.hienao.openlist2strm.mapper.RoleMapper;
import com.hienao.openlist2strm.mapper.UserMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignService {

  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signUp(SignUpDto signUpDto) {
    if (userMapper.existsByUsername(signUpDto.getUsername())) {
      throw new BusinessException("用户名已存在");
    }

    User user = new User();
    user.setUsername(signUpDto.getUsername());
    user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
    user.setEnable(true);

    // 保存用户
    userMapper.save(user);

    // 分配默认角色
    Role userRole = roleMapper.findByCode(ERole.GENERAL.name());
    if (userRole == null) {
      throw new BusinessException("默认角色不存在");
    }
    
    // 绑定用户角色
    userMapper.insertUserRole(user.getId(), userRole.getId());
  }

  public Long signIn(SignInDto signInDto) {
    User user = userMapper.findByUsername(signInDto.getUsername());
    if (user == null) {
      throw new BusinessException("用户不存在");
    }

    if (!passwordEncoder.matches(signInDto.getPassword(), user.getPassword())) {
      throw new BusinessException("密码错误");
    }
    if (!user.getEnable()) {
      throw new BusinessException("用户已被禁用");
    }
    return user.getId();
  }
}
