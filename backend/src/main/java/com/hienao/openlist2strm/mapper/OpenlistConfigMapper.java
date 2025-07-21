package com.hienao.openlist2strm.mapper;

import com.hienao.openlist2strm.entity.OpenlistConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * openlist配置信息Mapper接口
 *
 * @author hienao
 * @since 2024-01-01
 */
@Mapper
public interface OpenlistConfigMapper {

  /**
   * 根据ID查询配置
   *
   * @param id 主键ID
   * @return 配置信息
   */
  OpenlistConfig selectById(@Param("id") Long id);

  /**
   * 根据用户名查询配置
   *
   * @param username 用户名
   * @return 配置信息
   */
  OpenlistConfig selectByUsername(@Param("username") String username);

  /**
   * 查询所有启用的配置
   *
   * @return 配置列表
   */
  List<OpenlistConfig> selectActiveConfigs();

  /**
   * 查询所有配置
   *
   * @return 配置列表
   */
  List<OpenlistConfig> selectAll();

  /**
   * 插入配置
   *
   * @param config 配置信息
   * @return 影响行数
   */
  int insert(OpenlistConfig config);

  /**
   * 更新配置
   *
   * @param config 配置信息
   * @return 影响行数
   */
  int updateById(OpenlistConfig config);

  /**
   * 根据ID删除配置
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(@Param("id") Long id);

  /**
   * 启用/禁用配置
   *
   * @param id 主键ID
   * @param isActive 是否启用
   * @return 影响行数
   */
  int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
}
