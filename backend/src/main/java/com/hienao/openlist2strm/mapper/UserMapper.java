package com.hienao.openlist2strm.mapper;

import com.hienao.openlist2strm.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserMapper {

  User findById(Long id);

  User findByUsername(String username);

  boolean existsByUsername(String username);

  User findByIdWithRolesAndPermissions(@Param("userId") Long userId);

  List<User> findByUsernameContaining(@Param("username") String username, @Param("offset") int offset, @Param("limit") int limit);

  long countByUsernameContaining(@Param("username") String username);

  void save(User user);

  void update(User user);

  void deleteById(Long id);

  List<User> findAll();

  // 用户角色关系管理
  void insertUserRole(Long userId, Long roleId);
  
  void deleteUserRoles(Long userId);
  
  void deleteUserRole(Long userId, Long roleId);
}