package com.hienao.openlist2strm.controller;

import com.hienao.openlist2strm.dto.PageRequestDto;
import com.hienao.openlist2strm.dto.PageResponseDto;
import com.hienao.openlist2strm.dto.urp.*;
import com.hienao.openlist2strm.service.UserRolePermissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-role-permission")
@RequiredArgsConstructor
public class UserRolePermissionController {

  private static final String READ_PERMISSION =
      "hasAuthority(T(com.hienao.openlist2strm.model.urp.EPermission).READ_USER_ROLE_PERMISSION)";
  private static final String WRITE_PERMISSION =
      "hasAuthority(T(com.hienao.openlist2strm.model.urp.EPermission).WRITE_USER_ROLE_PERMISSION)";
  private static final String DELETE_PERMISSION =
      "hasAuthority(T(com.hienao.openlist2strm.model.urp.EPermission).DELETE_USER_ROLE_PERMISSION)";

  private final UserRolePermissionService userRolePermissionService;

  @PreAuthorize(READ_PERMISSION)
  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  public PageResponseDto<List<UserRolePermissionDto>> queryUsers(
      @ModelAttribute PageRequestDto pageRequestDto, @ModelAttribute UserQueryDto userQueryDto) {
    return userRolePermissionService.pageQueryUser(pageRequestDto, userQueryDto);
  }

  @PreAuthorize(READ_PERMISSION)
  @GetMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserRolePermissionDto queryUser(@PathVariable Long userId) {
    return userRolePermissionService.queryUniqueUserWithRolePermission(userId);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public void createUser(@RequestBody @Valid UserUpsertDto userUpsertDto) {
    userUpsertDto.setId(null); // 确保是创建操作
    userRolePermissionService.upsertUser(userUpsertDto);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PutMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public void updateUser(
      @PathVariable Long userId, @RequestBody @Valid UserUpsertDto userUpsertDto) {
    userUpsertDto.setId(userId);
    userRolePermissionService.upsertUser(userUpsertDto);
  }

  @PreAuthorize(DELETE_PERMISSION)
  @DeleteMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long userId) {
    userRolePermissionService.deleteUser(userId);
  }

  @PreAuthorize(READ_PERMISSION)
  @GetMapping("/roles")
  @ResponseStatus(HttpStatus.OK)
  public PageResponseDto<List<RoleDto>> queryRoles(
      @ModelAttribute PageRequestDto pageRequestDto, @ModelAttribute RoleQueryDto roleQueryDto) {
    return userRolePermissionService.pageQueryRole(pageRequestDto, roleQueryDto);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PostMapping("/roles")
  @ResponseStatus(HttpStatus.CREATED)
  public void createRole(@RequestBody @Valid RoleUpsertDto roleUpsertDto) {
    roleUpsertDto.setId(null); // 确保是创建操作
    userRolePermissionService.upsertRole(roleUpsertDto);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PutMapping("/roles/{roleId}")
  @ResponseStatus(HttpStatus.OK)
  public void updateRole(
      @PathVariable Long roleId, @RequestBody @Valid RoleUpsertDto roleUpsertDto) {
    roleUpsertDto.setId(roleId);
    userRolePermissionService.upsertRole(roleUpsertDto);
  }

  @PreAuthorize(DELETE_PERMISSION)
  @DeleteMapping("/roles/{roleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRole(@PathVariable Long roleId) {
    userRolePermissionService.deleteRole(roleId);
  }

  @PreAuthorize(READ_PERMISSION)
  @GetMapping("/permissions")
  @ResponseStatus(HttpStatus.OK)
  public PageResponseDto<List<PermissionDto>> queryPermissions(
      @ModelAttribute PageRequestDto pageRequestDto,
      @ModelAttribute PermissionQueryDto permissionQueryDto) {
    return userRolePermissionService.pageQueryPermission(pageRequestDto, permissionQueryDto);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PostMapping("/permissions")
  @ResponseStatus(HttpStatus.CREATED)
  public void createPermission(@RequestBody @Valid PermissionUpsertDto permissionUpsertDto) {
    permissionUpsertDto.setId(null); // 确保是创建操作
    userRolePermissionService.upsertPermission(permissionUpsertDto);
  }

  @PreAuthorize(WRITE_PERMISSION)
  @PutMapping("/permissions/{permissionId}")
  @ResponseStatus(HttpStatus.OK)
  public void updatePermission(
      @PathVariable Long permissionId,
      @RequestBody @Valid PermissionUpsertDto permissionUpsertDto) {
    permissionUpsertDto.setId(permissionId);
    userRolePermissionService.upsertPermission(permissionUpsertDto);
  }

  @PreAuthorize(DELETE_PERMISSION)
  @DeleteMapping("/permissions/{permissionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePermission(@PathVariable Long permissionId) {
    userRolePermissionService.deletePermission(permissionId);
  }
}
