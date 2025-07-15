package com.hienao.openlist2strm.mapper;

import com.hienao.openlist2strm.entity.Permission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionMapper {

  Permission findById(Long id);

  Permission findByCode(String code);

  List<Permission> findByCodeIn(List<String> codes);

  List<Permission> findByIdIn(List<Long> ids);

  List<Permission> findByCodeContainingAndNameContaining(@Param("code") String code, @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

  long countByCodeContainingAndNameContaining(@Param("code") String code, @Param("name") String name);

  void save(Permission permission);

  void update(Permission permission);

  void deleteById(Long id);

  List<Permission> findAll();
}