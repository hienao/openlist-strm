package com.hienao.openlist2strm.entity;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
  private Long id;
  private String code;
  private String name;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Set<User> users = new HashSet<>();
  private Set<Permission> permissions = new HashSet<>();
}
