package com.hienao.openlist2strm.service;

import com.hienao.openlist2strm.dto.PageRequestDto;
import com.hienao.openlist2strm.dto.PageResponseDto;
import com.hienao.openlist2strm.dto.urp.*;
import com.hienao.openlist2strm.entity.Permission;
import com.hienao.openlist2strm.entity.Role;
import com.hienao.openlist2strm.entity.User;
import com.hienao.openlist2strm.exception.BusinessException;
import com.hienao.openlist2strm.mapper.PermissionMapper;
import com.hienao.openlist2strm.mapper.RoleMapper;
import com.hienao.openlist2strm.mapper.UserMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRolePermissionService {

  private static final String USER_NOT_FOUND = "用户不存在";

  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final PermissionMapper permissionMapper;
  private final PasswordEncoder passwordEncoder;

  public UserRolePermissionDto queryUniqueUserWithRolePermission(Long userId) {
    User user = userMapper.findByIdWithRolesAndPermissions(userId);
    if (user == null) {
      throw new BusinessException(USER_NOT_FOUND);
    }
    return convertToUserRolePermissionDto(user);
  }

  public PageResponseDto<List<UserRolePermissionDto>> pageQueryUser(
      PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {

    int offset = pageRequestDto.getPage() * pageRequestDto.getSize();
    int limit = pageRequestDto.getSize();

    List<User> users = userMapper.findByUsernameContaining(
        userQueryDto.getUsername(), offset, limit);
    long totalCount = userMapper.countByUsernameContaining(userQueryDto.getUsername());

    List<UserRolePermissionDto> userDtos =
        users.stream()
            .map(this::convertToUserRolePermissionDto)
            .collect(Collectors.toList());

    return new PageResponseDto<>(totalCount, userDtos);
  }

  public PageResponseDto<List<RoleDto>> pageQueryRole(
      PageRequestDto pageRequestDto, RoleQueryDto roleQueryDto) {

    int offset = pageRequestDto.getPage() * pageRequestDto.getSize();
    int limit = pageRequestDto.getSize();

    List<Role> roles = roleMapper.findByCodeContainingAndNameContaining(
        roleQueryDto.getRoleCode(), roleQueryDto.getRoleName(), offset, limit);
    long totalCount = roleMapper.countByCodeContainingAndNameContaining(
        roleQueryDto.getRoleCode(), roleQueryDto.getRoleName());

    List<RoleDto> roleDtos =
        roles.stream().map(this::convertToRoleDto).collect(Collectors.toList());

    return new PageResponseDto<>(totalCount, roleDtos);
  }

  public PageResponseDto<List<PermissionDto>> pageQueryPermission(
      PageRequestDto pageRequestDto, PermissionQueryDto permissionQueryDto) {

    int offset = pageRequestDto.getPage() * pageRequestDto.getSize();
    int limit = pageRequestDto.getSize();

    List<Permission> permissions = permissionMapper.findByCodeContainingAndNameContaining(
        permissionQueryDto.getPermissionCode(),
        permissionQueryDto.getPermissionName(),
        offset, limit);
    long totalCount = permissionMapper.countByCodeContainingAndNameContaining(
        permissionQueryDto.getPermissionCode(),
        permissionQueryDto.getPermissionName());

    List<PermissionDto> permissionDtos =
        permissions.stream()
            .map(this::convertToPermissionDto)
            .collect(Collectors.toList());

    return new PageResponseDto<>(totalCount, permissionDtos);
  }

  @Transactional
  public void upsertUser(UserUpsertDto userUpsertDto) {
    if (userUpsertDto.getId() != null) {
      User existingUser = userMapper.findById(userUpsertDto.getId());
      if (existingUser == null) {
        throw new BusinessException(USER_NOT_FOUND);
      }
      User user = new User();
      user.setId(userUpsertDto.getId());
      user.setUsername(userUpsertDto.getUsername());
      user.setPassword(passwordEncoder.encode(userUpsertDto.getPassword()));
      user.setEnable(userUpsertDto.getEnable());
      userMapper.update(user);
    } else {
      if (userMapper.existsByUsername(userUpsertDto.getUsername())) {
        throw new BusinessException("用户名已存在");
      }
      User user = new User();
      user.setUsername(userUpsertDto.getUsername());
      user.setPassword(passwordEncoder.encode(userUpsertDto.getPassword()));
      user.setEnable(userUpsertDto.getEnable());
      userMapper.save(user);
    }
  }

  @Transactional
  public void upsertRole(RoleUpsertDto roleUpsertDto) {
    if (roleUpsertDto.getId() != null) {
      Role existingRole = roleMapper.findById(roleUpsertDto.getId());
      if (existingRole == null) {
        throw new BusinessException("角色不存在");
      }
      Role role = new Role();
      role.setId(roleUpsertDto.getId());
      role.setCode(roleUpsertDto.getCode());
      role.setName(roleUpsertDto.getName());
      roleMapper.update(role);
    } else {
      Role role = new Role();
      role.setCode(roleUpsertDto.getCode());
      role.setName(roleUpsertDto.getName());
      roleMapper.save(role);
    }
  }

  @Transactional
  public void upsertPermission(PermissionUpsertDto permissionUpsertDto) {
    if (permissionUpsertDto.getId() != null) {
      Permission existingPermission = permissionMapper.findById(permissionUpsertDto.getId());
      if (existingPermission == null) {
        throw new BusinessException("权限不存在");
      }
      Permission permission = new Permission();
      permission.setId(permissionUpsertDto.getId());
      permission.setCode(permissionUpsertDto.getCode());
      permission.setName(permissionUpsertDto.getName());
      permissionMapper.update(permission);
    } else {
      Permission permission = new Permission();
      permission.setCode(permissionUpsertDto.getCode());
      permission.setName(permissionUpsertDto.getName());
      permissionMapper.save(permission);
    }
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(USER_NOT_FOUND);
    }
    userMapper.deleteById(userId);
  }

  @Transactional
  public void deleteRole(Long roleId) {
    Role role = roleMapper.findById(roleId);
    if (role == null) {
      throw new BusinessException("角色不存在");
    }
    roleMapper.deleteById(roleId);
  }

  @Transactional
  public void deletePermission(Long permissionId) {
    Permission permission = permissionMapper.findById(permissionId);
    if (permission == null) {
      throw new BusinessException("权限不存在");
    }
    permissionMapper.deleteById(permissionId);
  }

  @Transactional
  public void bindRoleToUser(Long userId, List<Long> roleIds) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(USER_NOT_FOUND);
    }

    // 删除用户现有的角色绑定
    userMapper.deleteUserRoles(userId);
    
    // 绑定新的角色
    if (roleIds != null && !roleIds.isEmpty()) {
      for (Long roleId : roleIds) {
        userMapper.insertUserRole(userId, roleId);
      }
    }
  }

  @Transactional
  public void bindPermissionToRole(Long roleId, List<Long> permissionIds) {
    Role role = roleMapper.findById(roleId);
    if (role == null) {
      throw new BusinessException("角色不存在");
    }

    // 删除角色现有的权限绑定
    roleMapper.deleteRolePermissions(roleId);
    
    // 绑定新的权限
    if (permissionIds != null && !permissionIds.isEmpty()) {
      for (Long permissionId : permissionIds) {
        roleMapper.insertRolePermission(roleId, permissionId);
      }
    }
  }

  private UserRolePermissionDto convertToUserRolePermissionDto(User user) {
    UserRolePermissionDto dto = new UserRolePermissionDto();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setPassword(user.getPassword());
    dto.setEnable(user.getEnable());
    dto.setCreateTime(user.getCreateTime());

    List<RoleDto> roleDtos =
        user.getRoles().stream().map(this::convertToRoleDto).collect(Collectors.toList());
    dto.setRoles(roleDtos);

    return dto;
  }

  private RoleDto convertToRoleDto(Role role) {
    RoleDto dto = new RoleDto();
    dto.setId(role.getId());
    dto.setCode(role.getCode());
    dto.setName(role.getName());

    List<PermissionDto> permissionDtos =
        role.getPermissions().stream()
            .map(this::convertToPermissionDto)
            .collect(Collectors.toList());
    dto.setPermissions(permissionDtos);

    return dto;
  }

  private PermissionDto convertToPermissionDto(Permission permission) {
    PermissionDto dto = new PermissionDto();
    dto.setId(permission.getId());
    dto.setCode(permission.getCode());
    dto.setName(permission.getName());
    return dto;
  }
}
