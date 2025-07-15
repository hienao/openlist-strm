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
public class User {
  private Long id;
  private String username;
  private String password;
  private Boolean enable = true;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Set<Role> roles = new HashSet<>();
}
