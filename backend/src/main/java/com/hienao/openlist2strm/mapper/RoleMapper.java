package com.hienao.openlist2strm.mapper;

import com.hienao.openlist2strm.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper {

  Role findById(Long id);

  Role findByCode(String code);

  List<Role> findByCodeIn(List<String> codes);

  List<Role> findByIdIn(List<Long> ids);

  Role findByIdWithPermissions(@Param("roleId") Long roleId);

  List<Role> findByCodeContainingAndNameContaining(@Param("code") String code, @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

  long countByCodeContainingAndNameContaining(@Param("code") String code, @Param("name") String name);

  void save(Role role);

  void update(Role role);

  void deleteById(Long id);

  List<Role> findAll();

  // 角色权限关系管理
  void insertRolePermission(Long roleId, Long permissionId);
  
  void deleteRolePermissions(Long roleId);
  
  void deleteRolePermission(Long roleId, Long permissionId);
}